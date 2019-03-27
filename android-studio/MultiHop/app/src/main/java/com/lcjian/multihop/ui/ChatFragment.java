package com.lcjian.multihop.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lcjian.multihop.R;
import com.lcjian.multihop.lib.Manager;
import com.lcjian.multihop.lib.Role;
import com.lcjian.multihop.lib.receive.HttpServer;
import com.lcjian.multihop.lib.send.PostTextMessageTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ChatFragment extends Fragment {

    @BindView(R.id.rv_message)
    RecyclerView rv_message;
    @BindView(R.id.et_message)
    EditText et_message;
    @BindView(R.id.btn_send)
    Button btn_send;

    private Unbinder mUnBinder;

    private Manager mManager;

    private HttpServer.OnReceivedListener mOnReceivedListener;

    private List<Object> mObjects;
    private MessageAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mOnReceivedListener = new HttpServer.OnReceivedListener() {
            @Override
            public void onTextMessageReceived(String text) {
                mObjects.add(text);
                rv_message.post(() -> mAdapter.notifyDataSetChanged());
            }

            @Override
            public void onAudioMessageReceived(File audio) {
                mObjects.add(audio);
                rv_message.post(() -> mAdapter.notifyDataSetChanged());
            }
        };

        mManager = Manager.getInstance();
        if (mManager.getRole() != Role.SENDER) {
            mManager.getHttpServer().addOnReceivedListener(mOnReceivedListener);
        }

        mObjects = new ArrayList<>();
        mAdapter = new MessageAdapter(mObjects);
        rv_message.setHasFixedSize(true);
        rv_message.setLayoutManager(new LinearLayoutManager(rv_message.getContext()));
        rv_message.setAdapter(mAdapter);

        btn_send.setOnClickListener(v -> {
            String text = et_message.getEditableText().toString();
            if (!TextUtils.isEmpty(text)) {
                mManager.getTaskRunner().addTask(new PostTextMessageTask(text));
                mObjects.add(text);
                mAdapter.notifyDataSetChanged();

                et_message.setText("");
            }
        });
        et_message.setEnabled(mManager.getRole() == Role.SENDER);
        btn_send.setEnabled(mManager.getRole() == Role.SENDER);
    }

    @Override
    public void onDestroyView() {
        if (mManager.getRole() != Role.SENDER) {
            mManager.getHttpServer().removeOnReceivedListener(mOnReceivedListener);
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    public static class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_MESSAGE_TEXT = 0;
        private static final int TYPE_MESSAGE_AUDIO = 1;

        private List<Object> mData;

        MessageAdapter(List<Object> data) {
            this.mData = data;
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mData.get(position) instanceof String) {
                return TYPE_MESSAGE_TEXT;
            } else {
                return TYPE_MESSAGE_AUDIO;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_MESSAGE_TEXT:
                    return new MessageTextViewHolder(parent);
                case TYPE_MESSAGE_AUDIO:
                    return new MessageAudioViewHolder(parent);
                default:
                    return new MessageTextViewHolder(parent);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MessageTextViewHolder) {
                ((MessageTextViewHolder) holder).bindTo((String) mData.get(position));
            } else if (holder instanceof MessageAudioViewHolder) {
                ((MessageAudioViewHolder) holder).bindTo((File) mData.get(position));
            }
        }

        static class MessageTextViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_message_text)
            TextView tv_message_text;

            String itemData;

            MessageTextViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_text_item, parent, false));
                ButterKnife.bind(this, this.itemView);
            }

            void bindTo(String s) {
                this.itemData = s;
                tv_message_text.setText(itemData);
            }
        }

        static class MessageAudioViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_message_audio)
            TextView tv_message_audio;

            File itemData;

            MessageAudioViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_audio_item, parent, false));
                ButterKnife.bind(this, this.itemView);
            }

            void bindTo(File f) {
                this.itemData = f;
                tv_message_audio.setText(itemData.getName());
            }
        }
    }
}
