package com.org.firefighting.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.R;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.SystemMessage;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;
import com.org.firefighting.ui.task.TaskDetailActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SystemMessagesActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText("系统信息");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, new SystemMessagesFragment(), "SystemMessagesFragment").commit();
    }

    public static class SystemMessagesFragment extends RecyclerFragment<SystemMessage> {

        private View mEmptyView;
        private SlimAdapter mAdapter;

        @Override
        protected void onEmptyAdapterCreated(EmptyAdapter emptyAdapter) {
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_data, recycler_view, false);
            emptyAdapter.setEmptyView(mEmptyView);
        }

        @Override
        protected void onEmptyViewShow(boolean error) {
            ((ImageView) mEmptyView).setImageResource(error ? R.drawable.net_error : R.drawable.no_search_result);
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<SystemMessage> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<SystemMessage>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.system_message_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<SystemMessage> viewHolder) {
                            viewHolder.clicked(v -> {
                                if (TextUtils.equals("1200", viewHolder.itemData.msgClassify)) {
                                    startActivity(new Intent(v.getContext(), TaskDetailActivity.class)
                                            .putExtra("task_id", viewHolder.itemData.busiId));
                                }
                            });
                        }

                        @Override
                        public void onBind(SystemMessage data, SlimAdapter.SlimViewHolder<SystemMessage> viewHolder) {
                            viewHolder
                                    .text(R.id.tv_title, (TextUtils.equals("1200", data.msgClassify) ? "【信息填报】" : "") +
                                            "(" + data.subClassify + ")")
                                    .text(R.id.tv_content, data.content)
                                    .text(R.id.tv_time, data.createTime);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<SystemMessage>> onCreatePageObservable(int currentPage) {
            return RestAPI.getInstance().apiService()
                    .getSystemMessages(SharedPreferencesDataSource.getSignInResponse().user.id,
                            null, 0, currentPage, 20)
                    .map(responseData -> {
                        PageResult<SystemMessage> pageResult = new PageResult<>();
                        pageResult.elements = responseData.result;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = responseData.total % 20 == 0
                                ? responseData.total / 20
                                : responseData.total / 20 + 1;
                        pageResult.total_elements = responseData.total;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<SystemMessage> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
