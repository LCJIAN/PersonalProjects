package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Record;
import com.lcjian.mmt.ui.base.BaseActivity;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        // 0:收支;1:充值;2:保证金充值记录;3:提现;
        int recordType = getIntent().getIntExtra("record_type", 0);
        tv_title.setText(recordType == 0 ? R.string.in_out_records
                : (recordType == 1 ? R.string.recharge_records
                : (recordType == 2 ? R.string.bond_recharge_records : R.string.withdraw_records)));
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        if (getSupportFragmentManager().findFragmentByTag("RecordsFragment") == null) {
            if (recordType == 0) { // 收支记录
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fl_fragment_container, new InOutRecordsFragment(), "RecordsFragment").commit();
            } else if (recordType == 1) { // 充值记录
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fl_fragment_container, new RechargeRecordsFragment(), "RecordsFragment").commit();
            } else if (recordType == 2) { // 保证金充值记录
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fl_fragment_container, new BondRechargeRecordsFragment(), "RecordsFragment").commit();
            } else { // 提现记录
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fl_fragment_container, new WithdrawRecordsFragment(), "RecordsFragment").commit();
            }
        }
    }

    public static class WithdrawRecordsFragment extends RecyclerFragment<Record> {

        private SlimAdapter mAdapter;

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
                            viewHolder.text(R.id.tv_order_no, data.doType)
                                    .text(R.id.tv_time, DateUtils.convertDateToStr(new Date(data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS))
                                    .text(R.id.tv_amount, viewHolder.itemView.getContext().getString(R.string.currency_sign) + data.amount / 100);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Record>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getWithdrawRecords((currentPage - 1) * 20, 20)
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

    public static class RechargeRecordsFragment extends RecyclerFragment<Record> {

        private SlimAdapter mAdapter;

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
                        public void onInit(SlimAdapter.SlimViewHolder<Record> viewHolder) {
                            viewHolder.textColor(R.id.tv_amount, ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorTextBlack));
                        }

                        @Override
                        public void onBind(Record data, SlimAdapter.SlimViewHolder<Record> viewHolder) {
                            viewHolder.text(R.id.tv_order_no, data.doType)
                                    .text(R.id.tv_time, DateUtils.convertDateToStr(new Date(data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS))
                                    .text(R.id.tv_amount, "+ " + data.amount / 100 + viewHolder.itemView.getContext().getString(R.string.currency));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Record>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getRechargeRecords((currentPage - 1) * 20, 20)
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

    public static class InOutRecordsFragment extends RecyclerFragment<Record> {

        private SlimAdapter mAdapter;

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
                        public void onInit(SlimAdapter.SlimViewHolder<Record> viewHolder) {
                            viewHolder.textColor(R.id.tv_amount, ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorTextBlack));
                        }

                        @Override
                        public void onBind(Record data, SlimAdapter.SlimViewHolder<Record> viewHolder) {
                            viewHolder.text(R.id.tv_order_no, data.doType)
                                    .text(R.id.tv_time, DateUtils.convertDateToStr(new Date(data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS))
                                    .text(R.id.tv_amount, data.amount / 100 + viewHolder.itemView.getContext().getString(R.string.currency));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Record>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getInOutRecords((currentPage - 1) * 20, 20)
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

    public static class BondRechargeRecordsFragment extends RecyclerFragment<Record> {

        private SlimAdapter mAdapter;

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
                        public void onInit(SlimAdapter.SlimViewHolder<Record> viewHolder) {
                            viewHolder.textColor(R.id.tv_amount, ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorTextBlack));
                        }

                        @Override
                        public void onBind(Record data, SlimAdapter.SlimViewHolder<Record> viewHolder) {
                            viewHolder.text(R.id.tv_order_no, data.doType)
                                    .text(R.id.tv_time, DateUtils.convertDateToStr(new Date(data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS))
                                    .text(R.id.tv_amount, data.amount / 100 + viewHolder.itemView.getContext().getString(R.string.currency));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Record>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getBondRechargeRecords((currentPage - 1) * 20, 20)
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
