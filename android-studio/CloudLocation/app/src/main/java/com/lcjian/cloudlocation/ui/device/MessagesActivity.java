package com.lcjian.cloudlocation.ui.device;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.entity.PageResult;
import com.lcjian.cloudlocation.data.network.entity.Messages;
import com.lcjian.cloudlocation.data.network.entity.SignInInfo;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.RecyclerFragment;
import com.lcjian.cloudlocation.ui.base.SlimAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
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
        setContentView(R.layout.activity_recycler_fragment);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.message_center));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, new MessagesFragment())
                .commit();
    }

    public static class MessagesFragment extends RecyclerFragment<Messages.Message> {

        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Messages.Message> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Messages.Message>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.message_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Messages.Message> viewHolder) {
                            viewHolder.longClicked(v -> {
                                showDeleteDialog(v.getContext(), viewHolder.itemData);
                                return true;
                            });
                        }

                        @Override
                        public void onBind(Messages.Message data, SlimAdapter.SlimViewHolder<Messages.Message> viewHolder) {
                            viewHolder.text(R.id.tv_device_name, data.name)
                                    .text(R.id.tv_device_message, data.warn)
                                    .text(R.id.tv_message_time, data.createDate);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Messages.Message>> onCreatePageObservable(int currentPage) {
            SignInInfo signInInfo = getSignInInfo();
            Long id;
            Integer typeId;
            if (signInInfo.userInfo == null) {
                id = Long.parseLong(signInInfo.deviceInfo.deviceID);
                typeId = 1;
            } else {
                id = Long.parseLong(signInInfo.userInfo.userID);
                typeId = 0;
            }
            return mRestAPI.cloudService().getMessages(id,
                    typeId, currentPage, 20)
                    .map(messages -> {
                        PageResult<Messages.Message> pageResult = new PageResult<>();
                        if (messages.arr == null) {
                            messages.arr = new ArrayList<>();
                        }
                        pageResult.elements = messages.arr;
                        pageResult.page_number = messages.nowPage == null ? 0 : Integer.parseInt(messages.nowPage);
                        pageResult.page_size = 20;
                        pageResult.total_pages = pageResult.elements.size() < 20 ? currentPage : Integer.MAX_VALUE;
                        pageResult.total_elements = messages.resSize == null ? 0 : Integer.parseInt(messages.resSize);
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<Messages.Message> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recycler_view.addItemDecoration(new HorizontalDividerItemDecoration.Builder(view.getContext())
                    .size(1)
                    .build());
            super.onViewCreated(view, savedInstanceState);
            mDisposables = new CompositeDisposable();
        }

        @Override
        public void onDestroyView() {
            mDisposables.dispose();
            super.onDestroyView();
        }

        private void showDeleteDialog(Context context, Messages.Message data) {
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
        private void deleteMessage(Messages.Message data) {
            SignInInfo signInInfo = getSignInInfo();
            Long id;
            Integer typeId;
            if (signInInfo.userInfo == null) {
                id = Long.parseLong(signInInfo.deviceInfo.deviceID);
                typeId = 1;
            } else {
                typeId = 0;
                id = Long.parseLong(signInInfo.userInfo.userID);
            }
            mDisposables.add(mRestAPI.cloudService()
                    .deleteMessage(id, typeId, data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(state -> {
                        if (TextUtils.equals("0", state.state)) {
                            Toast.makeText(App.getInstance(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
                        } else {
                            List<Messages.Message> list = new ArrayList<>((List<Messages.Message>) mAdapter.getData());
                            list.remove(data);
                            mAdapter.updateData(list);
                        }
                    }, throwable -> {
                    }));
        }
    }
}
