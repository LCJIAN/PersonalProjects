package com.org.firefighting.ui.resource;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

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
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ResourcesActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, new ResourcesFragment(), "ResourcesFragment").commit();
    }

    public static class ResourcesFragment extends RecyclerFragment<ResourceEntity> {

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
        public RecyclerView.Adapter onCreateAdapter(List<ResourceEntity> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<ResourceEntity>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.resource_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<ResourceEntity> viewHolder) {
                            viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), ResourceDetailActivity.class)
                                    .putExtra("resource_id", viewHolder.itemData.id)
                                    .putExtra("resource_table_comment", viewHolder.itemData.tableComment)));
                        }

                        @Override
                        public void onBind(ResourceEntity data, SlimAdapter.SlimViewHolder<ResourceEntity> viewHolder) {
                            viewHolder.text(R.id.tv_identity, "标识符：" + data.shareXxzybh)
                                    .text(R.id.tv_address_info, data.shareXxzymc)
                                    .text(R.id.tv_police_category, "所属业务警种：" + data.shareXxzyflSsywjz)
                                    .text(R.id.tv_factor, "所属要素：" + data.shareXxzyflSsys)
                                    .text(R.id.tv_content, data.tableComment)
                                    .text(R.id.tv_supplier, "提供单位：" + data.unitName)
                                    .text(R.id.tv_count_1, new Spans("浏览：").append(String.valueOf(data.browses), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_2, new Spans("调用：").append(String.valueOf(data.calls), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_3, new Spans("下载：").append(String.valueOf(data.download), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_4, new Spans("调阅：").append(String.valueOf(data.apply), new ForegroundColorSpan(Color.RED)));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<ResourceEntity>> onCreatePageObservable(int currentPage) {
            return RestAPI.getInstance().apiServiceSB2()
                    .getResources(null, SharedPreferencesDataSource.getSignInResponse().user.id,
                            null, null, currentPage, 20)
                    .map(responseData -> {
                        PageResult<ResourceEntity> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<ResourceEntity> data) {
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
