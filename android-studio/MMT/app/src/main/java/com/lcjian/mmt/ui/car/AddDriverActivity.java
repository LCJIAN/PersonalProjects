package com.lcjian.mmt.ui.car;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aitangba.pickdatetime.DatePickDialog;
import com.aitangba.pickdatetime.bean.DateParams;
import com.lcjian.mmt.App;
import com.lcjian.mmt.BuildConfig;
import com.lcjian.mmt.GlideApp;
import com.lcjian.mmt.GlideEngine;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.Certificate;
import com.lcjian.mmt.data.network.entity.DriverPrepare;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.util.DateUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import timber.log.Timber;

public class AddDriverActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.et_driver)
    EditText et_driver;
    @BindView(R.id.et_phone)
    EditText et_phone;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.iv_identity_fore)
    ImageView iv_identity_fore;
    @BindView(R.id.iv_identity_back)
    ImageView iv_identity_back;
    @BindView(R.id.et_certificate_name)
    EditText et_certificate_name;
    @BindView(R.id.et_certificate_no)
    EditText et_certificate_no;
    @BindView(R.id.iv_certificate)
    ImageView iv_certificate;
    @BindView(R.id.tv_forever)
    TextView tv_forever;
    @BindView(R.id.tv_expiry)
    TextView tv_expiry;
    @BindView(R.id.tv_expiry_time_start)
    TextView tv_expiry_time_start;
    @BindView(R.id.tv_expiry_time_end)
    TextView tv_expiry_time_end;

    private boolean mForever;
    private Date mStartDate;
    private Date mEndDate;
    private String mIdentityFore;
    private String mIdentityBack;
    private String mCertificate;

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);
        ButterKnife.bind(this);

        mForever = true;

        tv_title.setText(R.string.add_driver);
        tv_nav_right.setVisibility(View.VISIBLE);
        tv_nav_right.setText(R.string.confirm);
        tv_nav_right.setOnClickListener(v -> submit());
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        iv_identity_fore.setOnClickListener(v -> chooseImage(1000));
        iv_identity_back.setOnClickListener(v -> chooseImage(1001));
        iv_certificate.setOnClickListener(v -> chooseImage(1002));
        tv_forever.setOnClickListener(v -> {
            mForever = true;
            switchExpiry();
        });
        tv_expiry.setOnClickListener(v -> {
            mForever = false;
            switchExpiry();
        });
        tv_expiry_time_start.setOnClickListener(v -> showDatePickDialog(0));
        tv_expiry_time_end.setOnClickListener(v -> showDatePickDialog(1));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            if (requestCode == 1000) {
                mIdentityFore = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mIdentityFore)
                        .into(iv_identity_fore);
            } else if (requestCode == 1001) {
                mIdentityBack = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mIdentityBack)
                        .into(iv_identity_back);
            } else {
                mCertificate = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mCertificate)
                        .into(iv_certificate);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroy();
    }

    private void chooseImage(int requestCode) {
        RxPermissions rxPermissions = new RxPermissions(this);
        mDisposableP = rxPermissions.request(
                Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        Matisse.from(this)
                                .choose(MimeType.ofImage())
                                .capture(true)
                                .captureStrategy(new CaptureStrategy(true, BuildConfig.FILE_PROVIDER_AUTHORITIES, "Matisse"))
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .thumbnailScale(0.85f)
                                .imageEngine(new GlideEngine())
                                .forResult(requestCode);
                    } else {
                        Toast.makeText(App.getInstance(), "no permissions", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDatePickDialog(int position) {
        Calendar todayCal = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        startCal.add(Calendar.YEAR, -100);
        endCal.add(Calendar.YEAR, 100);

        new DatePickDialog.Builder()
                .setTypes(DateParams.TYPE_YEAR,
                        DateParams.TYPE_MONTH,
                        DateParams.TYPE_DAY)
                .setCurrentDate(todayCal.getTime())
                .setStartDate(startCal.getTime())
                .setEndDate(endCal.getTime())
                .setOnSureListener(date -> {
                    if (position == 0) {
                        mStartDate = date;
                        tv_expiry_time_start.setText(DateUtils.convertDateToStr(mStartDate));
                    } else {
                        mEndDate = date;
                        tv_expiry_time_end.setText(DateUtils.convertDateToStr(mEndDate));
                    }
                })
                .show(this);
    }

    private void switchExpiry() {
        tv_forever.setBackgroundColor(mForever ? 0xff429fcb : 0xffcdcdcd);
        tv_expiry.setBackgroundColor(!mForever ? 0xff429fcb : 0xffcdcdcd);
        tv_expiry_time_start.setEnabled(!mForever);
        tv_expiry_time_end.setEnabled(!mForever);
    }

    private void submit() {
        if (TextUtils.isEmpty(et_phone.getEditableText())
                || TextUtils.isEmpty(et_driver.getEditableText())
                || TextUtils.isEmpty(et_password.getEditableText())
                || TextUtils.isEmpty(et_certificate_name.getEditableText())
                || TextUtils.isEmpty(et_certificate_no.getEditableText())
                || mIdentityFore == null
                || mIdentityBack == null
                || mCertificate == null
                || (!mForever && (mStartDate == null || mEndDate == null))) {
            Toast.makeText(App.getInstance(), R.string.please_input_all, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress();
        mDisposable = createUploadSingle(Arrays.asList(mIdentityFore, mIdentityBack, mCertificate))
                .flatMap(strings -> {
                            DriverPrepare driverPrepare = new DriverPrepare();
                            driverPrepare.mobile = et_phone.getEditableText().toString();
                            driverPrepare.realname = et_driver.getEditableText().toString();
                            driverPrepare.password = et_password.getEditableText().toString();
                            driverPrepare.certificatesList = new ArrayList<>();
                            Certificate certificateFore = new Certificate();
                            Certificate certificateBack = new Certificate();
                            Certificate certificate = new Certificate();

                            certificateFore.sortId = "x";
                            certificateFore.cerName = "身份证正面";
                            certificateFore.cerNo = null;
                            certificateFore.picturePath = strings.get(0);

                            certificateBack.sortId = "x";
                            certificateBack.cerName = "身份证背面";
                            certificateBack.cerNo = null;
                            certificateBack.picturePath = strings.get(1);

                            certificate.sortId = "x";
                            certificate.cerName = et_certificate_name.getEditableText().toString();
                            certificate.cerNo = et_certificate_no.getEditableText().toString();
                            certificate.picturePath = strings.get(2);

                            driverPrepare.certificatesList.add(certificateFore);
                            driverPrepare.certificatesList.add(certificateBack);
                            driverPrepare.certificatesList.add(certificate);
                            return mRestAPI.cloudService().addDriver(driverPrepare);
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            if (stringResponseData.code == 1) {
                                finish();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Timber.e(throwable);
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private Single<List<String>> createUploadSingle(List<String> photos) {
        if (photos == null || photos.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        return Observable
                .fromIterable(photos)
                .map(s -> {
                    if (s.startsWith("http")) {
                        return Single.just(s);
                    }
                    File file = new File(s);
                    return mRestAPI
                            .cloudService()
                            .uploadCer(MultipartBody.Part.createFormData(
                                    "uploadFile", URLDecoder.decode(file.getName(), "utf-8"), RequestBody.create(MediaType.parse("image/*"), file)))
                            .map(stringResponseData -> stringResponseData.data);
                })
                .toList()
                .flatMap(singles -> Single.zip(singles, objects -> {
                    List<String> strings = new ArrayList<>();
                    for (Object object : objects) {
                        strings.add((String) object);
                    }
                    return strings;
                }));
    }

}
