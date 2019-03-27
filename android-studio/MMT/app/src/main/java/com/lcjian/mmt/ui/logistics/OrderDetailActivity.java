package com.lcjian.mmt.ui.logistics;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.util.DateUtils;
import com.lcjian.mmt.util.Spans;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OrderDetailActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_order_no)
    TextView tv_order_no;
    @BindView(R.id.tv_product_name)
    TextView tv_product_name;
    @BindView(R.id.tv_commission)
    TextView tv_commission;
    @BindView(R.id.tv_quantity)
    TextView tv_quantity;
    @BindView(R.id.tv_total_freight)
    TextView tv_total_freight;
    @BindView(R.id.tv_order_create_date)
    TextView tv_order_create_date;
    @BindView(R.id.tv_predicate_load_time)
    TextView tv_predicate_load_time;
    @BindView(R.id.tv_predicate_unload_time)
    TextView tv_predicate_unload_time;

    private String mId;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);

        mId = getIntent().getStringExtra("trans_order_id");
        tv_title.setText(R.string.order_detail);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        setup();
    }

    private void setup() {
        hideProgress();
        mDisposable = mRestAPI.cloudService().getTransOrderDetail(mId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transOrderResponseData -> {
                            hideProgress();
                            tv_order_no.setText(transOrderResponseData.data.tranOrderCode);
                            tv_product_name.setText(transOrderResponseData.data.product.name);
                            tv_quantity.setText(new Spans().append(String.valueOf(transOrderResponseData.data.quantity / 1000))
                                    .append(getString(R.string.weight_holder)));
                            tv_total_freight.setText(new Spans().append(getString(R.string.currency_sign))
                                    .append(String.valueOf(transOrderResponseData.data.amount / 100)));
                            tv_order_create_date.setText(DateUtils.convertDateToStr(new Date(transOrderResponseData.data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS));
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
