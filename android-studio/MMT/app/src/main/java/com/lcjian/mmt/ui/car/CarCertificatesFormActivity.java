package com.lcjian.mmt.ui.car;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.lcjian.mmt.data.network.entity.CarPrepare;
import com.lcjian.mmt.data.network.entity.Certificate;
import com.lcjian.mmt.data.network.entity.Image;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.main.MainActivity;
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
import androidx.constraintlayout.widget.ConstraintLayout;
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

public class CarCertificatesFormActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.cer_1)
    ConstraintLayout cer_1;
    @BindView(R.id.cer_2)
    ConstraintLayout cer_2;
    @BindView(R.id.cer_3)
    ConstraintLayout cer_3;
    @BindView(R.id.cer_4)
    ConstraintLayout cer_4;
    @BindView(R.id.cer_5)
    ConstraintLayout cer_5;
    @BindView(R.id.cer_6)
    ConstraintLayout cer_6;
    @BindView(R.id.cer_7)
    ConstraintLayout cer_7;
    @BindView(R.id.cer_8)
    ConstraintLayout cer_8;

    private CarPrepare mCarPrepare;
    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_certificates);
        ButterKnife.bind(this);

        mCarPrepare = (CarPrepare) getIntent().getSerializableExtra("car_prepare");
        if (mCarPrepare.certificates == null) {
            mCarPrepare.certificates = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                Certificate certificate = new Certificate();
                certificate.sortId = "0";
                certificate.picturePath = "";
                mCarPrepare.certificates.add(certificate);
            }
        }
        tv_title.setText(R.string.car_cer);
        tv_nav_right.setText("去认证");
        tv_nav_right.setVisibility(View.VISIBLE);
        btn_nav_back.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("car_prepare", mCarPrepare);
            setResult(RESULT_OK, intent);
            finish();
        });
        tv_nav_right.setOnClickListener(v -> submit());

        setupItem(cer_1, 0);
        setupItem(cer_2, 1);
        setupItem(cer_3, 2);
        setupItem(cer_4, 3);
        setupItem(cer_5, 4);
        setupItem(cer_6, 5);
        setupItem(cer_7, 6);
        setupItem(cer_8, 7);
    }

    private void setupItem(ConstraintLayout parent, int position) {
        Certificate certificate = mCarPrepare.certificates.get(position);

        TextView tv_cer_name_i = parent.findViewById(R.id.tv_cer_name_i);
        EditText et_cer_no_i = parent.findViewById(R.id.et_cer_no_i);
        ImageView iv_cer_image_i = parent.findViewById(R.id.iv_cer_image_i);
        TextView tv_cer_exp_forever_i = parent.findViewById(R.id.tv_cer_exp_forever_i);
        TextView tv_cer_exp_i = parent.findViewById(R.id.tv_cer_exp_i);
        TextView tv_cer_exp_time_start_i = parent.findViewById(R.id.tv_cer_exp_time_start_i);
        TextView tv_cer_exp_time_end_i = parent.findViewById(R.id.tv_cer_exp_time_end_i);

        switch (position) {
            case 0:
                tv_cer_name_i.setText("行驶证");
                certificate.cerName = "行驶证";
                break;
            case 1:
                tv_cer_name_i.setText("道路运输许可证");
                certificate.cerName = "道路运输许可证";
                break;
            case 2:
                tv_cer_name_i.setText("年检资料");
                certificate.cerName = "年检资料";
                break;
            case 3:
                tv_cer_name_i.setText("罐体年检报告");
                certificate.cerName = "罐体年检报告";
                break;
            case 4:
                tv_cer_name_i.setText("危险品运输许可证");
                certificate.cerName = "危险品运输许可证";
                break;
            case 5:
                tv_cer_name_i.setText("GPS安装证");
                certificate.cerName = "GPS安装证";
                break;
            case 6:
                tv_cer_name_i.setText("挂靠公司危险品运输资质");
                certificate.cerName = "挂靠公司危险品运输资质";
                break;
            case 7:
                tv_cer_name_i.setText("挂靠公司营业执照");
                certificate.cerName = "挂靠公司营业执照";
                break;
            default:
                tv_cer_name_i.setText("挂靠公司营业执照");
                certificate.cerName = "挂靠公司营业执照";
                break;
        }
        if (!TextUtils.isEmpty(certificate.cerNo)) {
            et_cer_no_i.setText(certificate.cerNo);
        }
        if (!TextUtils.isEmpty(certificate.picturePath)) {
            GlideApp.with(this)
                    .load(certificate.picturePath)
                    .into(iv_cer_image_i);
        }
        switchExpiry(tv_cer_exp_forever_i, tv_cer_exp_i, tv_cer_exp_time_start_i, tv_cer_exp_time_end_i, certificate);
        if (certificate.expStart != null) {
            tv_cer_exp_time_start_i.setText(DateUtils.convertDateToStr(new Date(certificate.expStart)));
        }
        if (certificate.expEnd != null) {
            tv_cer_exp_time_end_i.setText(DateUtils.convertDateToStr(new Date(certificate.expEnd)));
        }

        iv_cer_image_i.setOnClickListener(v -> chooseImage(mCarPrepare.certificates.indexOf(certificate)));
        tv_cer_exp_forever_i.setOnClickListener(v -> {
            certificate.isForever = "1";
            switchExpiry(tv_cer_exp_forever_i, tv_cer_exp_i, tv_cer_exp_time_start_i, tv_cer_exp_time_end_i, certificate);
        });
        tv_cer_exp_i.setOnClickListener(v -> {
            certificate.isForever = "0";
            switchExpiry(tv_cer_exp_forever_i, tv_cer_exp_i, tv_cer_exp_time_start_i, tv_cer_exp_time_end_i, certificate);
        });
        tv_cer_exp_time_start_i.setOnClickListener(v -> showDatePickDialog(tv_cer_exp_time_start_i, tv_cer_exp_time_end_i, 0, certificate));
        tv_cer_exp_time_end_i.setOnClickListener(v -> showDatePickDialog(tv_cer_exp_time_start_i, tv_cer_exp_time_end_i, 1, certificate));
        et_cer_no_i.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                certificate.cerNo = s.toString();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            Certificate certificate = mCarPrepare.certificates.get(requestCode);
            String uri = Matisse.obtainPathResult(data).get(0);
            ConstraintLayout cl;
            switch (requestCode) {
                case 0:
                    cl = cer_1;
                    break;
                case 1:
                    cl = cer_2;
                    break;
                case 2:
                    cl = cer_3;
                    break;
                case 3:
                    cl = cer_4;
                    break;
                case 4:
                    cl = cer_5;
                    break;
                case 5:
                    cl = cer_6;
                    break;
                case 6:
                    cl = cer_7;
                    break;
                case 7:
                    cl = cer_8;
                    break;
                default:
                    cl = cer_8;
                    break;
            }
            GlideApp.with(this)
                    .load(uri)
                    .into((ImageView) cl.findViewById(R.id.iv_cer_image_i));
            certificate.picturePath = uri;
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

    private void submit() {
        showProgress();
        mDisposable = Single.zip(
                mCarPrepare.escort.certificates.isEmpty()
                        ? Single.just(Collections.emptyList())
                        : createUploadSingleEscortCer(Collections.singletonList(mCarPrepare.escort.certificates.get(0).picturePath)),
                mCarPrepare.images.isEmpty()
                        ? Single.just(Collections.emptyList())
                        : Observable.fromIterable(mCarPrepare.images).map(image -> image.url).toList().flatMap(this::createUploadSingleCarImages),
                mCarPrepare.certificates.isEmpty()
                        ? Single.just(Collections.emptyList())
                        : Observable.fromIterable(mCarPrepare.certificates).map(certificate -> certificate.picturePath).toList().flatMap(this::createUploadSingleCers),
                Img::new)
                .flatMap(img -> {
                    if (!img.escorts.isEmpty()) {
                        mCarPrepare.escort.certificates.get(0).picturePath = img.escorts.get(0);
                    }
                    if (!img.carImages.isEmpty()) {
                        for (int i = 0; i < img.carImages.size(); i++) {
                            Image image = mCarPrepare.images.get(i);
                            image.url = img.carImages.get(i);
                        }
                    }
                    if (!img.carCerImages.isEmpty()) {
                        for (int i = 0; i < img.carCerImages.size(); i++) {
                            Certificate certificate = mCarPrepare.certificates.get(i);
                            certificate.picturePath = img.carCerImages.get(i);
                        }
                    }
                    return mRestAPI.cloudService().addCar(mCarPrepare);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            if (stringResponseData.code == 1) {
                                startActivity(new Intent(CarCertificatesFormActivity.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Timber.e(throwable);
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
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

    private void switchExpiry(TextView tv_forever, TextView tv_expiry, TextView tv_expiry_time_start,
                              TextView tv_expiry_time_end, Certificate certificate) {
        boolean forever = TextUtils.equals("1", certificate.isForever);
        tv_forever.setBackgroundColor(forever ? 0xff429fcb : 0xffcdcdcd);
        tv_expiry.setBackgroundColor(!forever ? 0xff429fcb : 0xffcdcdcd);
        tv_expiry_time_start.setEnabled(!forever);
        tv_expiry_time_end.setEnabled(!forever);
    }

    private void showDatePickDialog(TextView tv_expiry_time_start,
                                    TextView tv_expiry_time_end,
                                    int position,
                                    Certificate certificate) {
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
                        certificate.expStart = date.getTime();
                        tv_expiry_time_start.setText(DateUtils.convertDateToStr(date));
                    } else {
                        certificate.expEnd = date.getTime();
                        tv_expiry_time_end.setText(DateUtils.convertDateToStr(date));
                    }
                })
                .show(this);
    }

    private Single<List<String>> createUploadSingleCarImages(List<String> photos) {
        if (photos == null || photos.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        return Observable
                .fromIterable(photos)
                .map(s -> {
                    if (s.startsWith("http") || TextUtils.isEmpty(s)) {
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

    private Single<List<String>> createUploadSingleEscortCer(List<String> photos) {
        if (photos == null || photos.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        return Observable
                .fromIterable(photos)
                .map(s -> {
                    if (s.startsWith("http") || TextUtils.isEmpty(s)) {
                        return Single.just(s);
                    }
                    File file = Luban.with(this).load(s).get().get(0);
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

    private Single<List<String>> createUploadSingleCers(List<String> photos) {
        if (photos == null || photos.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        return Observable
                .fromIterable(photos)
                .map(s -> {
                    if (s.startsWith("http") || TextUtils.isEmpty(s)) {
                        return Single.just(s);
                    }
                    File file = Luban.with(this).load(s).get().get(0);
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

    private static class Img {
        private List<String> escorts;
        private List<String> carImages;
        private List<String> carCerImages;

        private Img(List<String> escorts, List<String> carImages, List<String> carCerImages) {
            this.escorts = escorts;
            this.carImages = carImages;
            this.carCerImages = carCerImages;
        }
    }

}
