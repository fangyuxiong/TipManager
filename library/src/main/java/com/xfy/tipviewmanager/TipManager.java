package com.xfy.tipviewmanager;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.xfy.tipviewmanager.triangle.BottomTriangleDrawable;
import com.xfy.tipviewmanager.triangle.LeftTriangleDrawable;
import com.xfy.tipviewmanager.triangle.RightTriangleDrawable;
import com.xfy.tipviewmanager.triangle.TopTriangleDrawable;

import java.util.HashMap;

/**
 * Created by XiongFangyu on 2017/6/21.
 */
public class TipManager {

    private static HashMap<Activity, TipManager> Pool;

    public static TipManager bindActivity(Activity activity) {
        TipManager tipManager = findTipManager(activity);
        if (tipManager != null) {
            return tipManager;
        }
        ViewGroup container = (ViewGroup) activity.findViewById(android.R.id.content);
        TipViewLayout tipViewLayout = new TipViewLayout(activity);
        container.addView(tipViewLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        tipManager = new TipManager(tipViewLayout);
        tipManager.initActivityParams(activity);
        saveToPool(activity, tipManager);
        return tipManager;
    }

    public static void unbindActivity(Activity activity) {
        if (Pool != null) {
            TipManager tipManager = Pool.remove(activity);
            if (tipManager != null) {
                tipManager.release();
            }
        }
    }

    private static TipManager findTipManager(Activity activity) {
        if (Pool != null) {
            return Pool.get(activity);
        }
        return null;
    }

    private static void saveToPool(Activity activity, TipManager tipManager) {
        if (Pool == null)
            Pool = new HashMap<>();
        Pool.put(activity, tipManager);
    }

    private TipViewLayout tipViewLayout;
    private HashMap<View, ITip> tips;
    private float textSize;
    private int textColor;
    private Rect textPadding;
    private Drawable background;
    private Drawable[] triangles;
    private int statusHeight = -404;
    private int screenWidth, screenHeight;
    private int marginEdge = 10;
    private boolean needTipAnim = true;
    private ITipAnimation tipAnimation = new DefaultTipAnimation();

    public TipManager(TipViewLayout tipViewLayout) {
        this.tipViewLayout = tipViewLayout;
        Resources res = tipViewLayout.getResources();
        setBackground(res.getDrawable(R.drawable.tip_background));
        final int dtc = res.getColor(R.color.default_tip_color);
        setTriangles(new LeftTriangleDrawable().setColor(dtc),
                new TopTriangleDrawable().setColor(dtc),
                new RightTriangleDrawable().setColor(dtc),
                new BottomTriangleDrawable().setColor(dtc));
        setTextColor(res.getColor(R.color.default_tip_text_color));
        setTextSize(res.getDimensionPixelSize(R.dimen.default_tip_text_size));
        final int defaultTextPadding = res.getDimensionPixelOffset(R.dimen.default_tip_text_padding);
        setTextPadding(defaultTextPadding, defaultTextPadding, defaultTextPadding, defaultTextPadding);
    }

    public boolean isTipShowing(View targetView) {
        ITip tip = findTip(targetView);
        if (tip != null)
            return tip.isShowing();
        return false;
    }

    public ITip showTipView(View targetView, CharSequence text, @ITip.TriangleDirection int direction) {
        ITip tip = findTip(targetView);
        if (tip == null) {
            tip = new NormalTip();
            saveTip(targetView, tip);
        }
        tip.setNeedAnimation(needTipAnim)
                .setTipAnimation(tipAnimation);
        initTip(targetView, tip, text, direction);
        tipViewLayout.addNormalTip(tip);
        tip.show();
        return tip;
    }

    public void hideTipView(View targetView) {
        ITip tip = findTip(targetView);
        if (tip != null) {
            tip.hide();
        }
    }

    public void setNeedTipAnim(boolean needTipAnim) {
        this.needTipAnim = needTipAnim;
    }

    public TipManager setBackground(Drawable background) {
        this.background = background;
        return this;
    }

    public TipManager setTriangles(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        if (triangles == null)
            triangles = new Drawable[4];
        triangles[0] = left;
        triangles[1] = top;
        triangles[2] = right;
        triangles[3] = bottom;
        return this;
    }

    public TipManager setTextSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    public TipManager setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public TipManager setTextPadding(int pl, int pt, int pr, int pb) {
        if (textPadding == null)
            textPadding = new Rect();
        textPadding.set(pl, pt, pr, pb);
        return this;
    }

    public TipManager setMarginEdge(int margin) {
        this.marginEdge = margin;
        return this;
    }

    private void initTip(View targetView, ITip tip, CharSequence text, @ITip.TriangleDirection int direction) {
        Rect rect = new Rect(0, 0, targetView.getWidth(), targetView.getHeight());
        int[] loc = new int[2];
        targetView.getLocationInWindow(loc);
        rect.offset(loc[0], loc[1] - statusHeight);

        tip.setText(text);
        tip.setTextColor(textColor);
        tip.setTextSize(textSize);
        tip.setTextPadding(textPadding.left, textPadding.top, textPadding.right, textPadding.bottom);
        tip.setBackgroundDrawable(background.getConstantState().newDrawable());
        tip.setTriangleDirection(direction);
        if (direction != ITip.Triangle.NONE)
            tip.setTriangleDrawable(triangles[direction - 1].getConstantState().newDrawable());
        if (tip instanceof NormalTip) {
            initNormalTip(rect, (NormalTip) tip, direction);
        }
    }

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
            if (left < marginEdge) {
                left = marginEdge;
                right = left + needWidth;
            } else if (right > screenWidth - marginEdge) {
                right = screenWidth - marginEdge;
                left = right - needWidth;
            }
            if (direction == ITip.Triangle.TOP) {
                top = viewRect.bottom;
            } else {
                top = viewRect.top - needHeight;
            }
            bottom = top + needHeight;
            margin = centerX - left - (tw >> 1);
        } else if (direction != ITip.Triangle.NONE){
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

    private void saveTip(View view, ITip tip) {
        if (tips == null) {
            tips = new HashMap<>();
        }
        tips.put(view, tip);
    }

    private ITip findTip(View targetView) {
        if (tips != null) {
            return tips.get(targetView);
        }
        return null;
    }

    private void release() {
        tipViewLayout.removeAllTip();
        ViewParent parent = tipViewLayout.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(tipViewLayout);
        }
        if (tips != null) {
            tips.clear();
        }
        tipViewLayout = null;
    }

    private void initActivityParams(Activity context) {
        if (statusHeight != -404) {
            return;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        int flags = context.getWindow().getAttributes().flags;
        if ((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            statusHeight = 0;
            return;
        }
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int height = context.getResources().getDimensionPixelSize(resourceId);
        if (height == 0) {
            height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, context.getResources().getDisplayMetrics()));
        }
        statusHeight = height;
    }
}
