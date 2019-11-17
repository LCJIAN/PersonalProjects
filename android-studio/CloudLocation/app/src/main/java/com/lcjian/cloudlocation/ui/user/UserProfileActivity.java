package com.lcjian.cloudlocation.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UserProfileActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_user_other_info)
    TextView tv_user_other_info;
    @BindView(R.id.tv_contact_name)
    TextView tv_contact_name;
    @BindView(R.id.cl_contact)
    ConstraintLayout cl_contact;
    @BindView(R.id.tv_phone)
    TextView tv_phone;
    @BindView(R.id.cl_phone)
    ConstraintLayout cl_phone;
    @BindView(R.id.tv_email)
    TextView tv_email;
    @BindView(R.id.cl_email)
    ConstraintLayout cl_email;
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.cl_address)
    ConstraintLayout cl_address;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.user_info));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.bjwl_bc);
        btn_nav_right.setOnClickListener(v -> save());

        tv_user_name.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_user_name.getText().toString()), 1000));
        cl_contact.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_contact_name.getText().toString()), 1001));
        cl_phone.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_phone.getText().toString()), 1002));
        cl_email.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_email.getText().toString()), 1003));
        cl_address.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_address.getText().toString()), 1004));

        showProgress();
        mDisposable = mRestAPI.cloudService().getUserProfile(Long.parseLong(getSignInInfo().userInfo.userID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userProfile -> {
                            hideProgress();
                            tv_user_name.setText(userProfile.name);
                            tv_user_other_info.setText(userProfile.loginName);
                            tv_contact_name.setText(userProfile.contact);
                            tv_phone.setText(userProfile.phone);
                            tv_email.setText(userProfile.email);
                            tv_address.setText(userProfile.address);
                        },
                        throwable -> hideProgress());
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1000:
                    tv_user_name.setText(data.getStringExtra("text"));
                    break;
                case 1001:
                    tv_contact_name.setText(data.getStringExtra("text"));
                    break;
                case 1002:
                    tv_phone.setText(data.getStringExtra("text"));
                    break;
                case 1003:
                    tv_email.setText(data.getStringExtra("text"));
                    break;
                case 1004:
                    tv_address.setText(data.getStringExtra("text"));
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void save() {
        showProgress();
        mDisposable = mRestAPI.cloudService().updateUserProfile(
                Long.parseLong(getSignInInfo().userInfo.userID),
                tv_user_name.getText().toString(),
                tv_contact_name.getText().toString(),
                tv_phone.getText().toString(),
                tv_email.getText().toString(),
                tv_address.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                            hideProgress();
                            if (TextUtils.equals("2005", state.state)) {
                                Toast.makeText(App.getInstance(), R.string.save_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(App.getInstance(), R.string.save_failed, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> hideProgress());
    }
}
