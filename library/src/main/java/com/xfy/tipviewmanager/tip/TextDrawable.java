package com.xfy.tipviewmanager.tip;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;

/**
 * Created by XiongFangyu on 2017/5/3.
 *
 * 显示普通文字drawable
 */
public class TextDrawable extends Drawable {

    protected CharSequence text;
    protected TextPaint textPaint;

    private float translateX, translateY;
    private float scale = 1;

    private LayoutListener layoutListener;

    public TextDrawable() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (TextUtils.isEmpty(text))
            return;
        Rect bounds = getBounds();
        final float left = getTextLeft();
        final float y = getTextY();
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale, bounds.centerX(), bounds.centerY());
        canvas.drawText(text.toString(), left, y, textPaint);
        canvas.restore();
    }

    public float getTextLeft() {
        Rect bounds = getBounds();
        final float len = getIntrinsicWidth();
        return bounds.left + (bounds.width() - len) / 2;
    }

    public float getTextY() {
        Rect bounds = getBounds();
        final Paint.FontMetrics metrics = textPaint.getFontMetrics();
        final float height = metrics.bottom - metrics.top;
        return height / 2 - metrics.bottom + bounds.centerY();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        if (textPaint != null)
            textPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (textPaint != null)
            textPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public void setBounds(int l, int t, int r, int b) {
        super.setBounds(l, t, r, b);
    }

    public void setText(CharSequence text) {
        if (isTextSame(this.text, text))
            return;
        this.text = text;
        invalidateSelf();
    }

    public CharSequence getText() {
        return text;
    }

    public void setTextSize(float px) {
        if (textPaint != null)
            textPaint.setTextSize(px);
        invalidateSelf();
    }

    public void setTextColor(int color) {
        if (textPaint != null)
            textPaint.setColor(color);
        invalidateSelf();
    }

    public void setShader(Shader shader) {
        if (textPaint != null)
            textPaint.setShader(shader);
        invalidateSelf();
    }

    public void setXfermode(Xfermode xfermode) {
        if (textPaint != null)
            textPaint.setXfermode(xfermode);
        invalidateSelf();
    }

    public void setStyle(Paint.Style style) {
        if (textPaint != null)
            textPaint.setStyle(style);
        invalidateSelf();
    }

    public Paint getPaint() {
        return textPaint;
    }

    public float getTranslateX() {
        return translateX;
    }

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
        invalidateSelf();
    }

    public float getTranslateY() {
        return translateY;
    }

    public void setTranslateY(float translateY) {
        this.translateY = translateY;
        invalidateSelf();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidateSelf();
    }

    public
    @IntRange(from = 0, to = 255)
    int getAlpha() {
        if (textPaint != null)
            return textPaint.getAlpha();
        return 255;
    }

    public int getWidth() {
        return getBounds().width();
    }

    public int getHeight() {
        return getBounds().height();
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) getMeasureTextHeight();
    }

    public int getIntrinsicWidth() {
        return (int) getMeasureTextWidth();
    }

    public void setLayoutListener(LayoutListener layoutListener) {
        this.layoutListener = layoutListener;
    }

    protected void layoutSelf() {
        if (layoutListener != null) {
            layoutListener.reqeustLayout(this);
        }
    }

    private float getMeasureTextWidth() {
        if (TextUtils.isEmpty(text))
            return 0;
        return textPaint.measureText(text.toString());
    }

    private float getMeasureTextHeight() {
        final Paint.FontMetrics metrics = textPaint.getFontMetrics();
        final float height = metrics.bottom - metrics.top;
        return height;
    }

    protected boolean isTextSame(CharSequence t1, CharSequence t2) {
        if (t1 == null && t2 == null)
            return true;
        if (t1 != null && t1.equals(t2))
            return true;
        return false;
    }
}
