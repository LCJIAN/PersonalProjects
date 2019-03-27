package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Brokerage;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BrokerageActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.commission_info));
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        if (getSupportFragmentManager().findFragmentByTag("BrokerageFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_fragment_container, new BrokerageFragment(), "BrokerageFragment").commit();
        }
    }

    public static class BrokerageFragment extends RecyclerFragment<Brokerage> {

        private SlimAdapter mAdapter;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Brokerage> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Brokerage>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.brokerage_item;
                        }

                        @Override
                        public void onBind(Brokerage data, SlimAdapter.SlimViewHolder<Brokerage> viewHolder) {
                            String s = "";
                            switch (data.invoiceStatus) {
                                case 0:
                                    s = "未开票";
                                    break;
                                case 1:
                                    s = "已开票";
                                    break;
                                case 2:
                                    s = "开票中";
                                    break;
                                default:
                                    break;
                            }
                            viewHolder
                                    .text(R.id.tv_order_no_b, data.mmtOrder.tranOrderCode)
                                    .text(R.id.tv_order_product_name_b, data.mmtProducts.name)
                                    .text(R.id.tv_order_product_spec_b, data.mmtProducts.special)
                                    .text(R.id.tv_company_b, data.mmtMerchants.name)
                                    .text(R.id.tv_brokerage_amount_b, data.amount / 100 + "元")
                                    .text(R.id.tv_car_order_no_b, data.mmtTransOrder.tranOrderCode)
                                    .text(R.id.tv_status_b, s);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Brokerage>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getBrokerage((currentPage - 1) * 20, 20)
                    .map(quoteResponsePageData -> {
                        PageResult<Brokerage> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<Brokerage> data) {
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

