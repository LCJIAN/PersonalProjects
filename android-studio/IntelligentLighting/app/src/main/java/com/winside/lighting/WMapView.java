package com.winside.lighting;

import android.content.Context;
import android.util.AttributeSet;

import kz.mobdev.mapview.library.MapView;

public class WMapView extends MapView {

    public WMapView(Context context) {
        super(context);
    }

    public WMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean withFloorPlan(float x, float y) {
        return true;
    }
}
