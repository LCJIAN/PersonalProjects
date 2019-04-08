package com.lcjian.drinkwater.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Config;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class GuideActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_skip)
    TextView tv_skip;
    @BindView(R.id.ts_title)
    TextSwitcher ts_title;
    @BindView(R.id.vs_guide_sleep_time)
    ViewStub vs_guide_sleep_time;
    @BindView(R.id.vs_guide_get_up_time)
    ViewStub vs_guide_get_up_time;
    @BindView(R.id.vs_guide_weight)
    ViewStub vs_guide_weight;
    @BindView(R.id.vs_guide_gender)
    ViewStub vs_guide_gender;
    @BindView(R.id.vs_guide_hello)
    ViewStub vs_guide_hello;
    @BindView(R.id.fl_fragment_container)
    FrameLayout fl_fragment_container;
    @BindView(R.id.btn_next)
    Button btn_next;

    private int mState;

    private View v_hello;
    private LinearLayout ll_hello;

    private View v_gender;
    private LinearLayout ll_gender_male;
    private ImageView iv_gender_male;
    private TextView tv_gender_male;
    private LinearLayout ll_gender_female;
    private ImageView iv_gender_female;
    private TextView tv_gender_female;

    private View v_weight;
    private ImageView iv_weight;
    private NumberPickerView pv_weight;
    private NumberPickerView pv_weight_unit;

    private View v_get_up_time;
    private ImageView iv_get_up;
    private NumberPickerView pv_get_up_time_hour;
    private NumberPickerView pv_get_up_time_colon;
    private NumberPickerView pv_get_up_time_minute;

    private View v_sleep_time;
    private ImageView iv_sleep;
    private NumberPickerView pv_sleep_time_hour;
    private NumberPickerView pv_sleep_time_colon;
    private NumberPickerView pv_sleep_time_minute;

    private PublishSubject<Boolean> mSubject;
    private Disposable mDisposable;
    private Disposable mDisposableU;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowAnimations();
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);

        if (v_hello == null) {
            v_hello = vs_guide_hello.inflate();
            ll_hello = v_hello.findViewById(R.id.ll_hello);
        }
        btn_next.setText(R.string.start);

        tv_skip.setOnClickListener(this);
        btn_next.setOnClickListener(this);

        mSubject = PublishSubject.create();
        mDisposable = Observable
                .combineLatest(mSubject,
                        mAppDatabase.configDao().getAllAsync().toObservable().flatMap(Observable::fromIterable),
                        mAppDatabase.settingDao().getAllAsync().toObservable().flatMap(Observable::fromIterable),
                        mAppDatabase.unitDao().getAllAsync().toObservable(),
                        (aBoolean, config, setting, units) -> new DataHolder(config, setting, units))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataHolder -> {
                            if (v_gender != null) {
                                setupGender(dataHolder.setting);
                            }
                            if (v_weight != null) {
                                setupWeight(dataHolder.config, dataHolder.units, dataHolder.setting);
                            }
                            if (v_get_up_time != null) {
                                setupGetUpTime(dataHolder.setting);
                            }
                            if (v_sleep_time != null) {
                                setupSleepTime(dataHolder.setting);
                            }
                        },
                        throwable -> {
                        });
    }

    @Override
    public void onBackPressed() {
        if (mState == 0 || mState == 5) {
            super.onBackPressed();
        } else {
            pre();
        }
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        if (mDisposableU != null) {
            mDisposableU.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_skip:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.btn_next:
                next();
                break;
            case R.id.ll_gender_male: {
                Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                setting.gender = 0;
                setting.weight = 70d;
                mAppDatabase.settingDao().update(setting);
            }
            break;
            case R.id.ll_gender_female: {
                Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                setting.gender = 1;
                setting.weight = 60d;
                mAppDatabase.settingDao().update(setting);
            }
            default:
                break;
        }
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            window.setAllowEnterTransitionOverlap(false);
            window.setEnterTransition(new TransitionSet()
                    .addTransition(new Slide(Gravity.END)
                            .addTarget(R.id.tv_skip)
                            .addTarget(R.id.tv_hello)
                            .addTarget(R.id.tv_info_before_start)
                            .addTarget(R.id.btn_next))
                    .addTransition(new Fade().addTarget(R.id.tv_skip)
                            .addTarget(R.id.tv_hello)
                            .addTarget(R.id.tv_info_before_start)
                            .addTarget(R.id.btn_next)
                            .addTarget(window.getDecorView())));
        }
    }

    private void setupGender(Setting setting) {
        if (setting.gender == 0) {
            iv_gender_male.setImageResource(R.drawable.ic_avatar_male_checked);
            iv_gender_female.setImageResource(R.drawable.ic_avatar_female_unchecked);
            tv_gender_male.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            tv_gender_female.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
        } else {
            iv_gender_male.setImageResource(R.drawable.ic_avatar_male_unchecked);
            iv_gender_female.setImageResource(R.drawable.ic_avatar_female_checked);
            tv_gender_male.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            tv_gender_female.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        }
    }

    private void setupWeight(Config config, List<Unit> units, Setting setting) {
        Unit currentUnit = null;
        for (Unit u : units) {
            if (u.id.equals(setting.unitId)) {
                currentUnit = u;
                break;
            }
        }
        if (currentUnit != null) {
            int minValue = (int) (config.minWeight * currentUnit.rate);
            int maxValue = (int) (config.maxWeight * currentUnit.rate);
            int value = (int) (setting.weight * currentUnit.rate) - 1;

            List<String> strings = new ArrayList<>();
            for (int i = minValue; i <= maxValue; i++) {
                strings.add(String.valueOf(i));
            }
            String[] a = new String[strings.size()];
            pv_weight.refreshByNewDisplayedValues(strings.toArray(a));
            pv_weight.setValue(value);
        }
        {
            List<String> strings = new ArrayList<>();
            for (Unit unit : units) {
                strings.add(unit.name.split(",")[0]);
            }
            String[] a = new String[strings.size()];
            pv_weight_unit.setDisplayedValuesAndPickedIndex(strings.toArray(a), units.indexOf(currentUnit), true);
        }

        if (setting.gender == 0) {
            iv_weight.setImageResource(R.drawable.ic_weight_male);
        } else {
            iv_weight.setImageResource(R.drawable.ic_weight_female);
        }
    }

    private void setupGetUpTime(Setting setting) {
        setTimePickerData(pv_get_up_time_hour, 0, 23, Integer.parseInt(setting.wakeUpTime.split(":")[0]));
        setTimePickerData(pv_get_up_time_minute, 0, 59, Integer.parseInt(setting.wakeUpTime.split(":")[1]));
        setTimePickerData(pv_get_up_time_colon, 0, 0, 0);

        if (setting.gender == 0) {
            iv_get_up.setImageResource(R.drawable.ic_get_up_time_male);
        } else {
            iv_get_up.setImageResource(R.drawable.ic_get_up_time_female);
        }
    }

    private void setupSleepTime(Setting setting) {
        setTimePickerData(pv_sleep_time_hour, 0, 23, Integer.parseInt(setting.sleepTime.split(":")[0]));
        setTimePickerData(pv_sleep_time_minute, 0, 59, Integer.parseInt(setting.sleepTime.split(":")[1]));
        setTimePickerData(pv_sleep_time_colon, 0, 0, 0);

        if (setting.gender == 0) {
            iv_sleep.setImageResource(R.drawable.ic_sleep_time_male);
        } else {
            iv_sleep.setImageResource(R.drawable.ic_sleep_time_female);
        }
    }

    private void setTimePickerData(NumberPickerView picker, int minValue, int maxValue, int value) {
        List<String> strings = new ArrayList<>();
        if (minValue == maxValue) {
            strings.add(":");
        } else {
            for (int i = minValue; i <= maxValue; i++) {
                strings.add(String.format(Locale.ENGLISH, "%02d", i));
            }
        }
        String[] a = new String[strings.size()];
        picker.refreshByNewDisplayedValues(strings.toArray(a));
        picker.setValue(value);
    }

    private void next() {
        switch (mState) {
            case 0:
                if (v_gender == null) {
                    v_gender = vs_guide_gender.inflate();
                    ll_gender_male = v_gender.findViewById(R.id.ll_gender_male);
                    iv_gender_male = v_gender.findViewById(R.id.iv_gender_male);
                    tv_gender_male = v_gender.findViewById(R.id.tv_gender_male);
                    ll_gender_female = v_gender.findViewById(R.id.ll_gender_female);
                    iv_gender_female = v_gender.findViewById(R.id.iv_gender_female);
                    tv_gender_female = v_gender.findViewById(R.id.tv_gender_female);

                    ll_gender_male.setTranslationX(1000);
                    ll_gender_female.setTranslationX(1000);

                    ll_gender_male.setOnClickListener(this);
                    ll_gender_female.setOnClickListener(this);
                    mSubject.onNext(true);
                }
                showNextTitle(getString(R.string.your_gender));

                v_gender.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    ObjectAnimator in = Utils.slideHIn(ll_gender_female, 2.5f);
                    in.setStartDelay(100);
                    set.playTogether(Utils.slideHIn(ll_gender_male, 2.5f), in,
                            Utils.slideHOut(ll_hello, -1f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_hello.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_gender.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                btn_next.setText(R.string.es);
                mState++;
                break;
            case 1:
                if (v_weight == null) {
                    v_weight = vs_guide_weight.inflate();
                    iv_weight = v_weight.findViewById(R.id.iv_weight);
                    pv_weight = v_weight.findViewById(R.id.pv_weight);
                    pv_weight_unit = v_weight.findViewById(R.id.pv_weight_unit);

                    iv_weight.setTranslationX(1000);
                    pv_weight.setTranslationX(1000);
                    pv_weight_unit.setTranslationX(1000);

                    mSubject.onNext(true);
                }
                showNextTitle(getString(R.string.your_weight));

                v_weight.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    AnimatorSet out = Utils.slideHFadeOut(ll_gender_female, -1f);
                    out.setStartDelay(50);
                    AnimatorSet in = Utils.slideHFadeIn(iv_weight, 1.5f);
                    in.setStartDelay(50);
                    set.playTogether(Utils.slideHOut(ll_gender_male, -2f),
                            out,
                            in,
                            Utils.slideHFadeIn(pv_weight, 3f),
                            Utils.slideHFadeIn(pv_weight_unit, 3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_gender.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_weight.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                v_weight.postDelayed(() -> pv_weight.smoothScrollToValue(pv_weight.getValue() - 1, pv_weight.getValue()), 200);
                mState++;
                break;
            case 2:
                if (v_get_up_time == null) {
                    v_get_up_time = vs_guide_get_up_time.inflate();
                    iv_get_up = v_get_up_time.findViewById(R.id.iv_get_up);
                    pv_get_up_time_hour = v_get_up_time.findViewById(R.id.pv_get_up_time_hour);
                    pv_get_up_time_colon = v_get_up_time.findViewById(R.id.pv_get_up_time_colon);
                    pv_get_up_time_minute = v_get_up_time.findViewById(R.id.pv_get_up_time_minute);

                    iv_get_up.setTranslationX(1000);
                    pv_get_up_time_hour.setTranslationY(1000);
                    pv_get_up_time_colon.setTranslationY(1000);
                    pv_get_up_time_minute.setTranslationY(1000);

                    mSubject.onNext(true);
                }
                showNextTitle(getString(R.string.get_up_time));

                v_get_up_time.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideHFadeOut(iv_weight, -0.3f),
                            Utils.slideVFadeOut(pv_weight, -0.3f),
                            Utils.slideVFadeOut(pv_weight_unit, -0.3f),
                            Utils.slideHFadeIn(iv_get_up, 0.5f),
                            Utils.slideVFadeIn(pv_get_up_time_hour, 0.3f),
                            Utils.slideVFadeIn(pv_get_up_time_colon, 0.3f),
                            Utils.slideVFadeIn(pv_get_up_time_minute, 0.3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_weight.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_get_up_time.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                v_get_up_time.postDelayed(() -> pv_get_up_time_hour
                        .smoothScrollToValue(pv_get_up_time_hour.getValue() - 1, pv_get_up_time_hour.getValue()), 100);
                mState++;
                break;
            case 3:
                if (v_sleep_time == null) {
                    v_sleep_time = vs_guide_sleep_time.inflate();
                    iv_sleep = v_sleep_time.findViewById(R.id.iv_sleep);
                    pv_sleep_time_hour = v_sleep_time.findViewById(R.id.pv_sleep_time_hour);
                    pv_sleep_time_colon = v_sleep_time.findViewById(R.id.pv_sleep_time_colon);
                    pv_sleep_time_minute = v_sleep_time.findViewById(R.id.pv_sleep_time_minute);

                    iv_sleep.setTranslationX(1000);
                    pv_sleep_time_hour.setTranslationY(1000);
                    pv_sleep_time_colon.setTranslationY(1000);
                    pv_sleep_time_minute.setTranslationY(1000);

                    mSubject.onNext(true);
                }
                showNextTitle(getString(R.string.sleep_time));

                v_sleep_time.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideHFadeOut(iv_get_up, -0.3f),
                            Utils.slideVFadeOut(pv_get_up_time_hour, -0.3f),
                            Utils.slideVFadeOut(pv_get_up_time_colon, -0.3f),
                            Utils.slideVFadeOut(pv_get_up_time_minute, -0.3f),
                            Utils.slideHFadeIn(iv_sleep, 0.5f),
                            Utils.slideVFadeIn(pv_sleep_time_hour, 0.3f),
                            Utils.slideVFadeIn(pv_sleep_time_colon, 0.3f),
                            Utils.slideVFadeIn(pv_sleep_time_minute, 0.3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_get_up_time.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_sleep_time.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                v_sleep_time.postDelayed(() -> pv_sleep_time_hour
                        .smoothScrollToValue(pv_sleep_time_hour.getValue() - 1, pv_sleep_time_hour.getValue()), 100);
                mState++;
                break;
            default:
                v_sleep_time.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideVFadeOut(ts_title, 0f),
                            Utils.slideVFadeOut(btn_next, 0f),
                            Utils.slideVFadeOut(iv_sleep, -0.3f),
                            Utils.slideVFadeOut(pv_sleep_time_hour, -0.3f),
                            Utils.slideVFadeOut(pv_sleep_time_colon, -0.3f),
                            Utils.slideVFadeOut(pv_sleep_time_minute, -0.3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ts_title.setVisibility(View.GONE);
                            btn_next.setVisibility(View.GONE);
                            vs_guide_sleep_time.setVisibility(View.GONE);
                            fl_fragment_container.setVisibility(View.VISIBLE);
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.fl_fragment_container, new GuideFragment()).commit();
                        }
                    });
                    set.setDuration(400);
                    set.start();

                    mDisposableU = mAppDatabase.settingDao().getAllAsync().toObservable()
                            .flatMap(Observable::fromIterable)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe(setting -> {
                                        Unit unit = mAppDatabase.unitDao().getAllSyncByName(pv_weight_unit.getContentByCurrValue()).get(0);
                                        setting.unitId = unit.id;
                                        setting.weight = Integer.parseInt(pv_weight.getContentByCurrValue()) * unit.rate;
                                        setting.wakeUpTime = pv_get_up_time_hour.getContentByCurrValue() + ":" + pv_get_up_time_minute.getContentByCurrValue();
                                        setting.sleepTime = pv_sleep_time_hour.getContentByCurrValue() + ":" + pv_sleep_time_minute.getContentByCurrValue();
                                        setting.intakeGoal = setting.weight * (setting.gender + 1);
                                        mAppDatabase.settingDao().update(setting);
                                    },
                                    throwable -> {
                                    });
                });
                mState++;
                break;
        }
    }

    private void pre() {
        switch (mState) {
            case 1:
                showPreTitle("");
                v_gender.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideHOut(ll_gender_male, 2.5f),
                            Utils.slideHOut(ll_gender_female, 2.5f),
                            Utils.slideHIn(ll_hello, -1f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_gender.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_hello.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                btn_next.setText(R.string.start);

                mState--;
                break;
            case 2:
                showPreTitle(getString(R.string.your_gender));
                v_gender.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideHIn(ll_gender_male, -2f),
                            Utils.slideHFadeIn(ll_gender_female, -1f),
                            Utils.slideHFadeOut(iv_weight, 1.5f),
                            Utils.slideHFadeOut(pv_weight, 3f),
                            Utils.slideHFadeOut(pv_weight_unit, 3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_weight.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_gender.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                mState--;
                break;
            case 3:
                showPreTitle(getString(R.string.your_weight));
                v_weight.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideHFadeIn(iv_weight, -0.5f),
                            Utils.slideVFadeIn(pv_weight, -0.3f),
                            Utils.slideVFadeIn(pv_weight_unit, -0.3f),
                            Utils.slideHFadeOut(iv_get_up, 0.3f),
                            Utils.slideVFadeOut(pv_get_up_time_hour, 0.3f),
                            Utils.slideVFadeOut(pv_get_up_time_colon, 0.3f),
                            Utils.slideVFadeOut(pv_get_up_time_minute, 0.3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_get_up_time.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_weight.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                mState--;
                break;
            case 4:
                showPreTitle(getString(R.string.get_up_time));
                v_get_up_time.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(Utils.slideHFadeIn(iv_get_up, -0.5f),
                            Utils.slideVFadeIn(pv_get_up_time_hour, -0.3f),
                            Utils.slideVFadeIn(pv_get_up_time_colon, -0.3f),
                            Utils.slideVFadeIn(pv_get_up_time_minute, -0.3f),
                            Utils.slideHFadeOut(iv_sleep, 0.3f),
                            Utils.slideVFadeOut(pv_sleep_time_hour, 0.3f),
                            Utils.slideVFadeOut(pv_sleep_time_colon, 0.3f),
                            Utils.slideVFadeOut(pv_sleep_time_minute, 0.3f));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vs_guide_sleep_time.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_guide_get_up_time.setVisibility(View.VISIBLE);
                        }
                    });
                    set.setDuration(400);
                    set.setInterpolator(new FastOutSlowInInterpolator());
                    set.start();
                });
                mState--;
                break;
            default:
                break;
        }
    }

    private void showPreTitle(String title) {
        ts_title.setInAnimation(ts_title.getContext(), R.anim.slide_in_left);
        ts_title.setOutAnimation(ts_title.getContext(), R.anim.slide_out_right);
        ts_title.setText(title);
    }

    private void showNextTitle(String title) {
        ts_title.setInAnimation(ts_title.getContext(), R.anim.slide_in_right);
        ts_title.setOutAnimation(ts_title.getContext(), R.anim.slide_out_left);
        ts_title.setText(title);
    }

    private static class DataHolder {
        private Config config;
        private Setting setting;
        private List<Unit> units;

        private DataHolder(Config config, Setting setting, List<Unit> units) {
            this.config = config;
            this.setting = setting;
            this.units = units;
        }
    }
}
