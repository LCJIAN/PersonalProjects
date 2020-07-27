package com.org.firefighting.ui.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.FileUtils;
import com.org.chat.SimpleAudioPlayHelper;
import com.org.chat.SmackClient;
import com.org.chat.SmackClientService;
import com.org.chat.VCardLoader;
import com.org.chat.VoiceRecorderView;
import com.org.chat.data.db.entity.Message;
import com.org.chat.data.entity.JsonExtraBodyItem;
import com.org.firefighting.App;
import com.org.firefighting.BuildConfig;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.common.ImageViewerActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_state_info)
    TextView tv_state_info;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.rv_chat_message)
    RecyclerView rv_chat_message;

    @BindView(R.id.btn_switch_keyboard)
    ImageButton btn_switch_keyboard;
    @BindView(R.id.tv_hold_voice)
    VoiceRecorderView tv_hold_voice;
    @BindView(R.id.et_message)
    EditText et_message;
    @BindView(R.id.btn_get_image)
    ImageButton btn_get_image;
    @BindView(R.id.btn_send)
    TextView btn_send;

    private SlimAdapter mAdapter;

    protected String mOwnerJid;
    protected String mOppositeJid;
    protected String mOppositeName;

    private Disposable mDisposable;
    private Disposable mDisposableR;
    private Disposable mDisposableS;

    private VCardLoader mVCardLoader;
    private SmackClient mSmackClient;
    private SmackClient.StateChangeListener mStateChangeListener = state -> RxBus.getInstance().send(state);
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSmackClient = ((SmackClientService.LocalBinder) service).getService().getSmackClient();
            mSmackClient.addStateChangeListener(mStateChangeListener);
            mVCardLoader = VCardLoader.getInstance(mSmackClient);
            RxBus.getInstance().send(mSmackClient.getState());
            RxBus.getInstance().send(Boolean.TRUE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSmackClient.removeStateChangeListener(mStateChangeListener);
            mSmackClient = null;
        }
    };

    private boolean mIsVoice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mOwnerJid = getIntent().getStringExtra("owner_jid");
        mOppositeJid = getIntent().getStringExtra("opposite_jid");
        mOppositeName = getIntent().getStringExtra("opposite_name");

        btn_nav_back.setOnClickListener(this);
        btn_get_image.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_switch_keyboard.setOnClickListener(this);
        tv_hold_voice.setAudioRecorderFinishListener(new VoiceRecorderView.AudioRecorderFinishListener() {
            @Override
            public void onFinishRecorder(float audioSeconds, String audioFilePath) {
                JsonExtraBodyItem item = new JsonExtraBodyItem();
                item.contentType = "audio/amr;base64;" + ((int) audioSeconds);
                item.content = FileUtils.fileToBase64(new File(audioFilePath));
                mSmackClient.sendMessage(mOppositeJid, item);
            }
        });

        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<IncomingMessage>() {

                    private Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                    private Gson gson = new Gson();
                    private Map<String, Boolean> isVoicePlayings = new HashMap<>();

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.chat_msg_in_item;
                    }

                    @Override
                    public void onBind(IncomingMessage data, SlimAdapter.SlimViewHolder<IncomingMessage> viewHolder) {
                        TextView tv_in_msg_send_time = viewHolder.findViewById(R.id.tv_in_msg_send_time);
                        ImageView iv_in_avatar = viewHolder.findViewById(R.id.iv_in_avatar);
                        TextView tv_in_msg_content = viewHolder.findViewById(R.id.tv_in_msg_content);
                        ImageView iv_in_msg_content = viewHolder.findViewById(R.id.iv_in_msg_content);
                        LinearLayout ll_voice_in_msg_content = viewHolder.findViewById(R.id.ll_voice_in_msg_content);
                        View view_voice_anim = viewHolder.findViewById(R.id.view_voice_anim);
                        TextView tv_voice_duration = viewHolder.findViewById(R.id.tv_voice_duration);
                        View v_in_msg_clicked = viewHolder.findViewById(R.id.v_in_msg_clicked);

                        int position = viewHolder.getAbsoluteAdapterPosition();
                        long lastMsgTime;
                        if (position < 0 || position + 2 > mAdapter.getData().size()) {
                            lastMsgTime = 0;
                        } else {
                            Object o = mAdapter.getData().get(position + 1);
                            if (o instanceof IncomingMessage) {
                                lastMsgTime = ((IncomingMessage) o).msg.createTime.getTime();
                            } else {
                                lastMsgTime = ((OutgoingMessage) o).msg.createTime.getTime();
                            }
                        }
                        long thisMsgTime = data.msg.createTime.getTime();
                        if (thisMsgTime - lastMsgTime > 4 * 60 * 1000) {
                            tv_in_msg_send_time.setVisibility(View.VISIBLE);
                            tv_in_msg_send_time.setText(DateUtils.convertDateToStr(data.msg.createTime, DateUtils.YYYY_MM_DD_HH_MM_SS));
                        } else {
                            tv_in_msg_send_time.setVisibility(View.GONE);
                        }

                        v_in_msg_clicked.setVisibility(View.GONE);
                        mVCardLoader.displayAvatar(JidCreate.entityBareFromOrThrowUnchecked(mOppositeJid), iv_in_avatar, placeholder);
                        if (TextUtils.isEmpty(data.msg.body)) {
                            tv_in_msg_content.setVisibility(View.GONE);
                            List<JsonExtraBodyItem> items = gson.fromJson(data.msg.otherBodies, new TypeToken<List<JsonExtraBodyItem>>() {
                            }.getType());
                            String t = "";
                            String c = "";
                            if (items != null && !items.isEmpty()) {
                                t = items.get(0).contentType;
                                c = items.get(0).content;
                            }
                            if (TextUtils.equals(t, "image/jpeg;filepath")) {
                                ll_voice_in_msg_content.setVisibility(View.GONE);
                                iv_in_msg_content.setVisibility(View.VISIBLE);
                                GlideApp.with(iv_in_msg_content)
                                        .load(c)
                                        .downsample(DownsampleStrategy.AT_MOST)
                                        .override(iv_in_msg_content.getMaxWidth(), iv_in_msg_content.getMaxHeight())
                                        .into(iv_in_msg_content);

                                String uri = c;
                                iv_in_msg_content.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ImageViewerActivity.class)
                                        .putExtra("uris", new ArrayList<>(Collections.singletonList(Uri.fromFile(new File(uri)).toString())))));
                            } else {
                                iv_in_msg_content.setVisibility(View.GONE);
                                ll_voice_in_msg_content.setVisibility(View.VISIBLE);
                                tv_voice_duration.setText(t.split(";").length < 3 ? "0" : t.split(";")[2]);
                                v_in_msg_clicked.setVisibility(data.msg.localStatus == Message.LS_NEW ? View.VISIBLE : View.GONE);

                                String url = c;
                                if (isVoicePlayings.get(url) != null && isVoicePlayings.get(url)) {
                                    view_voice_anim.setBackgroundResource(R.drawable.anim_audio_recorder_play);
                                    AnimationDrawable animationDrawable = (AnimationDrawable) view_voice_anim.getBackground();
                                    animationDrawable.start();
                                } else {
                                    view_voice_anim.setBackgroundResource(R.mipmap.adj);
                                }

                                ll_voice_in_msg_content.setOnClickListener(v -> {
                                    SimpleAudioPlayHelper simpleAudioPlayHelper = SimpleAudioPlayHelper.getInstanceFor(url);
                                    simpleAudioPlayHelper.setListener(new SimpleAudioPlayHelper.SimpleListener() {

                                        @Override
                                        public void onIdle() {
                                            isVoicePlayings.put(url, false);
                                            mAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onPlay() {
                                            isVoicePlayings.put(url, true);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    simpleAudioPlayHelper.startPlayVoice();

                                    Message m = data.msg;
                                    m.localStatus = Message.LS_CLICKED;
                                    App.getInstance().getAppDatabase().messageDao().update(m);
                                });
                            }
                        } else {
                            tv_in_msg_content.setVisibility(View.VISIBLE);
                            iv_in_msg_content.setVisibility(View.GONE);
                            ll_voice_in_msg_content.setVisibility(View.GONE);
                            tv_in_msg_content.setText(data.msg.body);
                        }
                    }
                })
                .register(new SlimAdapter.SlimInjector<OutgoingMessage>() {

                    private Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                    private Gson gson = new Gson();
                    private Map<String, Boolean> isVoicePlayings = new HashMap<>();

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.chat_msg_out_item;
                    }

                    @Override
                    public void onBind(OutgoingMessage data, SlimAdapter.SlimViewHolder<OutgoingMessage> viewHolder) {
                        TextView tv_out_msg_send_time = viewHolder.findViewById(R.id.tv_out_msg_send_time);
                        ImageView iv_out_avatar = viewHolder.findViewById(R.id.iv_out_avatar);
                        TextView tv_out_msg_content = viewHolder.findViewById(R.id.tv_out_msg_content);
                        ImageView iv_out_msg_content = viewHolder.findViewById(R.id.iv_out_msg_content);
                        LinearLayout ll_voice_out_msg_content = viewHolder.findViewById(R.id.ll_voice_out_msg_content);
                        View view_voice_anim = viewHolder.findViewById(R.id.view_voice_anim);
                        TextView tv_voice_duration = viewHolder.findViewById(R.id.tv_voice_duration);
                        View v_out_msg_clicked = viewHolder.findViewById(R.id.v_out_msg_clicked);

                        int position = viewHolder.getAbsoluteAdapterPosition();
                        long lastMsgTime;
                        if (position < 0 || position + 2 > mAdapter.getData().size()) {
                            lastMsgTime = 0;
                        } else {
                            Object o = mAdapter.getData().get(position + 1);
                            if (o instanceof IncomingMessage) {
                                lastMsgTime = ((IncomingMessage) o).msg.createTime.getTime();
                            } else {
                                lastMsgTime = ((OutgoingMessage) o).msg.createTime.getTime();
                            }
                        }
                        long thisMsgTime = data.msg.createTime.getTime();
                        if (thisMsgTime - lastMsgTime > 4 * 60 * 1000) {
                            tv_out_msg_send_time.setVisibility(View.VISIBLE);
                            tv_out_msg_send_time.setText(DateUtils.convertDateToStr(data.msg.createTime, DateUtils.YYYY_MM_DD_HH_MM_SS));
                        } else {
                            tv_out_msg_send_time.setVisibility(View.GONE);
                        }

                        v_out_msg_clicked.setVisibility(View.GONE);
                        mVCardLoader.displayAvatar(JidCreate.entityBareFromOrThrowUnchecked(mOwnerJid), iv_out_avatar, placeholder);
                        if (TextUtils.isEmpty(data.msg.body)) {
                            tv_out_msg_content.setVisibility(View.GONE);
                            List<JsonExtraBodyItem> items = gson.fromJson(data.msg.otherBodies, new TypeToken<List<JsonExtraBodyItem>>() {
                            }.getType());
                            String t = "";
                            String c = "";
                            if (items != null && !items.isEmpty()) {
                                t = items.get(0).contentType;
                                c = items.get(0).content;
                            }
                            if (TextUtils.equals(t, "image/jpeg;filepath")) {
                                ll_voice_out_msg_content.setVisibility(View.GONE);
                                iv_out_msg_content.setVisibility(View.VISIBLE);
                                GlideApp.with(iv_out_msg_content)
                                        .load(c)
                                        .downsample(DownsampleStrategy.AT_MOST)
                                        .override(iv_out_msg_content.getMaxWidth(), iv_out_msg_content.getMaxHeight())
                                        .into(iv_out_msg_content);

                                String uri = c;
                                iv_out_msg_content.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ImageViewerActivity.class)
                                        .putExtra("uris", new ArrayList<>(Collections.singletonList(Uri.fromFile(new File(uri)).toString())))));
                            } else {
                                iv_out_msg_content.setVisibility(View.GONE);
                                ll_voice_out_msg_content.setVisibility(View.VISIBLE);
                                tv_voice_duration.setText(t.split(";").length < 3 ? "0" : t.split(";")[2]);
                                v_out_msg_clicked.setVisibility(data.msg.localStatus == Message.LS_NEW ? View.VISIBLE : View.GONE);

                                String url = c;
                                if (isVoicePlayings.get(url) != null && isVoicePlayings.get(url)) {
                                    view_voice_anim.setBackgroundResource(R.drawable.anim_audio_recorder_play);
                                    AnimationDrawable animationDrawable = (AnimationDrawable) view_voice_anim.getBackground();
                                    animationDrawable.start();
                                } else {
                                    view_voice_anim.setBackgroundResource(R.mipmap.adj);
                                }

                                ll_voice_out_msg_content.setOnClickListener(v -> {
                                    SimpleAudioPlayHelper simpleAudioPlayHelper = SimpleAudioPlayHelper.getInstanceFor(url);
                                    simpleAudioPlayHelper.setListener(new SimpleAudioPlayHelper.SimpleListener() {

                                        @Override
                                        public void onIdle() {
                                            isVoicePlayings.put(url, false);
                                            mAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onPlay() {
                                            isVoicePlayings.put(url, true);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    simpleAudioPlayHelper.startPlayVoice();

                                    Message m = data.msg;
                                    m.localStatus = Message.LS_CLICKED;
                                    App.getInstance().getAppDatabase().messageDao().update(m);
                                });
                            }
                        } else {
                            tv_out_msg_content.setVisibility(View.VISIBLE);
                            iv_out_msg_content.setVisibility(View.GONE);
                            ll_voice_out_msg_content.setVisibility(View.GONE);
                            tv_out_msg_content.setText(data.msg.body);
                        }
                    }
                });

        rv_chat_message.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rv_chat_message.setLayoutManager(layoutManager);
        rv_chat_message.setAdapter(mAdapter);

        mDisposableR = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof Boolean)
                .observeOn(Schedulers.io())
                .subscribe(aBoolean -> {
                    EntityBareJid jid = JidCreate.entityBareFromOrThrowUnchecked(mOppositeJid);
                    if (!TextUtils.isEmpty(mOppositeName)) {
                        VCard vCard = new VCard();
                        vCard.setNickName(mOppositeName);
                        mVCardLoader.putTempVCard(jid, vCard);
                    }
                    mVCardLoader.displayNickName(jid, tv_title);
                    setupContent();
                });
        mDisposableS = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof SmackClient.State)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    SmackClient.State state = (SmackClient.State) o;
                    tv_hold_voice.setEnabled(state == SmackClient.State.AUTHENTICATED);
                    btn_get_image.setEnabled(state == SmackClient.State.AUTHENTICATED);
                    btn_send.setEnabled(state == SmackClient.State.AUTHENTICATED);
                    tv_state_info.setText(new Spans(
                            ("(" + state.getMessage()
                                    .replace("idle", "连接中...")
                                    .replace("connected", "连接中...")
                                    .replace("connect failed", "连接失败")
                                    .replace("account created", "连接中...")
                                    .replace("account create failed", "连接失败")
                                    .replace("authenticated", "")
                                    .replace("authenticate failed", "连接失败")
                                    .replace("connection closed error", "离线")
                                    .replace("connection closed error conflict", "已下线")
                                    + ")")
                                    .replace("()", "")));
                });

        bindService(new Intent(this, SmackClientService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        SmackClientService.start(App.getInstance(), SharedPreferencesDataSource.getSignInResponse().user.id.toString(), "123456");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        App.getInstance().getAppDatabase().messageDao()
                .readMessages(
                        JidCreate.entityBareFromOrThrowUnchecked(mOwnerJid).asEntityBareJidString(),
                        JidCreate.entityBareFromOrThrowUnchecked(mOppositeJid).asEntityBareJidString());

        unbindService(mServiceConnection);
        if (mSmackClient != null) {
            mSmackClient.removeStateChangeListener(mStateChangeListener);
            mSmackClient = null;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposableR.dispose();
        mDisposableS.dispose();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nav_back:
                onBackPressed();
                break;
            case R.id.btn_switch_keyboard:
                mIsVoice = !mIsVoice;
                setupVoice();
                break;
            case R.id.btn_get_image:
                pickPhoto();
                break;
            case R.id.btn_send:
                if (mOppositeJid.contains("conference")) {

                } else {
                    String body = et_message.getEditableText().toString();
                    if (!TextUtils.isEmpty(body)) {
                        mSmackClient.sendMessage(mOppositeJid, et_message.getEditableText().toString());
                        et_message.setText("");
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            if (requestCode == 1000) {
                JsonExtraBodyItem item = new JsonExtraBodyItem();
                item.contentType = "image/jpeg;base64";
                item.content = FileUtils.fileToBase64(new File(Matisse.obtainPathResult(data).get(0)));
                mSmackClient.sendMessage(mOppositeJid, item);
            }
        }
    }

    private void setupContent() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = App.getInstance().getAppDatabase().messageDao()
                .getMessagesRx(
                        JidCreate.entityBareFromOrThrowUnchecked(mOwnerJid).asEntityBareJidString(),
                        JidCreate.entityBareFromOrThrowUnchecked(mOppositeJid).asEntityBareJidString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messages -> {
                            List<Object> list = new ArrayList<>();
                            for (Message m : messages) {
                                if (TextUtils.equals(m.fromEntityBareJid,
                                        JidCreate.entityBareFromOrThrowUnchecked(mOwnerJid).asEntityBareJidString())) {
                                    list.add(new OutgoingMessage(m));
                                } else {
                                    list.add(new IncomingMessage(m));
                                }
                            }
                            boolean smoothScroll = mAdapter.getData() != null && !mAdapter.getData().isEmpty();
                            mAdapter.updateData(list);
                            if (smoothScroll) {
                                int preFirstPosition = ((LinearLayoutManager) rv_chat_message.getLayoutManager()).findFirstVisibleItemPosition();
                                if (preFirstPosition < 5) {
                                    rv_chat_message.post(() -> rv_chat_message.smoothScrollToPosition(0));
                                }
                            } else {
                                rv_chat_message.post(() -> rv_chat_message.scrollToPosition(0));
                            }
                        },
                        throwable -> {
                        });
    }

    private void pickPhoto() {
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .capture(true)
                .captureStrategy(new CaptureStrategy(false, BuildConfig.FILE_PROVIDER_AUTHORITIES, "Matisse"))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(1000);
    }

    private void setupVoice() {
        if (mIsVoice) {
            btn_switch_keyboard.setImageResource(R.drawable.selector_chat_keyboard);
            tv_hold_voice.setVisibility(View.VISIBLE);
            et_message.setText("");
            et_message.setVisibility(View.GONE);
        } else {
            btn_switch_keyboard.setImageResource(R.drawable.selector_chat_voice);
            tv_hold_voice.setVisibility(View.GONE);
            et_message.setVisibility(View.VISIBLE);
        }
    }

    private static class IncomingMessage {
        private Message msg;

        private IncomingMessage(Message msg) {
            this.msg = msg;
        }
    }

    private static class OutgoingMessage {
        private Message msg;

        private OutgoingMessage(Message msg) {
            this.msg = msg;
        }
    }

}
