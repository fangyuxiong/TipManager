package com.xfy.tipviewmanager.tip;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;

/**
 * Created by XiongFangyu on 2017/7/8.
 *
 * 高级tip，可以修改文字显示方式，比如替换为图片或者带有前景色等等
 * 问题：
 *  若text里增加图片，文字宽度可能计算不准确，导致文字不能完全渲染或留大片空白
 */
public class AdvancedTextDrawable extends TextDrawable {

    private StaticLayout layout;

    public AdvancedTextDrawable() {
        super();
    }

    @Override
    public void setText(CharSequence text) {
        if (isTextSame(this.text, text))
            return;
        if (text == null) {
            this.text = null;
            layout = null;
            return;
        }
        this.text = text;
        layout = null;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (TextUtils.isEmpty(text))
            return;
        Rect bounds = getBounds();
        if (layout == null) {
            layout = new StaticLayout(text, textPaint, bounds.width(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            layoutSelf();
            return;
        }
        canvas.save();
        canvas.clipRect(bounds);
        canvas.translate(bounds.left, bounds.top);
        layout.draw(canvas);
        canvas.restore();
    }

    @Override
    public int getIntrinsicHeight() {
        if (layout == null)
            return super.getIntrinsicHeight();
        return layout.getHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return super.getIntrinsicWidth();
    }
}
