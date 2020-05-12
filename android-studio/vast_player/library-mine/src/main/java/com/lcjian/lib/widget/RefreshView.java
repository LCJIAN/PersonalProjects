package com.lcjian.lib.widget;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * A simple text label view that can be applied as a "refresh" to any given {@link View}.
 * This class is intended to be instantiated at runtime rather than included in XML layouts.
 *
 * @author LCJIAN
 */
public class RefreshView {

    public static final int POSITION_TOP_LEFT = 1;
    public static final int POSITION_TOP_RIGHT = 2;
    public static final int POSITION_BOTTOM_LEFT = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;
    public static final int POSITION_CENTER = 5;

    private static final int DEFAULT_MARGIN_DIP = 5;
    private static final int DEFAULT_POSITION = POSITION_CENTER;

    private static Animation fadeIn;
    private static Animation fadeOut;

    private View refresh;
    private View target;

    private int refreshPosition;
    private int refreshMarginH;
    private int refreshMarginV;

    public RefreshView(View target) {
        init(new ProgressBar(target.getContext()), target);
    }

    public RefreshView(View refresh, View target) {
        init(refresh, target);
    }

    private void init(View refresh, View target) {

        this.refresh = refresh;
        this.target = target;

        // apply defaults
        refreshPosition = DEFAULT_POSITION;
        refreshMarginH = dipToPixels(DEFAULT_MARGIN_DIP);
        refreshMarginV = refreshMarginH;

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(200);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(200);

        if (this.target != null) {
            applyTo(this.target);
        } else {
            show();
        }
    }

    private void applyTo(View target) {
        LayoutParams lp = target.getLayoutParams();
        ViewParent parent = target.getParent();
        FrameLayout container = new FrameLayout(target.getContext());

        ViewGroup group = (ViewGroup) parent;
        int index = group.indexOfChild(target);

        group.removeView(target);
        group.addView(container, index, lp);

        container.addView(target);

        refresh.setVisibility(View.GONE);
        container.addView(refresh);

        group.invalidate();
    }

    /**
     * Make the refresh visible in the UI.
     */
    public void show() {
        show(false, null);
    }

    /**
     * Make the refresh visible in the UI.
     *
     * @param animate flag to apply the default fade-in animation.
     */
    public void show(boolean animate) {
        show(animate, fadeIn);
    }

    /**
     * Make the refresh visible in the UI.
     *
     * @param anim Animation to apply to the view when made visible.
     */
    public void show(Animation anim) {
        show(true, anim);
    }

    /**
     * Make the refresh non-visible in the UI.
     */
    public void hide() {
        hide(false, null);
    }

    /**
     * Make the refresh non-visible in the UI.
     *
     * @param animate flag to apply the default fade-out animation.
     */
    public void hide(boolean animate) {
        hide(animate, fadeOut);
    }

    /**
     * Make the refresh non-visible in the UI.
     *
     * @param anim Animation to apply to the view when made non-visible.
     */
    public void hide(Animation anim) {
        hide(true, anim);
    }

    /**
     * Toggle the refresh visibility in the UI.
     */
    public void toggle() {
        toggle(false, null, null);
    }

    /**
     * Toggle the refresh visibility in the UI.
     *
     * @param animate flag to apply the default fade-in/out animation.
     */
    public void toggle(boolean animate) {
        toggle(animate, fadeIn, fadeOut);
    }

    /**
     * Toggle the refresh visibility in the UI.
     *
     * @param animIn  Animation to apply to the view when made visible.
     * @param animOut Animation to apply to the view when made non-visible.
     */
    public void toggle(Animation animIn, Animation animOut) {
        toggle(true, animIn, animOut);
    }

    private void show(boolean animate, Animation anim) {
        applyLayoutParams();
        if (animate) {
            refresh.startAnimation(anim);
        }
        refresh.setVisibility(View.VISIBLE);
    }

    private void hide(boolean animate, Animation anim) {
        refresh.setVisibility(View.GONE);
        if (animate) {
            refresh.startAnimation(anim);
        }
    }

    private void toggle(boolean animate, Animation animIn, Animation animOut) {
        if (refresh.isShown()) {
            hide(animate && (animOut != null), animOut);
        } else {
            show(animate && (animIn != null), animIn);
        }
    }

    private void applyLayoutParams() {

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        switch (refreshPosition) {
            case POSITION_TOP_LEFT:
                lp.gravity = Gravity.LEFT | Gravity.TOP;
                lp.setMargins(refreshMarginH, refreshMarginV, 0, 0);
                break;
            case POSITION_TOP_RIGHT:
                lp.gravity = Gravity.RIGHT | Gravity.TOP;
                lp.setMargins(0, refreshMarginV, refreshMarginH, 0);
                break;
            case POSITION_BOTTOM_LEFT:
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                lp.setMargins(refreshMarginH, 0, 0, refreshMarginV);
                break;
            case POSITION_BOTTOM_RIGHT:
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                lp.setMargins(0, 0, refreshMarginH, refreshMarginV);
                break;
            case POSITION_CENTER:
                lp.gravity = Gravity.CENTER;
                lp.setMargins(0, 0, 0, 0);
                break;
            default:
                break;
        }

        refresh.setLayoutParams(lp);
    }

    /**
     * Returns the target View this refresh has been attached to.
     */
    public View getTarget() {
        return target;
    }

    /**
     * Is this refresh currently visible in the UI?
     */
    public boolean isShown() {
        return refresh.isShown();
    }

    /**
     * Returns the positioning of this refresh.
     * <p>
     * one of POSITION_TOP_LEFT, POSITION_TOP_RIGHT, POSITION_BOTTOM_LEFT, POSITION_BOTTOM_RIGHT, POSTION_CENTER.
     */
    public int getBadgePosition() {
        return refreshPosition;
    }

    /**
     * Set the positioning of this refresh.
     *
     * @param layoutPosition one of POSITION_TOP_LEFT, POSITION_TOP_RIGHT, POSITION_BOTTOM_LEFT, POSITION_BOTTOM_RIGHT, POSTION_CENTER.
     */
    public void setBadgePosition(int layoutPosition) {
        this.refreshPosition = layoutPosition;
    }

    /**
     * Returns the horizontal margin from the target View that is applied to this refresh.
     */
    public int getHorizontalBadgeMargin() {
        return refreshMarginH;
    }

    /**
     * Returns the vertical margin from the target View that is applied to this refresh.
     */
    public int getVerticalBadgeMargin() {
        return refreshMarginV;
    }

    /**
     * Set the horizontal/vertical margin from the target View that is applied to this refresh.
     *
     * @param refreshMargin the margin in pixels.
     */
    public void setBadgeMargin(int refreshMargin) {
        this.refreshMarginH = refreshMargin;
        this.refreshMarginV = refreshMargin;
    }

    /**
     * Set the horizontal/vertical margin from the target View that is applied to this refresh.
     *
     * @param horizontal margin in pixels.
     * @param vertical   margin in pixels.
     */
    public void setBadgeMargin(int horizontal, int vertical) {
        this.refreshMarginH = horizontal;
        this.refreshMarginV = vertical;
    }

    private int dipToPixels(int dip) {
        Resources r = target.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return (int) px;
    }
}
