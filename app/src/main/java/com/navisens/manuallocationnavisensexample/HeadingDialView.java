package com.navisens.manuallocationnavisensexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;

class Point2D {
    double x;
    double y;
    Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("{ %.2f, %.2f}",x,y );
    }
}

public class HeadingDialView extends View {
    private float dialAngle;
    private final Drawable headingDialDrawable;
    private final double innerTouchBounds = 60;
    private double outerTouchBounds = 160;
    private double lastFi;

    public HeadingDialView(Context context) {
        this(context, null);
    }
    public HeadingDialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public HeadingDialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headingDialDrawable = context.getResources().getDrawable(R.drawable.heading_dial, null);
        } else {
            headingDialDrawable = context.getResources().getDrawable(R.drawable.heading_dial);
        }
    }

    public interface DialListener {
        void onDial(double number);
    }
    private final List<DialListener> dialListeners = new ArrayList<DialListener>();
    // ...
    public void addDialListener(DialListener listener) {
        Log.v(getClass().getSimpleName(),"Adding listener");
        dialListeners.add(listener);
    }
    public void removeDialListener(DialListener listener) {
        Log.v(getClass().getSimpleName(),"Removing listener");
        dialListeners.remove(listener);
    }

    private void fireDialListenerEvent(double number) {
        // TODO fire dial event
//        Log.v(getClass().getSimpleName(),"Number of listeners: " + dialListeners.size());
        for (DialListener listener : dialListeners) {
            listener.onDial(number);
//            Log.v(getClass().getSimpleName(), "calling the onDial");
        }
//        Log.v(getClass().getSimpleName(), String.format("dial %.2f",number));
//        Toast.makeText(getContext(),String.format("dial %.2f",number),Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();
//        Log.v(getClass().getSimpleName(),String.format("availableWidth: %.2f availableHeight: %.2f",availableWidth,availableHeight));
        int x = getMeasuredWidth() / 2;
        int y = getMeasuredWidth() / 2;
        canvas.save();
        headingDialDrawable.setBounds(0, 0, this.getMeasuredWidth(), this.getMeasuredWidth());

        if (dialAngle != 0) {
            canvas.rotate(dialAngle, x, y);
        }
//        InsetDrawable insetDial = new InsetDrawable(headingDialDrawable,)
        headingDialDrawable.draw(canvas);
        canvas.restore();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point2D center = new Point2D(getMeasuredWidth() / 2,getMeasuredHeight() / 2);
        outerTouchBounds = center.x;
//        final float centerX = getMeasuredWidth() / 2;
//        final float centerY = getMeasuredHeight() / 2;
        Point2D touch = new Point2D(event.getX(), event.getY());
//        float x1 = event.getX();
//        float y1 = event.getY();
        Point2D displacementFromCenter = new Point2D(center.x - touch.x, center.y - touch.y);
//        float x = x0 - x1;
//        float y = y0 - y1;
        // use pythagorus
        double   distanceFromCenter = Math.sqrt(pow(displacementFromCenter.x,2) + pow(displacementFromCenter.y,2));
        double sinfi = displacementFromCenter.y / distanceFromCenter;
        double fi = Math.toDegrees(Math.asin(sinfi));
        if (touch.x > center.x && center.y > touch.y) {
            fi = 180 - fi;
        } else if (touch.x > center.x && touch.y > center.y) {
            fi = 180 - fi;
        } else if (center.x > touch.x && touch.y > center.y) {
            fi += 360;
        }
//        Log.v(getClass().getSimpleName(),String.format("center: %s touch: %s displace: %s distance %.2f fi: %.2f lastfi: %.2f sinfi: %.2f",center,touch,displacementFromCenter,distanceFromCenter, fi, lastFi, sinfi));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (distanceFromCenter > innerTouchBounds && distanceFromCenter < outerTouchBounds) {
                    dialAngle += fi - lastFi;
                    dialAngle %= 360;
                    lastFi = fi;
                    final float angle = dialAngle % 360;
                    fireDialListenerEvent(angle);
//                    Log.v(getClass().getSimpleName(), "Dial Value: " + angle + " Width: " + getMeasuredWidth() + " Height: " + getMeasuredHeight());
                    invalidate();
                    return true;
                }
            case MotionEvent.ACTION_DOWN:
//                dialAngle = 0;
                lastFi = fi;
                return true;
            case MotionEvent.ACTION_UP:
//                final float angle = dialAngle % 360;
//                fireDialListenerEvent(angle);
//                Log.v(getClass().getSimpleName(), "Dial Value: " + angle + " Width: " + getMeasuredWidth() + " Height: " + getMeasuredHeight());
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}

