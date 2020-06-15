package com.org.firefighting.ui.common;

import android.content.Intent;
import android.os.Bundle;
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
import com.org.firefighting.BuildConfig;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.News;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NewsActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText("资讯");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, new NewsFragment(), "NewsFragment").commit();
    }

    public static class NewsFragment extends RecyclerFragment<News> {

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
        public RecyclerView.Adapter onCreateAdapter(List<News> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<News>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.news_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<News> viewHolder) {
                            viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), WebViewActivity.class)
                                    .putExtra("url", BuildConfig.API_URL_SB_4 + viewHolder.itemData.filePath + "/" + viewHolder.itemData.fileName)));
                        }

                        @Override
                        public void onBind(News data, SlimAdapter.SlimViewHolder<News> viewHolder) {
                            viewHolder
                                    .background(R.id.cl_news,
                                            viewHolder.getAbsoluteAdapterPosition() == 0 ? R.drawable.shape_card_top :
                                                    (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? R.drawable.shape_card_bottom :
                                                            R.drawable.shape_card_middle))
                                    .with(R.id.iv_news, view -> GlideApp.with(view)
                                            .load(BuildConfig.API_URL_SB_4 + data.thumbnail)
                                            .centerCrop().into((ImageView) view))
                                    .text(R.id.tv_news, data.title)
                                    .text(R.id.tv_news_from, data.sourceName)
                                    .text(R.id.tv_news_time, data.createDate)
                                    .text(R.id.tv_view_count, String.valueOf(0))
                                    .text(R.id.tv_comment_count, String.valueOf(0));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<News>> onCreatePageObservable(int currentPage) {
            return RestAPI.getInstance().apiServiceSB4()
                    .getNews(null, currentPage, 20)
                    .map(responseData -> {
                        PageResult<News> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<News> data) {
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
