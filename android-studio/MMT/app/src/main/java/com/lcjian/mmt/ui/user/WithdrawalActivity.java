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
import com.lcjian.mmt.data.network.entity.BankCard;
import com.lcjian.mmt.data.network.entity.Deposit;
import com.lcjian.mmt.ui.base.BaseActivity;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WithdrawalActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.tv_withdrawal_bank_card)
    TextView tv_withdrawal_bank_card;
    @BindView(R.id.et_withdrawal_amount)
    EditText et_withdrawal_amount;
    @BindView(R.id.tv_balance)
    TextView tv_balance;
    @BindView(R.id.tv_withdrawal_all)
    TextView tv_withdrawal_all;
    @BindView(R.id.btn_confirm_to_withdrawal)
    Button btn_confirm_to_withdrawal;

    private Disposable mDisposable;
    private Disposable mDisposableW;

    private BankCard mBankCard;
    private Deposit mDeposit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);
        ButterKnife.bind(this);

        tv_nav_right.setVisibility(View.VISIBLE);
        tv_nav_right.setText(getString(R.string.withdraw_records));
        tv_title.setText(getString(R.string.withdraw));
        btn_confirm_to_withdrawal.setText(R.string.withdraw);

        et_withdrawal_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validate();
            }
        });
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), RecordsActivity.class)
                .putExtra("record_type", 3)));
        tv_withdrawal_bank_card.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), BankCardsActivity.class)
                .putExtra("from", WithdrawalActivity.class.getSimpleName()), 1000));
        btn_confirm_to_withdrawal.setOnClickListener(v -> withdraw());
        tv_withdrawal_all.setOnClickListener(v -> {
            if (mDeposit != null) {
                et_withdrawal_amount.setText(String.valueOf(mDeposit.balance));
            }
        });

        validate();
        getDeposit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1000 && data != null) {
                mBankCard = (BankCard) data.getSerializableExtra("bank_card");
                String s = mBankCard.openBank + " " + mBankCard.cardNo;
                tv_withdrawal_bank_card.setText(s);
                validate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableW != null) {
            mDisposableW.dispose();
        }
        super.onDestroy();
    }

    private void validate() {
        btn_confirm_to_withdrawal.setEnabled(!TextUtils.isEmpty(et_withdrawal_amount.getEditableText().toString())
                && mBankCard != null);
    }

    private void getDeposit() {
        showProgress();
        mDisposable = mRestAPI.cloudService().getDeposit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deposit -> {
                            hideProgress();
                            mDeposit = deposit;
                            tv_balance.setText(getString(R.string.current_balance, new DecimalFormat("0.00").format(mDeposit.balance / 100)));
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void withdraw() {
        showProgress();
        mDisposableW = mRestAPI.cloudService().withdraw(mBankCard.id, (int) (Double.parseDouble(et_withdrawal_amount.getEditableText().toString()) * 100))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
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
}
