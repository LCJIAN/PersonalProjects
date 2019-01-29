package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Record;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.util.DimenUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RecordsActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    private Integer mRecordType; // 0:收支;1:充值;2:提现;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        mRecordType = getIntent().getIntExtra("record_type", 0);
        tv_title.setText(mRecordType == 0 ? "收支记录"
                : (mRecordType == 1 ? "充值记录"
                : "提现记录"));
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        if (getSupportFragmentManager().findFragmentByTag("RecordsFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_fragment_container, RecordsFragment.newInstance(mRecordType), "RecordsFragment").commit();
        }
    }

    public static class RecordsFragment extends RecyclerFragment<Record> {

        private SlimAdapter mAdapter;

        private Integer mType;

        public static RecordsFragment newInstance(Integer recordType) {
            RecordsFragment fragment = new RecordsFragment();
            Bundle args = new Bundle();
            args.putInt("record_type", recordType);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mType = getArguments().getInt("record_type", 0);
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Record> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Record>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.record_item;
                        }

                        @Override
                        public void onBind(Record data, SlimAdapter.SlimViewHolder<Record> viewHolder) {
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Record>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getRecords((currentPage - 1) * 20, 20)
                    .map(quoteResponsePageData -> {
                        PageResult<Record> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<Record> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recycler_view.addItemDecoration(new HorizontalDividerItemDecoration.Builder(view.getContext())
                    .size(DimenUtils.spToPixels(12, view.getContext()))
                    .build());
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
