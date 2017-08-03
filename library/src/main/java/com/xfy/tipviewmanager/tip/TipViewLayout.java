package com.xfy.tipviewmanager.tip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
public class TipViewLayout extends FrameLayout implements LayoutListener<NormalTip> {
    private static final int MAX_CLICK_TIME = 200;
    private static final int MAX_CLICK_DIS = 100;

    private ArrayList<NormalTipInfo> normalTips;

    private boolean handleEvent = false;
    private boolean touchToHideAll = false;
    private boolean needNotfiyListener = true;

    private NormalTip touchedTip = null;
    private float downX, downY;
    private long downTime = 0;

    private int screenWidth;
    private int marginEdge;

    public TipViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public TipViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TipViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (normalTips != null) {
            for (NormalTipInfo nt : normalTips) {
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
        if (normalTips == null || normalTips.isEmpty())
            return false;
        if (touchToHideAll) {
            hideAllTip();
            return false;
        } else if (!handleEvent) {
            return false;
        }
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                downTime = SystemClock.uptimeMillis();
                boolean allHide = true;
                for (NormalTipInfo nt : normalTips) {
                    NormalTip tip = nt != null ? nt.tip : null;
                    if (tip != null && tip.isVisible()) {
                        allHide = false;
                        if (tip.isTouched(x, y)) {
                            touchedTip = tip;
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
                    touchedTip.hide(needNotfiyListener);
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

    public void setMarginEdge(int marginEdge) {
        this.marginEdge = marginEdge;
    }

    public void removeTip(ITip tip) {
        if (tip instanceof NormalTip) {
            NormalTip normalTip = (NormalTip) tip;
            NormalTipInfo tipInfo = new NormalTipInfo(normalTip);
            if (normalTips != null && normalTips.contains(tipInfo)) {
                normalTips.remove(tipInfo);
            }
            normalTip.release();
            normalTip.setCallback(null);
        }
        invalidate();
    }

    public void removeAllTip() {
        if (normalTips != null) {
            for (NormalTipInfo nti : normalTips) {
                NormalTip nt = nti != null ? nti.tip : null;
                if (nt != null) {
                    nt.release();
                    nt.setCallback(null);
                }
            }
            normalTips.clear();
        }
        invalidate();
    }

    public void hideAllTip() {
        if (normalTips != null) {
            for (NormalTipInfo nti : normalTips) {
                NormalTip tip = nti != null ? nti.tip : null;
                if (tip != null) {
                    tip.hide();
                }
            }
            invalidate();
        }
    }

    private void addTipInfo(NormalTipInfo info) {
        if (normalTips == null) {
            normalTips = new ArrayList<>();
        }
        normalTips.add(info);
    }

    public void addTip(ITip tip, Rect viewRect, @ITip.TriangleDirection int direction) {
        if (normalTips == null) {
            normalTips = new ArrayList<>();
        }
        if (tip instanceof NormalTip) {
            NormalTip nt = (NormalTip) tip;
            nt.setCallback(this);
            nt.setLayoutListener(this);
            NormalTipInfo tipInfo = new NormalTipInfo(nt, viewRect, direction);
            addTipInfo(tipInfo);
            initNormalTip(viewRect, nt, direction);
        }
    }

    /**
     * 当tip为{@link NormalTip}时，设置tip位置信息
     * 目前{@link IAdvancedTip}的实现为{@link AdvancedTip}，
     * 是{@link NormalTip}的子类
     * @param viewRect      view的位置信息
     * @param tip           普通tip，使用drawable展示
     * @param direction     三角形指向 see {@link ITip.Triangle}
     */
    private void initNormalTip(Rect viewRect, NormalTip tip, @ITip.TriangleDirection int direction) {
        final int needWidth = tip.getIntrinsicWidth();
        final int needHeight = tip.getIntrinsicHeight();
        final int tw = tip.getTriangleWidth();
        final int th = tip.getTriangleHeight();
        int left = 0, top = 0, right = 0, bottom = 0;
        int margin = 0;
        if (direction == ITip.Triangle.TOP || direction == ITip.Triangle.BOTTOM) {
            int centerX = viewRect.centerX();
            left = centerX - (needWidth >> 1);
            right = centerX + (needWidth >> 1);
            final int maxRight = screenWidth - marginEdge;
            if (left < marginEdge) {
                left = marginEdge;
                right = left + needWidth;
                if (right > maxRight)
                    right = maxRight;
            } else if (right > maxRight) {
                right = maxRight;
                left = right - needWidth;
                if (left < marginEdge)
                    left = marginEdge;
            }
            if (direction == ITip.Triangle.TOP) {
                top = viewRect.bottom;
            } else {
                top = viewRect.top - needHeight;
            }
            bottom = top + needHeight;
            margin = centerX - left - (tw >> 1);
        } else if (direction != ITip.Triangle.NONE) {
            int centerY = viewRect.centerY();
            top = centerY - (needHeight >> 1);
            top = top < 0 ? 0 : top;
            bottom = top + needHeight;
            if (direction == ITip.Triangle.LEFT) {
                left = viewRect.right;
            } else {
                left = viewRect.left - needWidth;
            }
            right = left + needWidth;
            margin = centerY - top - (th >> 1);
        } else {
            left = viewRect.centerX() - (needWidth >> 1);
            right = left + needWidth;
            top = viewRect.centerY() - (needHeight >> 1);
            bottom = top + needHeight;
        }
        tip.setTriangleMargin(margin);
        tip.setBounds(left, top, right, bottom);
    }

    @Override
    public void reqeustLayout(NormalTip tip) {
        if (normalTips != null) {
            NormalTipInfo info = new NormalTipInfo(tip);
            int index = normalTips.indexOf(info);
            if (index >= 0) {
                info = normalTips.get(index);
                initNormalTip(info.viewRect, info.tip, info.direction);
            }
        }
    }

    public void setTouchToHideAll(boolean touchToHideAll) {
        this.touchToHideAll = touchToHideAll;
    }

    public void setTouchHideNeedNotify(boolean notify) {
        needNotfiyListener = notify;
    }
}
