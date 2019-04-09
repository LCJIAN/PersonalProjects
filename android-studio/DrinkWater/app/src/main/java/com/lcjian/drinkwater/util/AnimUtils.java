package com.lcjian.drinkwater.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class AnimUtils {

    public static ObjectAnimator slideHIn(View view, float percent) {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth() * percent, 0);
    }

    public static ObjectAnimator slideHOut(View view, float percent) {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, view.getWidth() * percent);
    }

    public static AnimatorSet slideHFadeIn(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth() * percent, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    public static AnimatorSet slideHFadeOut(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, view.getWidth() * percent);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    public static AnimatorSet slideVFadeIn(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getHeight() * percent, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    public static AnimatorSet slideVFadeOut(View view, float percent) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, view.getHeight() * percent);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorX).with(animatorAlpha);
        return animatorSet;
    }

    public static AnimatorSet scaleIn(View view, float percent) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, percent, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, percent, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY);
        return animatorSet;
    }
}
