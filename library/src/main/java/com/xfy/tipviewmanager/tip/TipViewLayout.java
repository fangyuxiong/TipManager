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

    private ArrayList<NormalTip> normalTips;

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
