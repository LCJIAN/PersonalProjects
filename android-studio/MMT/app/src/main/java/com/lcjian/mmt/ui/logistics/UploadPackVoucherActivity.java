package com.lcjian.mmt.ui.logistics;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
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
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.util.DateUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
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
import top.zibin.luban.Luban;

public class UploadPackVoucherActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.et_weight_1)
    EditText et_weight_1;
    @BindView(R.id.et_weight_2)
    EditText et_weight_2;
    @BindView(R.id.iv_after_load_pound)
    ImageView iv_after_load_pound;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    private String mId;
    private Boolean mLoad;
    private Date mTime;
    private String mAfter;

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pack_voucher);
        ButterKnife.bind(this);

        mId = getIntent().getStringExtra("car_order_id");
        mLoad = getIntent().getBooleanExtra("load", true);
        tv_title.setText(mLoad ? R.string.upload_load_pound : R.string.upload_unload_pound);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_time.setOnClickListener(v -> showDatePickDialog());
        iv_after_load_pound.setOnClickListener(v -> chooseImage());
        btn_confirm.setOnClickListener(v -> submit());
        et_weight_1.addTextChangedListener(this);
        et_weight_2.addTextChangedListener(this);
        validate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            if (requestCode == 1000) {
                mAfter = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mAfter)
                        .into(iv_after_load_pound);
            }
            validate();
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

    private void showDatePickDialog() {
        Calendar todayCal = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.YEAR, 6);

        new DatePickDialog.Builder()
                .setTypes(DateParams.TYPE_YEAR,
                        DateParams.TYPE_MONTH,
                        DateParams.TYPE_DAY,
                        DateParams.TYPE_HOUR,
                        DateParams.TYPE_MINUTE)
                .setCurrentDate(todayCal.getTime())
                .setStartDate(startCal.getTime())
                .setEndDate(endCal.getTime())
                .setOnSureListener(date -> {
                    mTime = date;
                    tv_time.setText(DateUtils.convertDateToStr(mTime, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    validate();
                })
                .show(this);
    }

    private void submit() {
        showProgress();
        mDisposable = createUploadSingle(Collections.singletonList(mAfter))
                .flatMap(strings ->
                        mRestAPI.cloudService().uploadTransPound(
                                mId,
                                mId,
                                mTime.getTime(),
                                mLoad ? 1 : 2,
                                Integer.parseInt(et_weight_1.getEditableText().toString()) * 1000,
                                Integer.parseInt(et_weight_2.getEditableText().toString()) * 1000,
                                strings.get(0))
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

    private void validate() {
        btn_confirm.setEnabled(mTime != null
                && mAfter != null
                && !TextUtils.isEmpty(et_weight_1.getEditableText())
                && !TextUtils.isEmpty(et_weight_2.getEditableText()));
    }

    private void chooseImage() {
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
                                .forResult(1000);
                    } else {
                        Toast.makeText(App.getInstance(), "no permissions", Toast.LENGTH_LONG).show();
                    }
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
                    File file = Luban.with(this).load(s).get().get(0);
                    return mRestAPI
                            .cloudService()
                            .uploadImage(MultipartBody.Part.createFormData(
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
