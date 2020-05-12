package com.lcjian.lib;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {

    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        ViewCompat.setTranslationY(child, Math.abs(dependency.getTop() * (child.getHeight() + ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin) / dependency.getHeight()));
        return true;
    }
}
