package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Invoice;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SimpleFragmentPagerAdapter;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InvoiceManageActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.vp)
    ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_manage);
        ButterKnife.bind(this);

        tv_title.setText(R.string.invoice_manage);
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        vp.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager())
                .addFragment(InvoicesFragment.newInstance(0), "未开票")
                .addFragment(InvoicesFragment.newInstance(1), "开票中")
                .addFragment(InvoicesFragment.newInstance(3), "已开票"));
        vp.setOffscreenPageLimit(2);
        tab.setupWithViewPager(vp);
    }

    public static class InvoicesFragment extends RecyclerFragment<Invoice> {

        private SlimAdapter mAdapter;
        private int mStatus; // 发票状态.0未开票1.申请开票3.已开票

        public static InvoicesFragment newInstance(int status) {
            InvoicesFragment fragment = new InvoicesFragment();
            Bundle args = new Bundle();
            args.putInt("status", status);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mStatus = getArguments().getInt("status", 0);
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Invoice> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Invoice>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.invoice_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Invoice> viewHolder) {
                        }

                        @Override
                        public void onBind(Invoice data, SlimAdapter.SlimViewHolder<Invoice> viewHolder) {
                            viewHolder.text(R.id.tv_order_no, data.orderPid)
                                    .text(R.id.tv_product_name, "")
                                    .text(R.id.tv_car_order_no, "")
                                    .text(R.id.tv_company, "")
                                    .text(R.id.tv_quantity, String.valueOf(data.quantity))
                                    .text(R.id.tv_price, "")
                                    .text(R.id.tv_amount, data.amount)
                                    .text(R.id.tv_time, DateUtils.convertDateToStr(new Date(data.createDate)))
                                    .text(R.id.tv_region, "")
                            ;
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Invoice>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getCommissionInvoices(mStatus, (currentPage - 1) * 20, 20)
                    .map(quoteResponsePageData -> {
                        PageResult<Invoice> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<Invoice> data) {
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
