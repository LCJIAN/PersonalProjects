package com.org.firefighting.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.Triple;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.chat.SmackClient;
import com.org.chat.SmackClientService;
import com.org.chat.VCardLoader;
import com.org.chat.data.db.entity.Message;
import com.org.chat.data.db.pojo.UnReadCount;
import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.SignInResponse;
import com.org.firefighting.data.network.entity.SystemMessage;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.chat.ChatActivity;
import com.org.firefighting.ui.common.SystemMessagesActivity;
import com.org.firefighting.util.Utils;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class ConversationsFragment extends BaseFragment {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.srl_conversation)
    SwipeRefreshLayout srl_conversation;
    @BindView(R.id.rv_conversation)
    RecyclerView rv_conversation;

    private Unbinder mUnBinder;

    private View mEmptyView;
    private EmptyAdapter mEmptyAdapter;
    private SlimAdapter mAdapter;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        srl_conversation.setColorSchemeResources(R.color.colorPrimary);
        srl_conversation.setOnRefreshListener(() -> RxBus.getInstance().send(Boolean.TRUE));

        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<SystemMessage>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.conversation_item_system_message;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<SystemMessage> viewHolder) {
                        viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), SystemMessagesActivity.class)));
                    }

                    @Override
                    public void onBind(SystemMessage data, SlimAdapter.SlimViewHolder<SystemMessage> viewHolder) {
                        View cl_conversation_item = viewHolder.findViewById(R.id.cl_conversation_item);
                        if (mAdapter.getData().size() == 1) {
                            cl_conversation_item.setBackgroundResource(R.drawable.shape_card);
                        } else {
                            if (viewHolder.getAbsoluteAdapterPosition() == 0) {
                                cl_conversation_item.setBackgroundResource(R.drawable.shape_card_top);
                            } else if (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1) {
                                cl_conversation_item.setBackgroundResource(R.drawable.shape_card_bottom);
                            } else {
                                cl_conversation_item.setBackgroundResource(R.drawable.shape_card_middle);
                            }
                        }
                        viewHolder
                                .visibility(R.id.v_divider, viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1
                                        ? View.INVISIBLE
                                        : View.VISIBLE)
                                .text(R.id.tv_content, data.content)
                                .text(R.id.tv_time, data.createTime);
                    }
                })
                .register(new SlimAdapter.SlimInjector<Conversation>() {

                    private Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                    private Map<SlimAdapter.SlimViewHolder<Conversation>, Badge> badges = new HashMap<>();

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.conversation_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<Conversation> viewHolder) {
                        View iv_avatar = viewHolder.findViewById(R.id.iv_avatar);
                        Badge badge = new QBadgeView(iv_avatar.getContext()).bindTarget(iv_avatar);
                        badges.put(viewHolder, badge);

                        viewHolder.clicked(v -> {
                            EntityJid oppositeJid;
                            EntityJid from = JidCreate.entityFromOrThrowUnchecked(viewHolder.itemData.message.fromEntityBareJid);
                            EntityJid to = JidCreate.entityFromOrThrowUnchecked(viewHolder.itemData.message.toEntityBareJid);
                            EntityJid user = mSmackClient.getUser();
                            if (to.isParentOf(user)) {
                                oppositeJid = from;
                            } else {
                                oppositeJid = to;
                            }
                            v.getContext().startActivity(new Intent(v.getContext(), ChatActivity.class)
                                    .putExtra("owner_jid", user.toString())
                                    .putExtra("opposite_jid", oppositeJid.toString()));
                        });
                    }

                    @Override
                    public void onBind(Conversation data, SlimAdapter.SlimViewHolder<Conversation> viewHolder) {
                        EntityJid oppositeJid;
                        EntityJid from = JidCreate.entityFromOrThrowUnchecked(data.message.fromEntityBareJid);
                        EntityJid to = JidCreate.entityFromOrThrowUnchecked(data.message.toEntityBareJid);
                        EntityJid user = mSmackClient.getUser();
                        if (to.isParentOf(user)) {
                            oppositeJid = from;
                        } else {
                            oppositeJid = to;
                        }

                        View cl_conversation_item = viewHolder.findViewById(R.id.cl_conversation_item);
                        if (mAdapter.getData().size() == 1) {
                            cl_conversation_item.setBackgroundResource(R.drawable.shape_card);
                        } else {
                            if (viewHolder.getAbsoluteAdapterPosition() == 0) {
                                cl_conversation_item.setBackgroundResource(R.drawable.shape_card_top);
                            } else if (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1) {
                                cl_conversation_item.setBackgroundResource(R.drawable.shape_card_bottom);
                            } else {
                                cl_conversation_item.setBackgroundResource(R.drawable.shape_card_middle);
                            }
                        }
                        viewHolder
                                .visibility(R.id.v_divider, viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1
                                        ? View.INVISIBLE
                                        : View.VISIBLE)
                                .with(R.id.tv_content, view1 -> {
                                    String text;
                                    if (TextUtils.isEmpty(data.message.body)) {
                                        if (data.message.otherBodies.contains("image")) {
                                            text = "[图片]";
                                        } else {
                                            text = "[语音]";
                                        }
                                    } else {
                                        text = data.message.body;
                                    }
                                    ((TextView) view1).setText(text);
                                })
                                .text(R.id.tv_time, DateUtils.convertDateToStr(data.message.createTime))
                                .with(R.id.tv_user_name, v -> mVCardLoader.displayNickName(oppositeJid.asEntityBareJid(), (TextView) v))
                                .with(R.id.iv_avatar, v -> {
                                    mVCardLoader.displayAvatar(oppositeJid.asEntityBareJid(), (ImageView) v, placeholder);
                                    badges.get(viewHolder).setBadgeNumber(data.unReadCount == null ? 0 : data.unReadCount.unReadCount);
                                });
                    }
                });

        rv_conversation.setHasFixedSize(true);
        rv_conversation.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_data, rv_conversation, false);
        mEmptyAdapter = new EmptyAdapter(mAdapter).setEmptyView(mEmptyView);
        mEmptyAdapter.hideEmptyView();
        rv_conversation.setAdapter(mEmptyAdapter);

        mDisposableR = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof Boolean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> setupContent());
        mDisposableS = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof SmackClient.State)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    SmackClient.State state = (SmackClient.State) o;
                    if (state == SmackClient.State.AUTHENTICATED) { // 连接成功
                        initNickNameAvatar();
                    }
                    tv_title.setText(new Spans().append(getString(R.string.action_conversation))
                            .append(("(" + state.getMessage()
                                            .replace("idle", "连接中...")
                                            .replace("connected", "连接中...")
                                            .replace("connect failed", "连接失败")
                                            .replace("account created", "连接中...")
                                            .replace("account create failed", "连接失败")
                                            .replace("authenticated", "")
                                            .replace("authenticate failed", "连接失败")
                                            .replace("connection closed error", "离线")
                                            .replace("connection closed error conflict", "已下线")
                                            + ")").replace("()", ""),
                                    new AbsoluteSizeSpan(DimenUtils.spToPixels(14, tv_title.getContext()))));
                });

        view.getContext().bindService(new Intent(view.getContext(), SmackClientService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        SignInResponse signInResponse = SharedPreferencesDataSource.getSignInResponse();
        SmackClientService.start(App.getInstance(),
                signInResponse.setting.get("IMserverIP"),
                signInResponse.setting.get("IMservername"),
                signInResponse.user.id.toString(),
                "123456");
    }

    @Override
    public void onDestroyView() {
        if (getContext() != null) {
            getContext().unbindService(mServiceConnection);
        }
        if (mSmackClient != null) {
            mSmackClient.removeStateChangeListener(mStateChangeListener);
            mSmackClient = null;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposableR.dispose();
        mDisposableS.dispose();
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private void setupContent() {
        srl_conversation.post(() -> srl_conversation.setRefreshing(true));
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = Flowable
                .combineLatest(
                        Flowable.just(mSmackClient.getUser()),
                        App.getInstance().getAppDatabase().messageDao()
                                .getConversationsRx(mSmackClient.getUser().asEntityBareJidString()),
                        RestAPI.getInstance().apiService().getSystemMessages(SharedPreferencesDataSource.getSignInResponse().user.id,
                                null, 0, 1, 1).toFlowable(),
                        Triple::create)
                .publish(tripleFlow -> tripleFlow
                        .zipWith(
                                tripleFlow
                                        .map(pair -> {
                                            assert pair.first != null;
                                            assert pair.second != null;
                                            EntityJid user = pair.first;
                                            List<Message> messages = pair.second;

                                            String userEntityBareJid = user.asEntityBareJidString();
                                            List<String> bareOppositeJidList = new ArrayList<>();
                                            for (Message m : messages) {
                                                if (TextUtils.equals(m.toEntityBareJid, userEntityBareJid)) {
                                                    bareOppositeJidList.add(m.fromEntityBareJid);
                                                } else {
                                                    bareOppositeJidList.add(m.toEntityBareJid);
                                                }
                                            }
                                            return App.getInstance().getAppDatabase().messageDao()
                                                    .getUnReadMessageCounts(userEntityBareJid, bareOppositeJidList);
                                        }),
                                (triple, unReadCounts) -> {
                                    assert triple.second != null;
                                    assert triple.third != null;
                                    SystemMessage systemMessage = triple.third.result.isEmpty() ? null : triple.third.result.get(0);
                                    List<Message> messages = triple.second;
                                    List<Conversation> conversations = new ArrayList<>();
                                    TotalUnReadCount totalUnReadCount = new TotalUnReadCount(0);
                                    for (Message m : messages) {
                                        UnReadCount u = null;
                                        for (UnReadCount un : unReadCounts) {
                                            if (TextUtils.equals(m.fromEntityBareJid, un.fromEntityBareJid)
                                                    || TextUtils.equals(m.toEntityBareJid, un.fromEntityBareJid)) {
                                                u = un;
                                                totalUnReadCount.number += u.unReadCount;
                                                break;
                                            }
                                        }
                                        Conversation c = new Conversation(m, u);
                                        conversations.add(c);
                                    }

                                    List<Object> objects = new ArrayList<>();
                                    if (systemMessage != null) {
                                        objects.add(systemMessage);
                                    }
                                    objects.addAll(conversations);

                                    return Pair.create(totalUnReadCount, objects);
                                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            srl_conversation.post(() -> srl_conversation.setRefreshing(false));
                            RxBus.getInstance().send(pair.first);
                            mAdapter.updateData(pair.second);
                            ((ImageView) mEmptyView).setImageResource(R.drawable.no_message);
                            mEmptyAdapter.showEmptyView();
                        },
                        throwable -> {
                            srl_conversation.post(() -> srl_conversation.setRefreshing(false));
                            mAdapter.updateData(Collections.emptyList());
                            ((ImageView) mEmptyView).setImageResource(R.drawable.net_error);
                            mEmptyAdapter.showEmptyView();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void initNickNameAvatar() {
        new Thread(() -> {
            VCard vCard = mSmackClient.getVCard(null);
            if (vCard != null) {
                boolean save = false;
                if (TextUtils.isEmpty(vCard.getNickName())) {
                    vCard.setNickName(SharedPreferencesDataSource.getSignInResponse().user.realName);
                    save = true;
                }
                if (vCard.getAvatar() == null) {
                    vCard.setAvatar(Utils.getBytes("http://124.162.30.39:9528/admin-ht/"
                            + SharedPreferencesDataSource.getSignInResponse().user.avatar, null, null));
                    save = true;
                }
                if (save) {
                    mSmackClient.saveVCard(vCard);
                }
            }
        }).start();
    }

    private static class Conversation {
        private Message message;
        private UnReadCount unReadCount;

        private Conversation(Message message, UnReadCount unReadCount) {
            this.message = message;
            this.unReadCount = unReadCount;
        }
    }

    public static class TotalUnReadCount {
        public int number;

        private TotalUnReadCount(int number) {
            this.number = number;
        }
    }
}
