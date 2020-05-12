package com.lcjian.lib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.widget.TextView;

public class UnderLineTextView extends TextView {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PathEffect pathEffect = new DashPathEffect(new float[]{20, 4, 20, 4}, 0);

    private Path path = new Path();

    public UnderLineTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public UnderLineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnderLineTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(getCurrentTextColor());
        paint.setStyle(Style.STROKE);
        //设置线粗
        paint.setStrokeWidth(2);
        paint.setPathEffect(pathEffect);
        path.moveTo(0, this.getHeight() - 1);
        path.lineTo(this.getWidth(), this.getHeight() - 1);
        canvas.drawPath(path, paint);
    }
}
