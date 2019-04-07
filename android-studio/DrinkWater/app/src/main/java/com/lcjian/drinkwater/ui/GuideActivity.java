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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class GuideActivity extends BaseActivity {

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

    private List<Unit> mUnits;
    private Config mConfig;
    private Setting mSetting;

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

        tv_skip.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        btn_next.setOnClickListener(v -> next());


        mConfig = mAppDatabase.configDao().getAllSync().get(0);
        mSetting = mAppDatabase.settingDao().getAllSync().get(0);
        mUnits = mAppDatabase.unitDao().getAllSync();
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

                    setupWeightPicker();
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

                    setupGetUpTimePicker();
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

                    setupSleepTimePicker();
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

    @Override
    public void onBackPressed() {
        if (mState == 0 || mState == 5) {
            super.onBackPressed();
        } else {
            pre();
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


    private void setupWeightPicker() {
        Unit currentUnit = null;
        for (Unit u : mUnits) {
            if (u.id.equals(mSetting.unitId)) {
                currentUnit = u;
                break;
            }
        }
        if (currentUnit != null) {
            int minValue = (int) (mConfig.minWeight * currentUnit.rate);
            int maxValue = (int) (mConfig.maxWeight * currentUnit.rate);
            int value = (int) (mSetting.weight * currentUnit.rate) - 1;

            List<String> strings = new ArrayList<>();
            for (int i = minValue; i <= maxValue; i++) {
                strings.add(String.valueOf(i));
            }
            String[] a = new String[strings.size()];
            pv_weight.setDisplayedValuesAndPickedIndex(strings.toArray(a), value, true);
        }
        {
            List<String> strings = new ArrayList<>();
            for (Unit unit : mUnits) {
                strings.add(unit.name.split(",")[0]);
            }
            String[] a = new String[strings.size()];
            pv_weight_unit.setDisplayedValuesAndPickedIndex(strings.toArray(a), mUnits.indexOf(currentUnit), true);
        }
    }


    private void setupGetUpTimePicker() {
        setTimePickerData(pv_get_up_time_hour, 0, 23, Integer.parseInt(mSetting.wakeUpTime.split(":")[0]));
        setTimePickerData(pv_get_up_time_minute, 0, 59, Integer.parseInt(mSetting.wakeUpTime.split(":")[1]));
        setTimePickerData(pv_get_up_time_colon, 0, 0, 0);
    }

    private void setupSleepTimePicker() {
        setTimePickerData(pv_sleep_time_hour, 0, 23, Integer.parseInt(mSetting.sleepTime.split(":")[0]));
        setTimePickerData(pv_sleep_time_minute, 0, 59, Integer.parseInt(mSetting.sleepTime.split(":")[1]));
        setTimePickerData(pv_sleep_time_colon, 0, 0, 0);
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
        picker.setDisplayedValuesAndPickedIndex(strings.toArray(a), value, true);
    }

}
