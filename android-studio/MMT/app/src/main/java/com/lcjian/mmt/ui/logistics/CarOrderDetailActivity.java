package com.lcjian.mmt.ui.logistics;

import android.os.Bundle;
import android.view.View;
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

public class CarOrderDetailActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.v_dot_0)
    View v_dot_0;
    @BindView(R.id.v_line_0)
    View v_line_0;
    @BindView(R.id.v_dot_1)
    View v_dot_1;
    @BindView(R.id.v_line_1)
    View v_line_1;
    @BindView(R.id.v_dot_2)
    View v_dot_2;
    @BindView(R.id.v_line_2)
    View v_line_2;
    @BindView(R.id.v_dot_3)
    View v_dot_3;
    @BindView(R.id.v_line_3)
    View v_line_3;
    @BindView(R.id.v_dot_4)
    View v_dot_4;
    @BindView(R.id.v_line_4)
    View v_line_4;
    @BindView(R.id.v_dot_5)
    View v_dot_5;
    @BindView(R.id.v_line_5)
    View v_line_5;
    @BindView(R.id.v_dot_6)
    View v_dot_6;
    @BindView(R.id.tv_car_order_no_d)
    TextView tv_car_order_no_d;
    @BindView(R.id.tv_car_order_product_name)
    TextView tv_car_order_product_name;
    @BindView(R.id.tv_car_order_price_d)
    TextView tv_car_order_price_d;
    @BindView(R.id.tv_car_order_quantity_d)
    TextView tv_car_order_quantity_d;
    @BindView(R.id.tv_car_order_amount_d)
    TextView tv_car_order_amount_d;
    @BindView(R.id.tv_car_order_car_no_d)
    TextView tv_car_order_car_no_d;
    @BindView(R.id.tv_car_order_car_driver_d)
    TextView tv_car_order_car_driver_d;
    @BindView(R.id.tv_car_order_car_phone_d)
    TextView tv_car_order_car_phone_d;
    @BindView(R.id.tv_car_order_car_escort_d)
    TextView tv_car_order_car_escort_d;
    @BindView(R.id.tv_car_order_car_escort_phone_d)
    TextView tv_car_order_car_escort_phone_d;
    @BindView(R.id.tv_car_order_load_time_d)
    TextView tv_car_order_load_time_d;
    @BindView(R.id.tv_car_order_unloaded_time_d)
    TextView tv_car_order_unloaded_time_d;

    private String mId;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_order_detail);
        ButterKnife.bind(this);

        mId = getIntent().getStringExtra("car_order_id");
        tv_title.setText(R.string.order_detail);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        setup();
    }

    private void setup() {
        hideProgress();
        mDisposable = mRestAPI.cloudService().getCarOrderDetail(mId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(transOrderResponseData -> {
                            hideProgress();
                            int status = Integer.parseInt(transOrderResponseData.data.status);
                            if (status >= 0) { // 空车未出发
                                v_dot_6.setBackgroundResource(R.drawable.shape_dot_status_ur);
                                v_line_5.setBackgroundColor(0xffff4800);
                            }
                            if (status >= 1) { // 已出车
                                v_dot_5.setBackgroundResource(R.drawable.shape_dot_status_ur);
                                v_line_4.setBackgroundColor(0xffff4800);
                            }
                            if (status >= 2) { // 待装货
                                v_dot_4.setBackgroundResource(R.drawable.shape_dot_status_ur);
                                v_line_3.setBackgroundColor(0xffff4800);
                            }
                            if (status >= 3) { // 运输中
                                v_dot_3.setBackgroundResource(R.drawable.shape_dot_status_ur);
                                v_line_2.setBackgroundColor(0xffff4800);
                            }
                            if (status >= 4) { // 待卸货
                                v_dot_2.setBackgroundResource(R.drawable.shape_dot_status_ur);
                                v_line_1.setBackgroundColor(0xffff4800);
                            }
                            if (status >= 5) { // 结束
                                v_dot_0.setBackgroundResource(R.drawable.shape_dot_status_ur);
                            }
                            if (status >= 6) { // 其他
                                v_dot_1.setBackgroundResource(R.drawable.shape_dot_status_ur);
                                v_line_0.setBackgroundColor(0xffff4800);
                            }

                            tv_car_order_no_d.setText(transOrderResponseData.data.tranOrderCode);
                            tv_car_order_product_name.setText(transOrderResponseData.data.mmtProducts.name);

                            tv_car_order_price_d.setText(new Spans().append(getString(R.string.currency_sign))
                                    .append(String.valueOf(transOrderResponseData.data.price / 100)));
                            tv_car_order_quantity_d.setText(new Spans().append(String.valueOf(transOrderResponseData.data.quantity / 1000))
                                    .append(getString(R.string.weight_holder)));
                            tv_car_order_amount_d.setText(new Spans().append(getString(R.string.currency_sign))
                                    .append(String.valueOf(transOrderResponseData.data.amount / 100)));
                            tv_car_order_car_no_d.setText(transOrderResponseData.data.cars.carCode);
                            tv_car_order_car_driver_d.setText(transOrderResponseData.data.cars.driver1 == null ? "暂无" : transOrderResponseData.data.cars.driver1.realname);
                            tv_car_order_car_phone_d.setText(transOrderResponseData.data.cars.driver1 == null ? "暂无" : transOrderResponseData.data.cars.driver1.mobile);
                            tv_car_order_car_escort_d.setText(transOrderResponseData.data.cars.escort == null ? "暂无" : transOrderResponseData.data.cars.escort.name);
                            tv_car_order_car_escort_phone_d.setText(transOrderResponseData.data.cars.escort == null ? "暂无" : transOrderResponseData.data.cars.escort.phone);
                            tv_car_order_load_time_d.setText(DateUtils.convertDateToStr(new Date(transOrderResponseData.data.loadTime), DateUtils.YYYY_MM_DD_HH_MM_SS));
                            tv_car_order_unloaded_time_d.setText(DateUtils.convertDateToStr(new Date(transOrderResponseData.data.unloadeTime), DateUtils.YYYY_MM_DD_HH_MM_SS));
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
