package com.lcjian.mmt.ui.user;

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
import com.lcjian.mmt.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddBankCardActivity extends BaseActivity implements TextWatcher, View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.et_bank_card_owner_name)
    EditText et_bank_card_owner_name;
    @BindView(R.id.et_bank_card_no)
    EditText et_bank_card_no;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_card);
        ButterKnife.bind(this);

        tv_title.setText(R.string.add_bank_card);
        et_bank_card_owner_name.addTextChangedListener(this);
        et_bank_card_no.addTextChangedListener(this);
        btn_confirm.setOnClickListener(this);
        validate();
    }

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

    private void validate() {
        btn_confirm.setEnabled(!TextUtils.isEmpty(et_bank_card_owner_name.getEditableText())
                && !TextUtils.isEmpty(et_bank_card_no.getEditableText()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                showProgress();
                mDisposable = mRestAPI.cloudService().addBankCard(et_bank_card_no.getEditableText().toString(),
                        et_bank_card_owner_name.getEditableText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bankCardResponseData -> {
                                    hideProgress();
                                    if (bankCardResponseData.code == 1) {
                                        finish();
                                    } else {
                                        Toast.makeText(App.getInstance(), bankCardResponseData.message, Toast.LENGTH_SHORT).show();
                                    }
                                },
                                throwable -> hideProgress());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
