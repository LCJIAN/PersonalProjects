package com.lcjian.cloudlocation.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.et_text)
    EditText et_text;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        et_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_confirm.setEnabled(!TextUtils.isEmpty(s));
            }
        });
        et_text.setText(getIntent().getStringExtra("text"));

        tv_title.setText(getString(R.string.edit));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_confirm.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("text", et_text.getEditableText().toString());
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
