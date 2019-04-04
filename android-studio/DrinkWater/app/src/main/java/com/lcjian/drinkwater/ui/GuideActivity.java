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

import com.lcjian.drinkwater.MainActivity;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.base.BaseActivity;

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
    @BindView(R.id.fl_container)
    FrameLayout fl_container;
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
                    ObjectAnimator in = slideHIn(ll_gender_female, 2.5f);
                    in.setStartDelay(100);
                    set.playTogether(slideHIn(ll_gender_male, 2.5f), in,
                            slideHOut(ll_hello, -1f));
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
                }
                showNextTitle(getString(R.string.your_weight));

                v_weight.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    AnimatorSet out = slideHFadeOut(ll_gender_female, -1f);
                    out.setStartDelay(50);
                    AnimatorSet in = slideHFadeIn(iv_weight, 1.5f);
                    in.setStartDelay(50);
                    set.playTogether(slideHOut(ll_gender_male, -2f),
                            out,
                            in,
                            slideHFadeIn(pv_weight, 3f),
                            slideHFadeIn(pv_weight_unit, 3f));
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
                }
                showNextTitle(getString(R.string.get_up_time));

                v_get_up_time.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(slideHFadeOut(iv_weight, -0.3f),
                            slideVFadeOut(pv_weight, -0.3f),
                            slideVFadeOut(pv_weight_unit, -0.3f),
                            slideHFadeIn(iv_get_up, 0.5f),
                            slideVFadeIn(pv_get_up_time_hour, 0.3f),
                            slideVFadeIn(pv_get_up_time_colon, 0.3f),
                            slideVFadeIn(pv_get_up_time_minute, 0.3f));
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
                }
                showNextTitle(getString(R.string.sleep_time));

                v_sleep_time.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(slideHFadeOut(iv_get_up, -0.3f),
                            slideVFadeOut(pv_get_up_time_hour, -0.3f),
                            slideVFadeOut(pv_get_up_time_colon, -0.3f),
                            slideVFadeOut(pv_get_up_time_minute, -0.3f),
                            slideHFadeIn(iv_sleep, 0.5f),
                            slideVFadeIn(pv_sleep_time_hour, 0.3f),
                            slideVFadeIn(pv_sleep_time_colon, 0.3f),
                            slideVFadeIn(pv_sleep_time_minute, 0.3f));
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
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    private void pre() {
        switch (mState) {
            case 1:
                showPreTitle("");
                v_gender.post(() -> {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(slideHOut(ll_gender_male, 2.5f),
                            slideHOut(ll_gender_female, 2.5f),
                            slideHIn(ll_hello, -1f));
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
                    set.playTogether(slideHIn(ll_gender_male, -2f),
                            slideHFadeIn(ll_gender_female, -1f),
                            slideHFadeOut(iv_weight, 1.5f),
                            slideHFadeOut(pv_weight, 3f),
                            slideHFadeOut(pv_weight_unit, 3f));
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
                    set.playTogether(slideHFadeIn(iv_weight, -0.5f),
                            slideVFadeIn(pv_weight, -0.3f),
                            slideVFadeIn(pv_weight_unit, -0.3f),
                            slideHFadeOut(iv_get_up, 0.3f),
                            slideVFadeOut(pv_get_up_time_hour, 0.3f),
                            slideVFadeOut(pv_get_up_time_colon, 0.3f),
                            slideVFadeOut(pv_get_up_time_minute, 0.3f));
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
                    set.playTogether(slideHFadeIn(iv_get_up, -0.5f),
                            slideVFadeIn(pv_get_up_time_hour, -0.3f),
                            slideVFadeIn(pv_get_up_time_colon, -0.3f),
                            slideVFadeIn(pv_get_up_time_minute, -0.3f),
                            slideHFadeOut(iv_sleep, 0.3f),
                            slideVFadeOut(pv_sleep_time_hour, 0.3f),
                            slideVFadeOut(pv_sleep_time_colon, 0.3f),
                            slideVFadeOut(pv_sleep_time_minute, 0.3f));
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
        if (mState == 0) {
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

    private ObjectAnimator slideHIn(View view, float percent) {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth() * percent, 0);
    }

    private ObjectAnimator slideHOut(View view, float percent) {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, view.getWidth() * percent);
    }

    private AnimatorSet slideHFadeIn(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth() * percent, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    private AnimatorSet slideHFadeOut(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, view.getWidth() * percent);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    private AnimatorSet slideVFadeIn(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getHeight() * percent, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    private AnimatorSet slideVFadeOut(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, view.getHeight() * percent);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }
}
