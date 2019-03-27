package com.lcjian.mmt.ui.logistics;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Message;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.BaseDialogFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.util.DateUtils;
import com.lcjian.mmt.util.DimenUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MessagesActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.message));
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        if (getSupportFragmentManager().findFragmentByTag("MessagesFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_fragment_container, new MessagesFragment(), "MessagesFragment").commit();
        }
    }

    public static class MessagesFragment extends RecyclerFragment<Message> {

        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Message> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Message>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.message_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Message> viewHolder) {
                            viewHolder
                                    .clicked(R.id.cl_message, v -> {
                                        viewHolder.itemData.msgStatus = 2;
                                        mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                        MessageDialogFragment.newInstance(viewHolder.itemData)
                                                .show(getChildFragmentManager(), "MessageDialogFragment");
                                    })
                                    .clicked(R.id.tv_delete_m, v -> showDeleteDialog(v.getContext(), viewHolder.itemData));
                        }

                        @Override
                        public void onBind(Message data, SlimAdapter.SlimViewHolder<Message> viewHolder) {
                            viewHolder.visibility(R.id.v_dot_unread, data.msgStatus == 1 ? View.VISIBLE : View.GONE)
                                    .text(R.id.tv_message_time, DateUtils.convertDateToStr(new Date(data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS))
                                    .text(R.id.tv_message_content, data.content);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Message>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getMessages(null, (currentPage - 1) * 20, 20)
                    .map(quoteResponsePageData -> {
                        PageResult<Message> pageResult = new PageResult<>();
                        if (quoteResponsePageData.elements == null) {
                            quoteResponsePageData.elements = new ArrayList<>();
                        }
                        pageResult.elements = quoteResponsePageData.elements;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = quoteResponsePageData.total_elements % 20 == 0
                                ? quoteResponsePageData.total_elements / 20
                                : quoteResponsePageData.total_elements / 20 + 1;
                        pageResult.total_elements = quoteResponsePageData.total_elements;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<Message> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recycler_view.addItemDecoration(new HorizontalDividerItemDecoration.Builder(view.getContext())
                    .size(DimenUtils.spToPixels(8, view.getContext()))
                    .build());
            super.onViewCreated(view, savedInstanceState);
            mDisposables = new CompositeDisposable();
        }

        private void showDeleteDialog(Context context, Message data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        deleteMessage(data);
                    })
                    .create().show();
        }

        @SuppressWarnings("unchecked")
        private void deleteMessage(Message data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .deleteMessages(data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    List<Message> list = new ArrayList<>((List<Message>) mAdapter.getData());
                                    list.remove(data);
                                    mAdapter.updateData(list);
                                } else {
                                    Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }
    }

    public static class MessageDialogFragment extends BaseDialogFragment {

        @BindView(R.id.tv_message_detail)
        TextView tv_message_detail;
        @BindView(R.id.btn_confirm)
        Button btn_confirm;
        Unbinder unbinder;

        private Message mMessage;

        public static MessageDialogFragment newInstance(Message message) {
            MessageDialogFragment fragment = new MessageDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable("message", message);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mMessage = (Message) getArguments().getSerializable("message");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_message_dialog, container, false);
            unbinder = ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            tv_message_detail.setText(mMessage.content);
            btn_confirm.setOnClickListener(v -> dismiss());

            mRestAPI.cloudService().viewMessage(mMessage.id)
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }
}

