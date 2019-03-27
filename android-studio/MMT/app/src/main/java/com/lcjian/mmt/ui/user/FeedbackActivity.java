package com.lcjian.mmt.ui.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.et_feedback)
    EditText et_feedback;
    @BindView(R.id.btn_submit)
    Button btn_submit;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);

        tv_title.setText(R.string.feedback);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        et_feedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_submit.setEnabled(!TextUtils.isEmpty(s));
            }
        });

        btn_submit.setEnabled(!TextUtils.isEmpty(et_feedback.getEditableText()));
        btn_submit.setOnClickListener(v -> addFeedback());
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void addFeedback() {
        showProgress();
        mDisposable = mRestAPI.cloudService().addFeedback(et_feedback.getEditableText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            if (stringResponseData.code == 1) {
                                Toast.makeText(App.getInstance(), getString(R.string.feedback_success), Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), stringResponseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }
}
