package com.org.firefighting.ui.service;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import com.lcjian.lib.text.Spans;
import com.org.firefighting.R;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ServiceListActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        ButterKnife.bind(this);

        tv_title.setText(R.string.announcement);
        btn_back.setOnClickListener(v -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, new ServiceListFragment(), "ServiceListFragment").commit();
    }

    public static class ServiceListFragment extends RecyclerFragment<ServiceEntity> {

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
        public RecyclerView.Adapter onCreateAdapter(List<ServiceEntity> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<ServiceEntity>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.service_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<ServiceEntity> viewHolder) {
                        }

                        @Override
                        public void onBind(ServiceEntity data, SlimAdapter.SlimViewHolder<ServiceEntity> viewHolder) {
                            viewHolder
                                    .text(R.id.tv_interface_type, "接口类型：" + (TextUtils.isEmpty(data.type) ? "未知" : data.type))
                                    .text(R.id.tv_update_time, "更新时间：" + (TextUtils.isEmpty(data.createDate) ? "未知" : data.createDate))
                                    .text(R.id.tv_service_name, data.name)
                                    .text(R.id.tv_service_des, data.remarks)
                                    .text(R.id.tv_count_1, new Spans("浏览：").append(String.valueOf(data.browses), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_2, new Spans("调用：").append(String.valueOf(data.calls), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_3, new Spans("连接：").append(String.valueOf(data.applyFrequency), new ForegroundColorSpan(Color.RED)));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<ServiceEntity>> onCreatePageObservable(int currentPage) {
            return RestAPI.getInstance().apiServiceSB3()
                    .getServices(null, null, SharedPreferencesDataSource.getSignInResponse().user.id,
                            null, currentPage, 20)
                    .map(responseData -> {
                        PageResult<ServiceEntity> pageResult = new PageResult<>();
                        pageResult.elements = responseData.data.result;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = responseData.data.total % 20 == 0
                                ? responseData.data.total / 20
                                : responseData.data.total / 20 + 1;
                        pageResult.total_elements = responseData.data.total;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<ServiceEntity> data) {
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
