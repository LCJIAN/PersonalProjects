package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.et_feedback)
    EditText et_feedback;
    @BindView(R.id.btn_submit)
    Button btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);

        tv_title.setText(R.string.feedback);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
    }
}
