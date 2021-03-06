package com.xfy.tipviewmanager.tip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Transformation;

import com.xfy.tipviewmanager.anim.ITipAnimation;

/**
 * Created by XiongFangyu on 2017/6/20.
 *
 * tip普通实现
 */
public class NormalTip extends Drawable implements ITip, Touchable, ValueAnimator.AnimatorUpdateListener, LayoutListener<TextDrawable> {

    private Drawable background;
    private TextDrawable textDrawable;
    private Drawable triangle;
    private Rect paddingRect;
    private int triangleMargin;

    private boolean needAnim = true;
    private
    @TriangleDirection
    int direction;
    private ValueAnimator animator;
    private long showAnimTime = SHOW_ANIMATION_DURATION;
    private long hideAnimTime = HIDE_ANIMATION_DURATION;

    private OnTipHideListener onTipHideListener;

    private Transformation mTransformation;
    private ITipAnimation tipAnimation;
    private LayoutListener layoutListener;

    private boolean inHidingAnim = false;
    private boolean needNotifyListener = true;

    private float translateX, translateY;

    public NormalTip() {
        textDrawable = newTextDrawable();
        paddingRect = new Rect();
        mTransformation = new Transformation();
        textDrawable.setLayoutListener(this);
    }

    protected TextDrawable newTextDrawable() {
        return new TextDrawable();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        final int measureWidth = bounds.width();
        final int measureHeight = bounds.height();

        final int needWidth = textDrawable.getIntrinsicWidth();
        final int needHeight = textDrawable.getIntrinsicHeight();

        final int triangleWidth = triangle != null ? triangle.getIntrinsicWidth() : 0;
        final int triangleHeight = triangle != null ? triangle.getIntrinsicHeight() : 0;

        int maxTextWidth = measureWidth - paddingRect.left - paddingRect.right;
        int maxTextHeight = measureHeight - paddingRect.top - paddingRect.bottom;

        final Rect baseRect = new Rect(bounds);

        switch (direction) {
            case Triangle.LEFT:
                if (triangle != null) {
                    int top = baseRect.top + triangleMargin;
                    triangle.setBounds(baseRect.left, top, baseRect.left + triangleWidth, top + triangleHeight);
                }
                baseRect.left += triangleWidth;
                maxTextWidth -= triangleWidth;
                break;
            case Triangle.TOP:
                if (triangle != null) {
                    int left = baseRect.left + triangleMargin;
                    triangle.setBounds(left, baseRect.top, left + triangleWidth, baseRect.top + triangleHeight);
                }
                baseRect.top += triangleHeight;
                maxTextHeight -= triangleHeight;
                break;
            case Triangle.RIGHT:
                if (triangle != null) {
                    int top = baseRect.top + triangleMargin;
                    triangle.setBounds(baseRect.right - triangleWidth, top, baseRect.right, top + triangleHeight);
                }
                maxTextWidth -= triangleWidth;
                break;
            case Triangle.BOTTOM:
                if (triangle != null) {
                    int left = baseRect.left + triangleMargin;
                    triangle.setBounds(left, baseRect.bottom - triangleHeight, left + triangleWidth, baseRect.bottom);
                }
                maxTextHeight -= triangleHeight;
                break;
            default:        //NONE
                if (triangle != null) {
                    triangle.setBounds(0, 0, 0, 0);
                }
                break;
        }
        final int textWidth = needWidth > 0 ? Math.min(needWidth, maxTextWidth) : maxTextWidth;
        final int textHeight = Math.min(needHeight, maxTextHeight);
        baseRect.left += paddingRect.left;
        baseRect.top += paddingRect.top;
        baseRect.right = baseRect.left + textWidth;
        baseRect.bottom = baseRect.top + textHeight;
        textDrawable.setBounds(baseRect);

        baseRect.left -= paddingRect.left;
        baseRect.top -= paddingRect.top;
        baseRect.right += paddingRect.right;
        baseRect.bottom += paddingRect.bottom;
        if (background != null) {
            background.setBounds(baseRect);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (isVisible()) {
            canvas.save();
            canvas.concat(mTransformation.getMatrix());
            canvas.translate(translateX, translateY);
            if (background != null) {
                background.draw(canvas);
            }
            if (textDrawable != null) {
                textDrawable.draw(canvas);
            }
            if (triangle != null) {
                triangle.draw(canvas);
            }
            canvas.restore();
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        if (background != null) {
            background.setAlpha(alpha);
        }
        if (textDrawable != null) {
            textDrawable.setAlpha(alpha);
        }
        if (triangle != null) {
            triangle.setAlpha(alpha);
        }
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (background != null) {
            background.setColorFilter(colorFilter);
        }
        if (textDrawable != null) {
            textDrawable.setColorFilter(colorFilter);
        }
        if (triangle != null) {
            triangle.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public ITip setNeedAnimation(boolean need) {
        needAnim = need;
        return this;
    }

    @Override
    public ITip setTipAnimation(ITipAnimation tipAnimation) {
        this.tipAnimation = tipAnimation;
        return this;
    }

    @Override
    public ITip setAnimationTime(long showTime, long hideTime) {
        //用于动画结束后判断是隐藏动画还是显示动画，目前暂无别的好方法
        if (showTime == hideTime)
            hideTime++;
        showAnimTime = showTime;
        hideAnimTime = hideTime;
        return this;
    }

    @Override
    public ITip show() {
        setVisible(true, false);
        inHidingAnim = false;
        if (needAnim) {
            initAnim(true);
            mTransformation.clear();
            animator.start();
        }
        return this;
    }

    void release() {
        unscheduleSelf(hideTask);
        layoutListener = null;
        onTipHideListener = null;
        if (textDrawable != null)
            textDrawable.setLayoutListener(null);
    }

    private void initAnim(boolean show) {
        if (animator == null) {
            animator = new ValueAnimator();
            animator.setInterpolator(null);
            animator.addUpdateListener(this);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animation.getDuration() == hideAnimTime) {
                        setVisible(false, false);
                        notifyHideListener();
                    }
                }
            });
        }
        if (show) {
            animator.setDuration(showAnimTime).setFloatValues(0, 1);
        } else {
            animator.setDuration(hideAnimTime).setFloatValues(1, 0);
        }
    }

    @Override
    public ITip hide() {
        return hide(true);
    }

    @Override
    public ITip hide(boolean needNotify) {
        if (isVisible() && !inHidingAnim) {
            needNotifyListener = needNotify;
            if (needAnim) {
                initAnim(false);
                inHidingAnim = true;
                animator.start();
            } else {
                setVisible(false, false);
                notifyHideListener();
            }
        }
        return this;
    }

    @Override
    public ITip autoHide(long delay) {
        if (delay > 0) {
            scheduleSelf(hideTask, delay + SystemClock.uptimeMillis());
        }
        return this;
    }

    @Override
    public boolean isShowing() {
        return isVisible();
    }

    @Override
    public ITip setTipText(CharSequence text) {
        if (text != null) {
            textDrawable.setText(text);
        } else {
            textDrawable.setText(null);
        }
        invalidateSelf();
        return this;
    }

    @Override
    public ITip setTipTextSize(float size) {
        textDrawable.setTextSize(size);
        invalidateSelf();
        return this;
    }

    @Override
    public ITip setTipTextColor(@ColorInt int color) {
        textDrawable.setTextColor(color);
        invalidateSelf();
        return this;
    }

    @Override
    public ITip setTipTextPadding(int pl, int pt, int pr, int pb) {
        paddingRect.set(pl, pt, pr, pb);
        return this;
    }

    @Override
    public ITip setTriangleMargin(int margin) {
        triangleMargin = margin;
        Rect bounds = getBounds();
        if (!bounds.isEmpty()) {
            onBoundsChange(bounds);
            invalidateSelf();
        }
        return this;
    }

    @Override
    public ITip setTipBackgroundDrawable(Drawable drawable) {
        background = drawable;
        invalidateSelf();
        return this;
    }

    @Override
    public ITip setTriangleDrawable(Drawable drawable) {
        triangle = drawable;
        invalidateSelf();
        return this;
    }

    @Override
    public ITip setTriangleDirection(@TriangleDirection int direction) {
        this.direction = direction;
        invalidateSelf();
        return this;
    }

    @Override
    public ITip setOnTipHideListener(OnTipHideListener onTipHideListener) {
        this.onTipHideListener = onTipHideListener;
        return this;
    }

    @Override
    public ITip setTranslateXY(float x, float y) {
        translateX = x;
        translateY = y;
        invalidateSelf();
        return this;
    }

    private Runnable hideTask = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (tipAnimation != null) {
            final float v = (float) animation.getAnimatedValue();
            tipAnimation.applyTransformation(v, mTransformation);
            setAlpha((int) (mTransformation.getAlpha() * 255));
            invalidateSelf();
        }
    }

    @Override
    public int getIntrinsicWidth() {
        final int textWidth = textDrawable.getIntrinsicWidth() + paddingRect.left + paddingRect.right;
        if (direction == Triangle.LEFT || direction == Triangle.RIGHT) {
            return textWidth + getTriangleWidth();
        }
        return textWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        final int textHeight = textDrawable.getIntrinsicHeight() + paddingRect.top + paddingRect.bottom;
        if (direction == Triangle.TOP || direction == Triangle.BOTTOM) {
            return textHeight + getTriangleHeight();
        }
        return textHeight;
    }

    public void setLayoutListener(LayoutListener listener) {
        this.layoutListener = listener;
    }

    protected void layoutSelf() {
        if (layoutListener != null) {
            layoutListener.reqeustLayout(this);
        }
    }

    public int getTriangleWidth() {
        return triangle != null ? triangle.getIntrinsicWidth() : 0;
    }

    public int getTriangleHeight() {
        return triangle != null ? triangle.getIntrinsicHeight() : 0;
    }

    private void notifyHideListener() {
        if (!needNotifyListener)
            return;
        if (onTipHideListener != null) {
            onTipHideListener.onHide(this);
        }
    }

    @Override
    public boolean isTouched(float x, float y) {
        return getBounds().contains((int) x, (int) y);
    }

    @Override
    public void reqeustLayout(TextDrawable obj) {
        if (obj == textDrawable)
            layoutSelf();
    }
}
