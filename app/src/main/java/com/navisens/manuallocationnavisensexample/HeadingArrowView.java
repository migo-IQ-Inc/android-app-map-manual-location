package com.navisens.manuallocationnavisensexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class HeadingArrowView extends View {
    private float dialAngle = 0;
    private Paint mPaintColor;
    private Paint mLinePaintColor;
    public HeadingArrowView(Context context) {
        this(context, null);
    }
    public HeadingArrowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public HeadingArrowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        RectShape line = new RectShape();
        mPaintColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (Build.VERSION.SDK_INT >= 23) {
            mPaintColor.setColor(getResources().getColor(R.color.navired, null));
        } else {
            mPaintColor.setColor(getResources().getColor(R.color.navired));
        }
        mPaintColor.setStrokeWidth(8.0f);
        mPaintColor.setStrokeCap(Paint.Cap.ROUND);

        mLinePaintColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaintColor.setStrokeWidth(8.0f);
        if (Build.VERSION.SDK_INT >= 23) {
            mLinePaintColor.setColor(getResources().getColor(R.color.navired,null));
        } else {
            mLinePaintColor.setColor(getResources().getColor(R.color.navired));
        }

        mLinePaintColor.setStyle(Paint.Style.STROKE);
        mLinePaintColor.setPathEffect(new DashPathEffect(new float[]{22,16},0));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = getMeasuredWidth() / 2;
        int y = getMeasuredWidth() / 2;
        canvas.save();
        canvas.drawLine((float)x,5,(float)(x-30),35.0f, mPaintColor);
        canvas.drawLine((float)x,5,(float)(x+30),35.0f, mPaintColor);
        canvas.drawLine((float)x,7,(float)(x+3),getHeight()-4, mLinePaintColor);
        if (dialAngle != 0) {
            canvas.rotate(dialAngle, x, y);
        }
        canvas.restore();
    }
}

