package kz.mobdev.mapview.library.layers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import kz.mobdev.mapview.library.MapView;

/**
 * BitmapLayer
 *
 * @author: onlylemi
 */
public class BitmapLayer extends MapBaseLayer {

    private PointF location;
    private Bitmap bitmap;
    private Paint paint;

    private boolean autoScale = false;

    private OnBitmapClickListener onBitmapClickListener;

    public BitmapLayer(MapView mapView, Bitmap bitmap) {
        this(mapView, bitmap, null);
    }

    public BitmapLayer(MapView mapView, Bitmap bitmap, PointF location) {
        super(mapView);
        this.location = location;
        this.bitmap = bitmap;

        paint = new Paint();
    }

    @Override
    public void onTouch(MotionEvent event) {
        if (onBitmapClickListener != null) {
            float currentZoom = mapView.getCurrentZoom();
            float[] goal = mapView.convertMapXYToScreenXY(event.getX(), event.getY());
            goal[0] = goal[0] * currentZoom;
            goal[1] = goal[1] * currentZoom;
            float[] xy = new float[2];
            xy[0] = location.x * currentZoom;
            xy[1] = location.y * currentZoom;
            if (goal[0] > xy[0] - bitmap.getWidth() / 2f
                    && goal[0] < xy[0] + bitmap.getWidth() / 2f
                    && goal[1] > xy[1] - bitmap.getHeight() / 2f
                    && goal[1] < xy[1] + bitmap.getHeight() / 2f) {
                Log.i("BitmapLayer", "goal: " + goal[0] + ", " + goal[1]
                        + " location:" + xy[0] + ", " + xy[1]
                        + " bitmap:" + bitmap.getWidth() + ", " + bitmap.getHeight()
                        + " currentZoom:" + currentZoom);
                onBitmapClickListener.onBitmapClick(this);
            }
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix currentMatrix, float currentZoom, float
            currentRotateDegrees) {
        if (isVisible && bitmap != null) {
            canvas.save();
            float[] goal = {location.x, location.y};
            if (!autoScale) {
                currentMatrix.mapPoints(goal);
            } else {
                canvas.setMatrix(currentMatrix);
            }
            canvas.drawBitmap(bitmap, goal[0] - bitmap.getWidth() / 2f,
                    goal[1] - bitmap.getHeight() / 2f, paint);
            canvas.restore();
        }
    }

    public PointF getLocation() {
        return location;
    }

    public void setLocation(PointF location) {
        this.location = location;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setOnBitmapClickListener(OnBitmapClickListener onBitmapClickListener) {
        this.onBitmapClickListener = onBitmapClickListener;
    }

    public interface OnBitmapClickListener {
        void onBitmapClick(BitmapLayer layer);
    }
}
