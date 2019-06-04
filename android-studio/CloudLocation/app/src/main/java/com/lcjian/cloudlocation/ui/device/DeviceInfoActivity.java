package com.lcjian.cloudlocation.ui.device;

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
import com.lcjian.cloudlocation.ui.user.EditActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceInfoActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.tv_device_name)
    TextView tv_device_name;
    @BindView(R.id.tv_device_no)
    TextView tv_device_no;
    @BindView(R.id.tv_device_dead_time)
    TextView tv_device_dead_time;
    @BindView(R.id.tv_device_car_no)
    TextView tv_device_car_no;
    @BindView(R.id.tv_device_model)
    TextView tv_device_model;
    @BindView(R.id.tv_device_sim)
    TextView tv_device_sim;
    @BindView(R.id.tv_device_contact)
    TextView tv_device_contact;
    @BindView(R.id.tv_device_contact_phone)
    TextView tv_device_contact_phone;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.device_info));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.bjwl_bc);
        btn_nav_right.setOnClickListener(v -> save());

        tv_device_name.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_device_name.getText().toString()), 1000));
        tv_device_car_no.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_device_car_no.getText().toString()), 1001));
        tv_device_sim.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_device_sim.getText().toString()), 1002));
        tv_device_contact.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_device_contact.getText().toString()), 1003));
        tv_device_contact_phone.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), EditActivity.class).putExtra("text", tv_device_contact_phone.getText().toString()), 1004));

        showProgress();
        mDisposable = mRestAPI.cloudService().getDeviceDetail(Long.parseLong(getIntent().getStringExtra("device_id")), "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceDetail -> {
                            hideProgress();
                            tv_device_name.setText(deviceDetail.name);
                            tv_device_no.setText(deviceDetail.sn);
                            tv_device_dead_time.setText(deviceDetail.hireExpireTime);
                            tv_device_car_no.setText(deviceDetail.carNum);
                            tv_device_model.setText(deviceDetail.model);
                            tv_device_sim.setText(deviceDetail.phone);
                            tv_device_contact.setText(deviceDetail.userName);
                            tv_device_contact_phone.setText(deviceDetail.cellPhone);
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
                    tv_device_name.setText(data.getStringExtra("text"));
                    break;
                case 1001:
                    tv_device_car_no.setText(data.getStringExtra("text"));
                    break;
                case 1002:
                    tv_device_sim.setText(data.getStringExtra("text"));
                    break;
                case 1003:
                    tv_device_contact.setText(data.getStringExtra("text"));
                    break;
                case 1004:
                    tv_device_contact_phone.setText(data.getStringExtra("text"));
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void save() {
        showProgress();
        mDisposable = mRestAPI.cloudService().updateDevice(
                Long.parseLong(getIntent().getStringExtra("device_id")),
                tv_device_name.getText().toString(),
                tv_device_car_no.getText().toString(),
                tv_device_sim.getText().toString(),
                tv_device_contact.getText().toString(),
                tv_device_contact_phone.getText().toString())
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
