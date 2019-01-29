package com.lcjian.mmt.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);
        ButterKnife.bind(this);

        tv_nav_right.setVisibility(View.VISIBLE);
        tv_nav_right.setText("提现记录");
        tv_title.setText("提现");
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), RecordsActivity.class)
                .putExtra("record_type", 2)));
    }
}
