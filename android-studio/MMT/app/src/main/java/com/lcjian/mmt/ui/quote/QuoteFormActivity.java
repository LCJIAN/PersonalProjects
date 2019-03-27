package com.lcjian.mmt.ui.quote;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aitangba.pickdatetime.DatePickDialog;
import com.aitangba.pickdatetime.bean.DateParams;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.QuotePrepare;
import com.lcjian.mmt.data.network.entity.TransQuoteForm;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.util.DateUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class QuoteFormActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.rv_quote_form)
    RecyclerView rv_quote_form;
    @BindView(R.id.btn_submit)
    Button btn_submit;

    private SlimAdapter mAdapter;
    private QuotePrepare mQuotePrepare;

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_form);
        ButterKnife.bind(this);

        mQuotePrepare = (QuotePrepare) getIntent().getSerializableExtra("quote_prepare");
        tv_title.setText(getString(R.string.fill_in_quote_form));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_submit.setOnClickListener(v -> checkPermissionsForSubmit());

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<TransQuoteForm>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.trans_quote_form_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<TransQuoteForm> viewHolder) {
                viewHolder
                        .with(R.id.et_trans_num, view -> ((EditText) view).addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                viewHolder.itemData.abletranNum = TextUtils.isEmpty(s) ? null : Integer.parseInt(s.toString());
                            }
                        }))
                        .with(R.id.et_t_price, view -> ((EditText) view).addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                viewHolder.itemData.tprice = TextUtils.isEmpty(s) ? null : Double.parseDouble(s.toString()) * 100;
                            }
                        }))
                        .with(R.id.et_ut_price, view -> ((EditText) view).addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                viewHolder.itemData.utprice = TextUtils.isEmpty(s) ? null : Double.parseDouble(s.toString()) * 100;
                            }
                        }))
                        .with(R.id.et_carrying_capacity, view -> ((EditText) view).addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                viewHolder.itemData.carryingCapacity = TextUtils.isEmpty(s) ? null : Integer.parseInt(s.toString());
                            }
                        }))
                        .with(R.id.et_time_consuming, view -> ((EditText) view).addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                viewHolder.itemData.timeConsuming = TextUtils.isEmpty(s) ? null : s.toString();
                            }
                        }))
                        .clicked(R.id.tv_load_time, v -> showDatePickDialog(0, viewHolder))
                        .clicked(R.id.tv_car_order_unload_time, v -> showDatePickDialog(1, viewHolder));
            }

            @Override
            public void onBind(TransQuoteForm data, SlimAdapter.SlimViewHolder<TransQuoteForm> viewHolder) {
                viewHolder.text(R.id.tv_car_no, data.car.carCode)
                        .text(R.id.tv_car_driver, data.car.driver1 == null ? "暂无" : data.car.driver1.realname)
                        .text(R.id.tv_car_contact, data.car.driver1 == null ? "暂无" : data.car.driver1.mobile)
                        .text(R.id.et_trans_num, data.abletranNum == null ? null : String.valueOf(data.abletranNum))
                        .text(R.id.et_t_price, data.tprice == null ? null : String.valueOf(data.tprice / 100))
                        .text(R.id.et_ut_price, data.utprice == null ? null : String.valueOf(data.utprice / 100))
                        .text(R.id.et_carrying_capacity, data.carryingCapacity == null ? null : String.valueOf(data.carryingCapacity))
                        .text(R.id.et_time_consuming, data.timeConsuming)
                        .text(R.id.tv_load_time, data.arrivalTime == null ? null : DateUtils.convertDateToStr(new Date(data.arrivalTime), "yyyy-MM-dd HH:mm"))
                        .text(R.id.tv_car_order_unload_time, data.unloadTime == null ? null : DateUtils.convertDateToStr(new Date(data.unloadTime), "yyyy-MM-dd HH:mm"));
            }
        });
        rv_quote_form.setHasFixedSize(true);
        rv_quote_form.setLayoutManager(new LinearLayoutManager(this));
        rv_quote_form.setAdapter(mAdapter);

        mDisposable = Observable
                .fromIterable(mQuotePrepare.cars)
                .map(car -> {
                    TransQuoteForm transQuoteForm = new TransQuoteForm();
                    transQuoteForm.trucksId = car.id;
                    transQuoteForm.car = car;
                    return transQuoteForm;
                })
                .toList()
                .subscribe(transQuoteForms -> mAdapter.updateData(transQuoteForms));
    }

    private void checkPermissionsForSubmit() {
        RxPermissions rxPermissions = new RxPermissions(this);
        mDisposableP = rxPermissions
                .request(Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        submit();
                    } else {
                        Toast.makeText(App.getInstance(), "no permissions", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void submit() {
        List<TransQuoteForm> forms = (List<TransQuoteForm>) mAdapter.getData();
        for (TransQuoteForm form : forms) {
            if (form.abletranNum == null
                    || form.tprice == null
                    || form.utprice == null
                    || form.arrivalTime == null
                    || form.unloadTime == null
                    || form.carryingCapacity == null
                    || TextUtils.isEmpty(form.timeConsuming)) {
                Toast.makeText(this, getString(R.string.please_input_all), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mQuotePrepare.cars = null;
        mQuotePrepare.carsItem = forms;
        startActivity(new Intent(this, QuoteSubmitPreviewActivity.class)
                .putExtra("quote_prepare", mQuotePrepare));
    }

    private void showDatePickDialog(int position, SlimAdapter.SlimViewHolder<TransQuoteForm> viewHolder) {
        Calendar todayCal = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.YEAR, 6);

        new DatePickDialog.Builder()
                .setTypes(DateParams.TYPE_YEAR,
                        DateParams.TYPE_MONTH,
                        DateParams.TYPE_DAY,
                        DateParams.TYPE_HOUR,
                        DateParams.TYPE_MINUTE)
                .setCurrentDate(todayCal.getTime())
                .setStartDate(startCal.getTime())
                .setEndDate(endCal.getTime())
                .setOnSureListener(date -> {
                    if (position == 0) {
                        viewHolder.itemData.arrivalTime = date.getTime();
                    } else {
                        viewHolder.itemData.unloadTime = date.getTime();
                    }
                    mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                })
                .show(this);
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroy();
    }
}
