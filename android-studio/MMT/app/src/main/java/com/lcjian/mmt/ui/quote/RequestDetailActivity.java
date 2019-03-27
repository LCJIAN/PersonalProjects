package com.lcjian.mmt.ui.quote;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.TransQuote;
import com.lcjian.mmt.ui.base.AdvanceAdapter;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.util.Spans;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RequestDetailActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.rv_request_detail)
    RecyclerView rv_request_detail;

    private TextView tv_length_loading;
    private TextView tv_weight_loading;
    private TextView tv_address_loading;
    private TextView tv_other_loding;
    private TextView tv_working_day_loding;
    private TextView tv_holiday_loding;

    private TextView tv_length_unloading;
    private TextView tv_weight_unloading;
    private TextView tv_address_unloading;
    private TextView tv_other_unloding;
    private TextView tv_working_day_unloding;
    private TextView tv_holiday_unloding;
    private TextView tv_quote_title_label;

    private String mId;
    private Disposable mDisposable;
    private SlimAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        ButterKnife.bind(this);
        mId = getIntent().getStringExtra("trans_request_id");
        tv_title.setText(R.string.detail);
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<TransQuote>() {

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.trans_request_quote_item;
                    }

                    @Override
                    public void onBind(TransQuote data, SlimAdapter.SlimViewHolder<TransQuote> viewHolder) {
                        viewHolder.text(R.id.tv_car_no_q, data.cars.carCode)
                                .text(R.id.tv_car_driver_q, data.cars.driver1 == null ? "暂无" : data.cars.driver1.realname)
                                .text(R.id.tv_able_tran_num_q, String.valueOf(data.abletranNum))
                                .text(R.id.tv_t_price_q, data.tprice / 100 + "元/吨")
                                .text(R.id.tv_ut_price_q, data.utprice / 100 + "元/吨");
                    }
                });
        rv_request_detail.setHasFixedSize(true);
        rv_request_detail.setLayoutManager(new LinearLayoutManager(this));
        rv_request_detail.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).size(1).build());

        AdvanceAdapter advanceAdapter = new AdvanceAdapter(mAdapter);
        View header = LayoutInflater.from(this).inflate(R.layout.trans_request_info, rv_request_detail, false);
        View loadingV = header.findViewById(R.id.cl_loading_info);
        View unloadingV = header.findViewById(R.id.cl_unloading_info);
        tv_length_loading = loadingV.findViewById(R.id.tv_length);
        tv_weight_loading = loadingV.findViewById(R.id.tv_weight);
        tv_address_loading = loadingV.findViewById(R.id.tv_address);
        tv_other_loding = loadingV.findViewById(R.id.tv_other);
        tv_working_day_loding = loadingV.findViewById(R.id.tv_working_day);
        tv_holiday_loding = loadingV.findViewById(R.id.tv_holiday);

        tv_length_unloading = unloadingV.findViewById(R.id.tv_length);
        tv_weight_unloading = unloadingV.findViewById(R.id.tv_weight);
        tv_address_unloading = unloadingV.findViewById(R.id.tv_address);
        tv_other_unloding = unloadingV.findViewById(R.id.tv_other);
        tv_working_day_unloding = unloadingV.findViewById(R.id.tv_working_day);
        tv_holiday_unloding = unloadingV.findViewById(R.id.tv_holiday);

        tv_quote_title_label = header.findViewById(R.id.tv_quote_title_label);

        advanceAdapter.addHeader(header);
        rv_request_detail.setAdapter(advanceAdapter);
        setup();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void setup() {
        showProgress();
        mDisposable = mRestAPI.cloudService().getTransRequest(mId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transRequestResponseData -> {
                    hideProgress();
                    if (transRequestResponseData.code == 1) {
                        tv_length_loading.setText(new Spans().append(String.valueOf(transRequestResponseData.data.product.mmtStores.scaleLength)).append("m"));
                        tv_weight_loading.setText(new Spans().append(String.valueOf(transRequestResponseData.data.product.mmtStores.scaleWeight / 1000)).append("吨"));
                        tv_address_loading.setText(transRequestResponseData.data.product.mmtStores.address);
                        if (!TextUtils.isEmpty(transRequestResponseData.data.product.mmtStores.remarks)) {
                            String[] a1 = transRequestResponseData.data.product.mmtStores.remarks.split("&");
                            tv_working_day_loding.setText(a1[0]);
                            tv_holiday_loding.setText(a1[1]);
                            tv_other_loding.setText(a1[2]);
                        }

                        tv_length_unloading.setText(new Spans().append(String.valueOf(transRequestResponseData.data.inquiry.store.scaleLength)).append("m"));
                        tv_weight_unloading.setText(new Spans().append(String.valueOf(transRequestResponseData.data.inquiry.store.scaleWeight / 1000)).append("吨"));
                        tv_address_unloading.setText(transRequestResponseData.data.inquiry.store.address);
                        if (!TextUtils.isEmpty(transRequestResponseData.data.inquiry.store.remarks)) {
                            String[] a1 = transRequestResponseData.data.inquiry.store.remarks.split("&");
                            tv_working_day_unloding.setText(a1[0]);
                            tv_holiday_unloding.setText(a1[1]);
                            tv_other_unloding.setText(a1[2]);
                        }

                        if (transRequestResponseData.data.quotes == null || transRequestResponseData.data.quotes.isEmpty()) {
                            tv_quote_title_label.setVisibility(View.GONE);
                        }
                        mAdapter.updateData(transRequestResponseData.data.quotes);
                    } else {
                        Toast.makeText(App.getInstance(), transRequestResponseData.message, Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
