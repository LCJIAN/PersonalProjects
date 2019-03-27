package com.lcjian.mmt.ui.car;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import com.lcjian.mmt.data.network.entity.Dict;
import com.lcjian.mmt.data.network.entity.Driver;
import com.lcjian.mmt.data.network.entity.Escort;
import com.lcjian.mmt.data.network.entity.Image;
import com.lcjian.mmt.data.network.entity.ProductType;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.common.PickerFragment;
import com.lcjian.mmt.util.DateUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddCarActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.et_car_no)
    EditText et_car_no;
    @BindView(R.id.tv_car_driver)
    TextView tv_car_driver;
    @BindView(R.id.et_car_length)
    EditText et_car_length;
    @BindView(R.id.et_car_width)
    EditText et_car_width;
    @BindView(R.id.et_car_height)
    EditText et_car_height;
    @BindView(R.id.et_car_box_length)
    EditText et_car_box_length;
    @BindView(R.id.et_car_box_width)
    EditText et_car_box_width;
    @BindView(R.id.et_car_box_height)
    EditText et_car_box_height;
    @BindView(R.id.tv_car_box_type)
    TextView tv_car_box_type;
    @BindView(R.id.rg_available_goods_type_1)
    RadioGroup rg_available_goods_type_1;
    @BindView(R.id.rg_available_goods_type_2)
    RadioGroup rg_available_goods_type_2;
    @BindView(R.id.tv_car_type)
    TextView tv_car_type;
    @BindView(R.id.ll_product_type)
    LinearLayout ll_product_type;
    @BindView(R.id.tv_product_type)
    TextView tv_product_type;
    @BindView(R.id.et_car_carrying_capacity)
    EditText et_car_carrying_capacity;
    @BindView(R.id.et_escort)
    EditText et_escort;
    @BindView(R.id.et_escort_phone)
    EditText et_escort_phone;
    @BindView(R.id.iv_escort_cer)
    ImageView iv_escort_cer;
    @BindView(R.id.tv_escort_cer_validate_time_start)
    TextView tv_escort_cer_validate_time_start;
    @BindView(R.id.tv_escort_cer_validate_time_end)
    TextView tv_escort_cer_validate_time_end;
    @BindView(R.id.iv_car_head_image)
    ImageView iv_car_head_image;
    @BindView(R.id.iv_car_side_one_image)
    ImageView iv_car_side_one_image;
    @BindView(R.id.iv_car_side_two_image)
    ImageView iv_car_side_two_image;
    @BindView(R.id.iv_car_tails_image)
    ImageView iv_car_tails_image;

    private Driver mDriver;
    private Dict mDictCar;
    private Dict mDictCarBox;

    private String mEscortUri;
    private String mCarHeadUri;
    private String mCarSideOneUri;
    private String mCarSideTwoUri;
    private String mCarTailsUri;

    private Date mExpStartDate;
    private Date mExpEndDate;

    private Integer a1;
    private Integer a2;

    private List<ProductType> mProductTypes;

    private CarPrepare mCarPrepare;

    private Disposable mDisposableD;
    private Disposable mDisposableC;
    private Disposable mDisposableCB;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);
        ButterKnife.bind(this);
        mProductTypes = new ArrayList<>();

        tv_title.setText(R.string.add_car);
        tv_nav_right.setVisibility(View.VISIBLE);
        tv_nav_right.setText(R.string.next_step);
        tv_nav_right.setOnClickListener(this);
        btn_nav_back.setOnClickListener(this);
        tv_car_driver.setOnClickListener(this);
        tv_car_type.setOnClickListener(this);
        tv_car_box_type.setOnClickListener(this);
        iv_escort_cer.setOnClickListener(this);
        iv_car_head_image.setOnClickListener(this);
        iv_car_side_one_image.setOnClickListener(this);
        iv_car_side_two_image.setOnClickListener(this);
        iv_car_tails_image.setOnClickListener(this);
        tv_escort_cer_validate_time_start.setOnClickListener(this);
        tv_escort_cer_validate_time_end.setOnClickListener(this);
        tv_product_type.setOnClickListener(this);

        rg_available_goods_type_1.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_solid) {
                a1 = 1;
            } else if (checkedId == R.id.rb_liquid) {
                a1 = 2;
            } else {
                a1 = 3;
            }
        });
        rg_available_goods_type_2.setOnCheckedChangeListener((group, checkedId) -> {
            TransitionManager.beginDelayedTransition((ViewGroup) ll_product_type.getParent());
            if (checkedId == R.id.rb_chemistry) {
                a2 = 1;
                ll_product_type.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rb_normal) {
                a2 = 2;
                ll_product_type.setVisibility(View.GONE);
            } else {
                a2 = 3;
                ll_product_type.setVisibility(View.GONE);
            }
        });
        rg_available_goods_type_1.check(R.id.rb_solid);
        rg_available_goods_type_2.check(R.id.rb_normal);
    }

    @Override
    protected void onDestroy() {
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        if (mDisposableC != null) {
            mDisposableC.dispose();
        }
        if (mDisposableCB != null) {
            mDisposableCB.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nav_back:
                onBackPressed();
                break;
            case R.id.tv_nav_right: {
                mCarPrepare = new CarPrepare();
                mCarPrepare.carCode = et_car_no.getEditableText().toString();
                mCarPrepare.carSize = et_car_length.getEditableText().toString() + "*" + et_car_width.getEditableText().toString() + "*" + et_car_height.getEditableText().toString();
                mCarPrepare.boxSize = et_car_box_length.getEditableText().toString() + "*" + et_car_box_width.getEditableText().toString() + "*" + et_car_box_height.getEditableText().toString();
                mCarPrepare.boxType = mDictCarBox == null ? null : mDictCarBox.type;
                mCarPrepare.goodsType = a1 + "" + a2;
                mCarPrepare.carType = mDictCar == null ? null : mDictCar.type;
                mCarPrepare.loadWeight = TextUtils.isEmpty(et_car_carrying_capacity.getEditableText()) ? null : Double.parseDouble(et_car_carrying_capacity.getEditableText().toString());
                mCarPrepare.driverId1 = mDriver == null ? null : mDriver.userId;
                mCarPrepare.ownerId = null;
                mCarPrepare.driverId2 = null;
                if (a2 == 1 && mProductTypes != null) {
                    List<String> strings = new ArrayList<>();
                    for (ProductType type : mProductTypes) {
                        strings.add(type.id);
                    }
                    mCarPrepare.productTypeIds = TextUtils.join(",", strings);
                }
                mCarPrepare.escort = new Escort();
                mCarPrepare.escort.name = et_escort.getEditableText().toString();
                mCarPrepare.escort.mobile = et_escort_phone.getEditableText().toString();

                mCarPrepare.escort.certificates = new ArrayList<>();
                Certificate certificate = new Certificate();
                certificate.cerName = "押运证";
                certificate.picturePath = mEscortUri == null ? "" : mEscortUri;
                certificate.expStart = mExpStartDate == null ? null : mExpStartDate.getTime();
                certificate.expEnd = mExpEndDate == null ? null : mExpEndDate.getTime();
                mCarPrepare.escort.certificates.add(certificate);

                mCarPrepare.images = new ArrayList<>();
                {
                    Image image = new Image();
                    image.sort = "1";
                    image.url = mCarHeadUri == null ? "" : mCarHeadUri;
                    mCarPrepare.images.add(image);
                }
                {
                    Image image = new Image();
                    image.sort = "2";
                    image.url = mCarSideOneUri == null ? "" : mCarSideOneUri;
                    mCarPrepare.images.add(image);
                }
                {
                    Image image = new Image();
                    image.sort = "3";
                    image.url = mCarSideTwoUri == null ? "" : mCarSideTwoUri;
                    mCarPrepare.images.add(image);
                }
                {
                    Image image = new Image();
                    image.sort = "4";
                    image.url = mCarTailsUri == null ? "" : mCarTailsUri;
                    mCarPrepare.images.add(image);
                }

                v.getContext().startActivity(new Intent(v.getContext(), CarCertificatesFormActivity.class)
                        .putExtra("car_prepare", mCarPrepare));
            }
            break;
            case R.id.tv_car_driver:
                showProgress();
                mDisposableD = mRestAPI.cloudService()
                        .getDrivers()
                        .toObservable()
                        .flatMap(listResponseData -> Observable.fromIterable(listResponseData.data))
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(drivers -> {
                                    hideProgress();
                                    ArrayList<String> strings = new ArrayList<>();
                                    for (Driver driver : drivers) {
                                        strings.add(driver.realname);
                                    }
                                    PickerFragment.newInstance(getString(R.string.please_pick_driver), strings)
                                            .setOnPickListener((item, position) -> {
                                                mDriver = drivers.get(position);
                                                tv_car_driver.setText(mDriver.realname);
                                            })
                                            .setExtraOnClickListener(v1 ->
                                                    v1.getContext().startActivity(new Intent(v1.getContext(), AddDriverActivity.class))
                                            )
                                            .show(getSupportFragmentManager(), "PickerFragment");
                                },
                                throwable -> hideProgress());
                break;
            case R.id.tv_car_type:
                showProgress();
                mDisposableC = mRestAPI.cloudService().dict("car_type")
                        .toObservable()
                        .flatMap(Observable::fromIterable)
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dictList -> {
                                    hideProgress();
                                    ArrayList<String> strings = new ArrayList<>();
                                    for (Dict dict : dictList) {
                                        strings.add(dict.label);
                                    }
                                    PickerFragment.newInstance(getString(R.string.please_pick_car_type), new ArrayList<>(strings))
                                            .setOnPickListener((item, position) -> {
                                                mDictCar = dictList.get(position);
                                                tv_car_type.setText(mDictCar.label);
                                            })
                                            .show(getSupportFragmentManager(), "PickerFragment");
                                },
                                throwable -> hideProgress());
                break;
            case R.id.tv_car_box_type:
                showProgress();
                mDisposableCB = mRestAPI.cloudService().dict("box_type")
                        .toObservable()
                        .flatMap(Observable::fromIterable)
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dictList -> {
                                    hideProgress();
                                    ArrayList<String> strings = new ArrayList<>();
                                    for (Dict dict : dictList) {
                                        strings.add(dict.label);
                                    }
                                    PickerFragment.newInstance(getString(R.string.please_pick_car_box_type), new ArrayList<>(strings))
                                            .setOnPickListener((item, position) -> {
                                                mDictCarBox = dictList.get(position);
                                                tv_car_box_type.setText(mDictCarBox.label);
                                            })
                                            .show(getSupportFragmentManager(), "PickerFragment");
                                },
                                throwable -> hideProgress());
                break;
            case R.id.tv_product_type:
                startActivityForResult(new Intent(this, ProductTypesActivity.class)
                        .putExtra("data", new ArrayList<>(mProductTypes)), 1005);
                break;
            case R.id.iv_escort_cer:
                chooseImage(1000);
                break;
            case R.id.iv_car_head_image:
                chooseImage(1001);
                break;
            case R.id.iv_car_side_one_image:
                chooseImage(1002);
                break;
            case R.id.iv_car_side_two_image:
                chooseImage(1003);
                break;
            case R.id.iv_car_tails_image:
                chooseImage(1004);
                break;
            case R.id.tv_escort_cer_validate_time_start:
                showDatePickDialog(0);
                break;
            case R.id.tv_escort_cer_validate_time_end:
                showDatePickDialog(1);
                break;
            default:
                break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            if (requestCode == 1000) {
                mEscortUri = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mEscortUri)
                        .into(iv_escort_cer);
            } else if (requestCode == 1001) {
                mCarHeadUri = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mCarHeadUri)
                        .into(iv_car_head_image);
            } else if (requestCode == 1002) {
                mCarSideOneUri = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mCarSideOneUri)
                        .into(iv_car_side_one_image);
            } else if (requestCode == 1003) {
                mCarSideTwoUri = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mCarSideTwoUri)
                        .into(iv_car_side_two_image);
            } else if (requestCode == 1004) {
                mCarTailsUri = Matisse.obtainPathResult(data).get(0);
                GlideApp.with(this)
                        .load(mCarTailsUri)
                        .into(iv_car_tails_image);
            } else {
                mProductTypes = (ArrayList<ProductType>) data.getSerializableExtra("data");
            }
        }
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
                        mExpStartDate = date;
                        tv_escort_cer_validate_time_start.setText(DateUtils.convertDateToStr(mExpStartDate));
                    } else {
                        mExpEndDate = date;
                        tv_escort_cer_validate_time_end.setText(DateUtils.convertDateToStr(mExpEndDate));
                    }
                })
                .show(this);
    }
}
