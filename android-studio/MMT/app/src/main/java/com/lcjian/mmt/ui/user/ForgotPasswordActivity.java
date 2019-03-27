package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgotPasswordActivity extends BaseActivity {

    @BindView(R.id.cl_top_bar)
    ConstraintLayout cl_top_bar;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

        cl_top_bar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tv_title.setText(R.string.forgot_password_2);
        btn_nav_back.setImageResource(R.drawable.chevron_left);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container,
                new ForgotPasswordOneFragment(), "ForgotPasswordOneFragment").commit();
    }

    void nextStep(String phone, String verificationCode) {
        tv_title.setText(R.string.modify_password);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container,
                ForgotPasswordTwoFragment.newInstance(phone, verificationCode)).addToBackStack("ForgotPasswordTwoFragment").commit();
    }
}
