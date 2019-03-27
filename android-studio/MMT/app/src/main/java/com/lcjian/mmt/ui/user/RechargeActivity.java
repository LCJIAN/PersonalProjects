package com.lcjian.mmt.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ThrowableConsumerAdapter;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.wxapi.WeChatPay;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RechargeActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.tv_balance)
    TextView tv_balance;
    @BindView(R.id.et_recharge_amount)
    EditText et_recharge_amount;
    @BindView(R.id.btn_confirm_to_recharge)
    Button btn_confirm_to_recharge;

    private boolean mBond;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        ButterKnife.bind(this);

        tv_nav_right.setVisibility(View.VISIBLE);
        tv_nav_right.setText(R.string.recharge_records);
        mBond = getIntent().getBooleanExtra("bond", false);
        tv_title.setText(mBond ? R.string.bond_recharge : R.string.recharge);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), RecordsActivity.class)
                .putExtra("record_type", mBond ? 2 : 1)));

        et_recharge_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_confirm_to_recharge.setEnabled(!TextUtils.isEmpty(s));
            }
        });

        btn_confirm_to_recharge.setEnabled(!TextUtils.isEmpty(et_recharge_amount.getEditableText()));
        btn_confirm_to_recharge.setOnClickListener(v -> {
            if (mBond) {
                rechargeBond((int) (Double.parseDouble(et_recharge_amount.getEditableText().toString()) * 100));
            } else {
                recharge(Double.parseDouble(et_recharge_amount.getEditableText().toString()));
            }
        });
        getDeposit();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void getDeposit() {
        showProgress();
        mDisposable = mRestAPI.cloudService().getDeposit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deposit -> {
                            hideProgress();
                            tv_balance.setText(new DecimalFormat("0.00").format(deposit.balance / 100));
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void rechargeBond(int amount) {
        showProgress();
        mDisposable = mRestAPI.cloudService().rechargeBond(amount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
                                Toast.makeText(App.getInstance(), R.string.recharge_success, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void recharge(Double amount) {
        showProgress();
        mDisposable = mRestAPI.cloudService().createWeChatPayOrder(amount, "余额充值", "", "WL")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderResponseData -> {
                            hideProgress();
                            if (orderResponseData.code == 1) {
                                WeChatPay.pay(RechargeActivity.this, orderResponseData.data.appid,
                                        orderResponseData.data.mch_id,
                                        orderResponseData.data.prepay_id,
                                        orderResponseData.data.nonce_str,
                                        TextUtils.isEmpty(orderResponseData.data.timestamp) ? String.valueOf(System.currentTimeMillis()) : orderResponseData.data.timestamp,
                                        orderResponseData.data.package_value,
                                        orderResponseData.data.sign);
                            } else {
                                Toast.makeText(App.getInstance(), orderResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }
}
