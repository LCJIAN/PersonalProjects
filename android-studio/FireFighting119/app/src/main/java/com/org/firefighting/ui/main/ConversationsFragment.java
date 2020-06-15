package com.org.firefighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Conversation;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.chat.DepartmentsActivity;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ConversationsFragment extends BaseFragment {

    @BindView(R.id.btn_go_contact)
    ImageButton btn_go_contact;
    @BindView(R.id.srl_conversation)
    SwipeRefreshLayout srl_conversation;
    @BindView(R.id.rv_conversation)
    RecyclerView rv_conversation;

    private Unbinder mUnBinder;

    private View mEmptyView;
    private EmptyAdapter mEmptyAdapter;
    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_go_contact.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), DepartmentsActivity.class));
        });
        srl_conversation.setColorSchemeResources(R.color.colorPrimary);
        srl_conversation.setOnRefreshListener(this::setupContent);

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Conversation>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.conversation_item;
            }

            @Override
            public void onBind(Conversation data, SlimAdapter.SlimViewHolder<Conversation> viewHolder) {
                viewHolder
                        .background(R.id.cl_conversation_item,
                                viewHolder.getAbsoluteAdapterPosition() == 0 ? R.drawable.shape_card_top :
                                        (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? R.drawable.shape_card_bottom :
                                                R.drawable.shape_card_middle))
                        .visibility(R.id.v_divider, viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? View.INVISIBLE : View.VISIBLE)
                        .text(R.id.tv_content, data.content)
                        .text(R.id.tv_time, data.createTime)
                        .text(R.id.tv_user_name, data.senderRealName);
            }
        });

        rv_conversation.setHasFixedSize(true);
        rv_conversation.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_data, rv_conversation, false);
        mEmptyAdapter = new EmptyAdapter(mAdapter).setEmptyView(mEmptyView);
        mEmptyAdapter.hideEmptyView();
        rv_conversation.setAdapter(mEmptyAdapter);

        setupContent();
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private void setupContent() {
        srl_conversation.post(() -> srl_conversation.setRefreshing(true));
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().apiService()
                .getConversations(SharedPreferencesDataSource.getSignInResponse().user.username,
                        null, 0, 1, 5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(conversationPageResponse -> {
                            srl_conversation.post(() -> srl_conversation.setRefreshing(false));
                            mAdapter.updateData(conversationPageResponse.result);
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
}
