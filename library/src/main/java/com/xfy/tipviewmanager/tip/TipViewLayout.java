package com.xfy.tipviewmanager.tip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.xfy.tipviewmanager.TipManager;

import java.util.ArrayList;

/**
 * Created by XiongFangyu on 2017/6/20.
 *
 * 承载{@link ITip} 的view
 * 放到Activity的根布局，故一个Activity实例只能绑定一个此view
 * {@link TipManager}
 */
public class TipViewLayout extends FrameLayout {
    private static final int MAX_CLICK_TIME = 200;
    private static final int MAX_CLICK_DIS = 100;

    private ArrayList<NormalTip> normalTips;

    private boolean handleEvent = false;

    private NormalTip touchedTip = null;
    private float downX, downY;
    private long downTime = 0;

    public TipViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public TipViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TipViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (normalTips != null) {
            for (NormalTip nt : normalTips) {
                if (nt != null) {
                    nt.draw(canvas);
                }
            }
        }
    }

    public void setHandleEvent(boolean handle) {
        handleEvent = handle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (normalTips == null || normalTips.isEmpty() || !handleEvent)
            return false;
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                downTime = SystemClock.uptimeMillis();
                boolean allHide = true;
                for (NormalTip nt : normalTips) {
                    if (nt != null && nt.isVisible()) {
                        allHide = false;
                        if (nt.isTouched(x, y)) {
                            touchedTip = nt;
                            break;
                        }
                    }
                }
                if (allHide)
                    return false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (touchedTip == null)
                    break;
                if (SystemClock.uptimeMillis() - downTime <= MAX_CLICK_TIME && isClick(x, y)) {
                    touchedTip.hide();
                    touchedTip = null;
                }
                break;
        }
        return handleEvent;
    }

    private boolean isClick(float x, float y) {
        float dx = x - downX;
        float dy = y - downY;
        return dx * dx + dy * dy <= MAX_CLICK_DIS * MAX_CLICK_DIS;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        if (drawable instanceof NormalTip) {
            invalidate();
            return;
        }
        super.invalidateDrawable(drawable);
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        if (who instanceof NormalTip) {
            postDelayed(what, when - SystemClock.uptimeMillis());
            return;
        }
        super.scheduleDrawable(who, what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        if (who instanceof NormalTip) {
            removeCallbacks(what);
            return;
        }
        super.unscheduleDrawable(who, what);
    }

    public void addNormalTip(ITip tip) {
        if (normalTips == null) {
            normalTips = new ArrayList<>();
        }
        NormalTip normalTip = (NormalTip) tip;
        normalTips.add(normalTip);
        normalTip.setCallback(this);
    }

    public void removeTip(ITip tip) {
        if (tip instanceof NormalTip) {
            NormalTip normalTip = (NormalTip) tip;
            if (normalTips != null && normalTips.contains(tip)) {
                normalTips.remove(tip);
            }
            normalTip.setCallback(null);
            normalTip.release();
        }
        invalidate();
    }

    public void removeAllTip() {
        if (normalTips != null) {
            normalTips.clear();
        }
        invalidate();
    }
}
