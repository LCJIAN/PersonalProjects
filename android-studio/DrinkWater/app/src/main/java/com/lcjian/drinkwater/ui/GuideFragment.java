package com.lcjian.drinkwater.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gelitenight.waveview.library.WaveView;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.base.BaseFragment;
import com.lcjian.drinkwater.util.DimenUtils;
import com.lcjian.drinkwater.util.Utils;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Scene;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class GuideFragment extends BaseFragment {

    @BindView(R.id.fl_guide)
    FrameLayout fl_guide;
    @BindView(R.id.wave_view)
    WaveView wave_view;
    @BindView(R.id.fl_scene)
    FrameLayout fl_scene;
    @BindView(R.id.tv_skip)
    TextView tv_skip;
    @BindView(R.id.ll_next_step)
    LinearLayout ll_next_step;
    @BindView(R.id.tv_next_step)
    TextView tv_next_step;
    @BindView(R.id.tv_step_indicator)
    TextView tv_step_indicator;

    @BindView(R.id.iv_avatar_l)
    ImageView iv_avatar_l;
    @BindView(R.id.progress_wheel)
    ProgressWheel progress_wheel;
    @BindView(R.id.fl_avatar)
    FrameLayout fl_avatar;
    @BindView(R.id.tv_plan_generating)
    TextView tv_plan_generating;

    @BindView(R.id.vs_fragment_guide_1)
    ViewStub vs_fragment_guide_1;
    @BindView(R.id.vs_fragment_guide_2)
    ViewStub vs_fragment_guide_2;

    private View v_fragment_guide_1;
    private TextView tv_daily_target;
    private TextView tv_unit;

    private View v_fragment_guide_2;
    private ImageView iv_daily_intake_cup;
    private TextView tv_daily_intake_cup;
    private FrameLayout fl_daily_intake_times;

    private ImageView iv_daily_intake_times;
    private LinearLayout ll_daily_intake_times;
    private TickerView tv_daily_intake_times;

    private ImageView iv_alert_clock;
    private ImageView iv_alert;

    private LinearLayout tv_how_to_drink_label;
    private TextView tv_how_to_drink;

    private LinearLayout tv_how_to_monitor_label;
    private LinearLayout ll_how_to_monitor;


    private Unbinder unbinder;

    private Disposable mDisposable;
    private AnimatorSet mAnimatorSet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        wave_view.setWaveColor(ContextCompat.getColor(wave_view.getContext(), R.color.colorPrimary),
                ContextCompat.getColor(wave_view.getContext(), R.color.colorPrimaryDark));
        wave_view.setShapeType(WaveView.ShapeType.SQUARE);
        wave_view.setShowWave(true);
        wave_view.setWaterLevelRatio(0);

        fl_avatar.post(() -> {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(Utils.slideVFadeIn(fl_avatar, 0.5f),
                    Utils.slideVFadeIn(tv_plan_generating, 0.5f));
            set.setDuration(400);
            set.start();
            animWaveView();

            animStep1();
        });
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        unbinder.unbind();
        super.onDestroyView();
    }

    private void animWaveView() {

        // horizontal animation.
        // wave waves infinitely.
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                wave_view, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(1000);
        waveShiftAnim.setInterpolator(new LinearInterpolator());

        // vertical animation.
        // water level increases from 0 to center of WaveView
        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                wave_view, "waterLevelRatio", 0f, 0.05f);
        waterLevelAnim.setDuration(2000);
        waterLevelAnim.setInterpolator(new DecelerateInterpolator());

        // amplitude animation.
        // wave grows big then grows small, repeatedly
        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
                wave_view, "amplitudeRatio", 0.01f, 0.02f);
        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnim.setDuration(5000);
        amplitudeAnim.setInterpolator(new LinearInterpolator());

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(waveShiftAnim, waterLevelAnim, amplitudeAnim);
        mAnimatorSet.start();
    }

    private void animStep1() {
        mDisposable = Single.just(true)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    mAnimatorSet.cancel();

                    v_fragment_guide_1 = vs_fragment_guide_1.inflate();
                    tv_daily_target = v_fragment_guide_1.findViewById(R.id.tv_daily_target);
                    tv_unit = v_fragment_guide_1.findViewById(R.id.tv_unit);

                    tv_daily_target.setText("2000");
                    tv_unit.setText("ml");

                    v_fragment_guide_1.setAlpha(0);
                    TransitionManager.go(Scene.getSceneForLayout(fl_scene, R.layout.vs_fragment_guide_0_scene, fl_scene.getContext()),
                            new TransitionSet().addTransition(new ChangeBounds()).addTransition(new Fade()));
                    ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
                            wave_view, "waterLevelRatio", 0.05f, 1f);
                    waterLevelAnim.setDuration(400);
                    waterLevelAnim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (fl_guide == null || wave_view == null) {
                                return;
                            }
                            fl_guide.setBackgroundColor(ContextCompat.getColor(fl_guide.getContext(), R.color.colorPrimaryDark));
                            wave_view.setVisibility(View.GONE);
                            ll_next_step.setVisibility(View.VISIBLE);
                            tv_step_indicator.setText("1/4");

                            Utils.slideVFadeIn(v_fragment_guide_1, 0.6f).start();

                            animStep2();
                        }
                    });
                    waterLevelAnim.start();
                });
    }


    private void animStep2() {
        mDisposable = Single.just(true)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {

                    v_fragment_guide_2 = vs_fragment_guide_2.inflate();
                    iv_daily_intake_cup = v_fragment_guide_2.findViewById(R.id.iv_daily_intake_cup);
                    tv_daily_intake_cup = v_fragment_guide_2.findViewById(R.id.tv_daily_intake_cup);
                    fl_daily_intake_times = v_fragment_guide_2.findViewById(R.id.fl_daily_intake_times);

                    iv_daily_intake_times = v_fragment_guide_2.findViewById(R.id.iv_daily_intake_times);
                    ll_daily_intake_times = v_fragment_guide_2.findViewById(R.id.ll_daily_intake_times);
                    tv_daily_intake_times = v_fragment_guide_2.findViewById(R.id.tv_daily_intake_times);

                    iv_alert_clock = v_fragment_guide_2.findViewById(R.id.iv_alert_clock);
                    iv_alert = v_fragment_guide_2.findViewById(R.id.iv_alert);

                    tv_how_to_drink_label = v_fragment_guide_2.findViewById(R.id.tv_how_to_drink_label);
                    tv_how_to_drink = v_fragment_guide_2.findViewById(R.id.tv_how_to_drink);

                    tv_how_to_monitor_label = v_fragment_guide_2.findViewById(R.id.tv_how_to_monitor_label);
                    ll_how_to_monitor = v_fragment_guide_2.findViewById(R.id.ll_how_to_monitor);

                    tv_daily_intake_cup.setText("100ml");
                    tv_how_to_drink.setText(getString(R.string.dk, "14", "100ml"));

                    fl_daily_intake_times.setScaleX(0);
                    fl_daily_intake_times.setScaleY(0);

                    iv_daily_intake_cup.setScaleX(10);
                    iv_daily_intake_cup.setScaleY(10);

                    tv_how_to_drink_label.setTranslationY(1000);

                    tv_daily_intake_times.setCharacterLists(TickerUtils.provideNumberList());
                    tv_daily_intake_times.setText("00");

                    AnimatorSet set = Utils.scaleIn(iv_daily_intake_cup, 10f);
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tv_daily_intake_cup.setVisibility(View.VISIBLE);
                            tv_next_step.setTextColor(ContextCompat.getColor(tv_next_step.getContext(), R.color.colorTextBlack));
                            tv_next_step.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_black, 0);
                            tv_step_indicator.setTextColor(ContextCompat.getColor(tv_next_step.getContext(), R.color.colorTextBlack));
                            tv_step_indicator.setText("2/4");

                            AnimatorSet set = new AnimatorSet();
                            set.playTogether(Utils.scaleIn(fl_daily_intake_times, 0f),
                                    Utils.slideVFadeIn(tv_how_to_drink_label, 0.6f));
                            set.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    tv_daily_intake_times.setText("14");
                                }
                            });
                            set.start();

                            animStep3();
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            vs_fragment_guide_1.setVisibility(View.GONE);
                            fl_guide.setBackgroundColor(ContextCompat.getColor(fl_guide.getContext(), android.R.color.transparent));
                        }
                    });
                    set.start();
                });
    }

    private void animStep3() {
        mDisposable = Single.just(true)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {

                    iv_daily_intake_cup.animate().scaleX(0.55f).scaleY(0.55f)
                            .translationY(DimenUtils.dipToPixels(16, iv_daily_intake_cup.getContext())).start();
                    fl_daily_intake_times.animate().scaleX(1.7f).scaleY(1.7f)
                            .translationX(-DimenUtils.dipToPixels(43, fl_daily_intake_times.getContext())).start();
                    iv_daily_intake_times.animate().rotation(0).start();
                    iv_alert_clock.animate().alpha(1f).start();
                    iv_alert.animate().alpha(1f).start();

                    ll_daily_intake_times.animate().alpha(0).start();
                    tv_daily_intake_cup.animate().alpha(0).start();

                    tv_how_to_monitor_label.setVisibility(View.VISIBLE);
                    tv_how_to_monitor_label.setTranslationX(1000);

                    tv_how_to_monitor_label.post(() -> {
                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(Utils.slideHOut(tv_how_to_drink_label, -2f),
                                Utils.slideHIn(tv_how_to_monitor_label, 2f));
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ll_daily_intake_times.setVisibility(View.INVISIBLE);
                                tv_daily_intake_cup.setVisibility(View.INVISIBLE);
                                tv_how_to_drink_label.setVisibility(View.INVISIBLE);

                                iv_alert.animate().rotation(30f).setInterpolator(new CycleInterpolator(4)).start();
                            }
                        });
                        set.start();
                    });

                    animStep4();
                });
    }

    private void animStep4() {
        mDisposable = Single.just(true)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {

                    ll_how_to_monitor.setVisibility(View.VISIBLE);
                    ll_how_to_monitor.setTranslationX(1000);

                    ll_how_to_monitor.post(() -> {
                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(Utils.slideHOut(iv_daily_intake_cup, -4f),
                                Utils.slideHOut(fl_daily_intake_times, -4f),
                                Utils.slideHOut(tv_how_to_monitor_label, -2f),
                                Utils.slideHIn(ll_how_to_monitor, 2f));
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                tv_how_to_monitor_label.setVisibility(View.INVISIBLE);
                            }
                        });
                        set.start();
                    });
                    animStep5();
                });
    }

    private void animStep5() {
        mDisposable = Single.just(true)
                .delay(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> startActivity(new Intent(v_fragment_guide_2.getContext(), MainActivity.class)));
    }
}
