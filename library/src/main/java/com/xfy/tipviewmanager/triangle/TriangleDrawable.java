package com.xfy.tipviewmanager.triangle;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by XiongFangyu on 2017/6/26.
 *
 * 默认三角形基类
 */
public abstract class TriangleDrawable extends Drawable {
    private static final int WIDTH = 60;
    private static final int HEIGHT = 30;
    private Paint mPaint;
    private CS cs;

    public TriangleDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        cs = newCs();
    }

    protected TriangleDrawable(CS cs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        this.cs = cs;
        setColor(cs.color);
    }

    protected abstract CS newCs();

    protected abstract float getRotateDegree();

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        cs.initPath(bounds);
        canvas.save();
        canvas.drawPath(cs.path, mPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    public TriangleDrawable setColor(int color) {
        mPaint.setColor(color);
        cs.color = color;
        return this;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public int getIntrinsicWidth() {
        int degree = (int) getRotateDegree();
        if (degree == 0 || degree == 180) {
            return WIDTH;
        }
        return HEIGHT;
    }

    public int getIntrinsicHeight() {
        int degree = (int) getRotateDegree();
        if (degree == 0 || degree == 180) {
            return HEIGHT;
        }
        return WIDTH;
    }

    public @Nullable ConstantState getConstantState() {
        return cs;
    }

    protected abstract static class CS extends ConstantState {
        protected Path path;
        private int color;
        public CS() {
            if (path == null)
                path = new Path();
        }

        private void initPath(Rect rect) {
            path.reset();
            int[] points = getPoint(rect);
            path.moveTo(points[0], points[1]);
            path.lineTo(points[2], points[3]);
            path.lineTo(points[4], points[5]);
            path.close();
        }

        protected abstract int[] getPoint(Rect rect);

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
