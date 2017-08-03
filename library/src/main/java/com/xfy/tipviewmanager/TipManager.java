package com.xfy.tipviewmanager;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.xfy.tipviewmanager.anim.DefaultTipAnimation;
import com.xfy.tipviewmanager.anim.ITipAnimation;
import com.xfy.tipviewmanager.tip.AdvancedTip;
import com.xfy.tipviewmanager.tip.IAdvancedTip;
import com.xfy.tipviewmanager.tip.ITextDelegate;
import com.xfy.tipviewmanager.tip.ITip;
import com.xfy.tipviewmanager.tip.NormalTip;
import com.xfy.tipviewmanager.tip.TipViewLayout;
import com.xfy.tipviewmanager.triangle.BottomTriangleDrawable;
import com.xfy.tipviewmanager.triangle.LeftTriangleDrawable;
import com.xfy.tipviewmanager.triangle.RightTriangleDrawable;
import com.xfy.tipviewmanager.triangle.TopTriangleDrawable;

import java.util.HashMap;

/**
 * Created by XiongFangyu on 2017/6/21.
 *
 * tip管理类，承载tip的view是{@link TipViewLayout}，每个Activity实例绑定一个{@link TipViewLayout}，
 * 内部维护一个对象池，对应每个Activity实例和{@link TipManager}。
 * 调用{@link #bindActivity(Activity)}获取{@link TipManager}对象；不使用时(eg: 在{@link Activity#onDestroy()})
 * 调用{@link #unbindActivity(Activity)}释放{@link TipManager}对象并清除对象池中相应的Activity
 *
 * 目前有两种tip，一种普通tip {@link ITip}，满足大多数需求
 * 另一种高级tip {@link IAdvancedTip}，可换行，可将文字换成图片等
 *
 * 设置项:
 *  {@link #setNeedTipAnim(boolean)}    设置显示或隐藏tip时是否需要动画
 *  {@link #setTipAnimation(ITipAnimation)} 设置动画具体实现，默认{@link DefaultTipAnimation}
 *  {@link #setBackground(Drawable)}    设置背景
 *  {@link #setTriangles(Drawable, Drawable, Drawable, Drawable)}   设置4个位置的三角形
 *  {@link #setTextSize(float)}         设置文字大小
 *  {@link #setTextColor(int)}          设置文字颜色
 *  {@link #setTextPadding(int, int, int, int)} 设置文字周围边距
 *  {@link #setMarginEdge(int)}         设置tip距离屏幕最小边距
 *  {@link #setTouchToHideAll(boolean)} 设置点击隐藏所有tip
 *  {@link #setTouchHideNeedNotfiy(boolean)} 设置点击隐藏是否产生回调
 *  {@link #setHandleTouchEevnt(boolean)}   设置是否消耗手势事件（是否点击穿透）
 *
 * tip与{@link View}对象相对应，一个{@link View}对象只能有一个tip，TipManager内部维护一个对象池，来实现{@link View}和tip一一对应
 * 显示tip时将从对象池中寻找相对应的tip，若没找到，则新建一个tip对象，若找到，使用已有tip对象，然后设置相应的文字及位置，并显示
 *
 * {@link #showTipView(View, CharSequence, int)}    显示tip
 * {@link #showAdvancedTip(View, CharSequence, ITextDelegate, int)}     显示高级tip {@link IAdvancedTip}
 * {@link #hideTipView(View)}                       隐藏tip
 * {@link #isTipShowing(View)}                      tip是否显示
 * {@link #removeTipView(View)}                     删除tip
 * {@link #hideTipLayout()}                         隐藏tip layout
 * {@link #showTipLayout()}                         显示tip layout 默认显示
 */
public class TipManager {
    /**
     * 维护单一Activity对应单一TipManager的对象池
     */
    private static HashMap<Activity, TipManager> Pool;

    /**
     * 绑定一个Activity对象，若已经绑定过，则返回之前绑定的{@link TipManager}对象
     * 若没绑定，则创建一个新的对象，并保存到对象池中
     * @param activity
     * @return 和Activity对象对应的 {@link TipManager}对象
     */
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

    /**
     * 解除和activity对象绑定的{@link TipManager}对象，并释放资源
     * @param activity
     */
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
    private boolean needTipAnim = true;
    private ITipAnimation tipAnimation;

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
        setMarginEdge(res.getDimensionPixelOffset(R.dimen.default_tip_margin));
        setTipAnimation(new DefaultTipAnimation());
    }

    /**
     * 返回和targetView对应的tip是否显示
     * @param targetView
     * @return false: 没有对应的tip或tip没有展示
     */
    public boolean isTipShowing(View targetView) {
        ITip tip = findTip(targetView);
        if (tip != null)
            return tip.isShowing();
        return false;
    }

    /**
     * 显示和targetView对应的tip，若{@link #tips}中不包含对应tip，则创建一个tip，并保存
     * 若包含，且tip为{@link IAdvancedTip}，删除并重新创建
     * 若包含，且tip为{@link NormalTip}，则使用已有tip，然后通过{@link #initTip(View, ITip, CharSequence, int)}设置tip的位置及其他信息
     * 并显示
     * @param targetView    需要显示tip指向的view
     * @param text          tip中的文字
     * @param direction     三角形指向方向 see {@link ITip.Triangle}
     * @return null if released
     */
    public @Nullable ITip showTipView(View targetView, CharSequence text, @ITip.TriangleDirection int direction) {
        return showTipView(targetView, text, 0, 0, direction);
    }

    /**
     * 显示和targetView对应的tip，若{@link #tips}中不包含对应tip，则创建一个tip，并保存
     * 若包含，且tip为{@link IAdvancedTip}，删除并重新创建
     * 若包含，且tip为{@link NormalTip}，则使用已有tip，然后通过{@link #initTip(View, ITip, CharSequence, int)}设置tip的位置及其他信息
     * 并显示
     * @param targetView    需要显示tip指向的view
     * @param text          tip中的文字
     * @param preTx         布局此tip时x移动距离
     * @param preTy         布局此tip时y移动距离
     * @param direction     三角形指向方向 see {@link ITip.Triangle}
     * @return
     */
    public
    @Nullable
    ITip showTipView(View targetView, CharSequence text, int preTx, int preTy, @ITip.TriangleDirection int direction) {
        if (tipViewLayout == null)
            return null;
        ITip tip = findTip(targetView);
        if (tip != null && tip instanceof IAdvancedTip) {
            removeTipView(targetView);
            tip = null;
        }
        if (tip == null) {
            tip = new NormalTip();
            saveTip(targetView, tip);
        }
        tip.setNeedAnimation(needTipAnim)
                .setTipAnimation(tipAnimation);
        Rect viewRect = initTip(targetView, tip, text, direction);
        tipViewLayout.addTip(tip, viewRect, preTx, preTy, direction);
        tip.show();
        return tip;
    }

    /**
     * 显示和targetView对应的{@link IAdvancedTip}，若{@link #tips}中对应的tip不是{@link IAdvancedTip}，
     * 则删除并重新创建；若{@link #tips}中不包含对应tip，则创建一个{@link AdvancedTip}
     * 通过{@link #initTip(View, ITip, CharSequence, int)}设置tip的位置及其他信息
     * @param targetView    需要显示tip指向的view
     * @param text          tip中的文字
     * @param textDelegate  文字调整器，如果为空，还不如用{@link #showTipView(View, CharSequence, int)}
     * @param direction     三角形指向方向 see {@link ITip.Triangle}
     * @return              null if released
     */
    public @Nullable
    IAdvancedTip showAdvancedTip(View targetView, CharSequence text, @Nullable ITextDelegate textDelegate, @ITip.TriangleDirection int direction) {
        return showAdvancedTip(targetView, text, 0, 0, textDelegate, direction);
    }

    /**
     * 显示和targetView对应的{@link IAdvancedTip}，若{@link #tips}中对应的tip不是{@link IAdvancedTip}，
     * 则删除并重新创建；若{@link #tips}中不包含对应tip，则创建一个{@link AdvancedTip}
     * 通过{@link #initTip(View, ITip, CharSequence, int)}设置tip的位置及其他信息
     * @param targetView    需要显示tip指向的view
     * @param text          tip中的文字
     * @param preTx         布局此tip时x移动距离
     * @param preTy         布局此tip时y移动距离
     * @param textDelegate  文字调整器，如果为空，还不如用{@link #showTipView(View, CharSequence, int)}
     * @param direction     三角形指向方向 see {@link ITip.Triangle}
     * @return null if released
     */
    public
    @Nullable
    IAdvancedTip showAdvancedTip(View targetView, CharSequence text, int preTx, int preTy,
                                 @Nullable ITextDelegate textDelegate, @ITip.TriangleDirection int direction) {
        if (tipViewLayout == null)
            return null;
        ITip tip = findTip(targetView);
        if (tip != null) {
            if (!(tip instanceof IAdvancedTip)) {
                removeTipView(targetView);
                tip = null;
            }
        }
        if (tip == null) {
            tip = new AdvancedTip();
            saveTip(targetView, tip);
        }
        tip.setNeedAnimation(needTipAnim)
                .setTipAnimation(tipAnimation);
        ((IAdvancedTip) tip).setTextDelegate(textDelegate);
        Rect viewRect = initTip(targetView, tip, text, direction);
        tipViewLayout.addTip(tip, viewRect, preTx, preTy, direction);
        tip.show();
        return (IAdvancedTip) tip;
    }

    /**
     * 隐藏对应tip
     * @param targetView
     */
    public void hideTipView(View targetView) {
        ITip tip = findTip(targetView);
        if (tip != null) {
            tip.hide();
        }
    }

    /**
     * 删除对应tip
     * @param targetView
     */
    public void removeTipView(View targetView) {
        if (tipViewLayout == null || tips == null)
            return;
        ITip tip = tips.remove(targetView);
        if (tip != null) {
            tipViewLayout.removeTip(tip);
        }
    }

    /**
     * 隐藏tip view
     */
    public void hideTipLayout() {
        if (tipViewLayout != null) {
            tipViewLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 显示tip view
     */
    public void showTipLayout() {
        if (tipViewLayout != null) {
            tipViewLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置是否消耗手势事件（是否点击穿透）
     * @param handle    true 消耗（点击不穿透）
     * @return  this object
     */
    public TipManager setHandleTouchEevnt(boolean handle) {
        if (tipViewLayout != null)
            tipViewLayout.setHandleEvent(handle);
        return this;
    }

    /**
     * 设置tip显示或隐藏时是否有动画
     * @param needTipAnim true: 有动画
     * @return this object
     */
    public TipManager setNeedTipAnim(boolean needTipAnim) {
        this.needTipAnim = needTipAnim;
        return this;
    }

    /**
     * 设置tip显隐动画的具体实现
     * 默认为{@link DefaultTipAnimation}
     * @param animation 动画实现
     * @return this object
     */
    public TipManager setTipAnimation(ITipAnimation animation) {
        tipAnimation = animation;
        return this;
    }

    /**
     * 设置背景，默认{@link com.xfy.tipviewmanager.R.drawable#tip_background}
     * @param background 背景drawable
     * @return this object
     */
    public TipManager setBackground(Drawable background) {
        this.background = background;
        return this;
    }

    /**
     * 设置4个方向的三角形
     * @param left      指向左边的三角形，默认{@link LeftTriangleDrawable}
     * @param top       指向上方的三角形，默认{@link TopTriangleDrawable}
     * @param right     指向右边的三角形，默认{@link RightTriangleDrawable}
     * @param bottom    指向下边的三角形，默认{@link BottomTriangleDrawable}
     * @return  this object
     */
    public TipManager setTriangles(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        if (triangles == null)
            triangles = new Drawable[4];
        triangles[0] = left;
        triangles[1] = top;
        triangles[2] = right;
        triangles[3] = bottom;
        return this;
    }

    /**
     * 设置文字大小
     * @param textSize px，默认{@link com.xfy.tipviewmanager.R.dimen#default_tip_text_size} 15sp
     * @return this object
     */
    public TipManager setTextSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    /**
     * 设置文字颜色
     * @param textColor 默认{@link com.xfy.tipviewmanager.R.color#default_tip_text_color}
     * @return this object
     */
    public TipManager setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * 设置文字边距
     * @param pl 左边距，默认{@link com.xfy.tipviewmanager.R.dimen#default_tip_text_padding} 5dp
     * @param pt 上边距，默认{@link com.xfy.tipviewmanager.R.dimen#default_tip_text_padding} 5dp
     * @param pr 右边距，默认{@link com.xfy.tipviewmanager.R.dimen#default_tip_text_padding} 5dp
     * @param pb 下边距，默认{@link com.xfy.tipviewmanager.R.dimen#default_tip_text_padding} 5dp
     * @return this object
     */
    public TipManager setTextPadding(int pl, int pt, int pr, int pb) {
        if (textPadding == null)
            textPadding = new Rect();
        textPadding.set(pl, pt, pr, pb);
        return this;
    }

    /**
     * 设置点击后隐藏所有的tip
     * @param hideAll   true：点击隐藏所有tip
     * @return this object
     */
    public TipManager setTouchToHideAll(boolean hideAll) {
        if (tipViewLayout != null) {
            tipViewLayout.setTouchToHideAll(hideAll);
        }
        return this;
    }

    /**
     * 设置点击隐藏是否产生回调
     * @param need true: 回调
     * @return this object
     */
    public TipManager setTouchHideNeedNotfiy(boolean need) {
        if (tipViewLayout != null) {
            tipViewLayout.setTouchHideNeedNotify(need);
        }
        return this;
    }

    /**
     * 设置tip距离屏幕边距
     * 当tip指向上方或下方时，若过长，tip距离左边屏幕至少有margin
     * 若左边屏幕剩余较多，则tip距离右边屏幕至少有margin
     * @param margin 默认{@link com.xfy.tipviewmanager.R.dimen#default_tip_margin} 5dp
     * @return this object
     */
    public TipManager setMarginEdge(int margin) {
        if (tipViewLayout != null)
            tipViewLayout.setMarginEdge(margin);
        return this;
    }

    /**
     * 设置tip文字信息，背景信息，位置信息等
     * @param targetView    根据targetView及其位置信息显示tip
     * @param tip           may be {@link NormalTip} or {@link com.xfy.tipviewmanager.tip.IAdvancedTip}
     *                      目前{@link com.xfy.tipviewmanager.tip.IAdvancedTip}的实现为{@link AdvancedTip}，
     *                      是{@link NormalTip}的子类
     * @param text          tip需要显示的文案
     * @param direction     三角形指向 see {@link ITip.Triangle}
     */
    private Rect initTip(View targetView, ITip tip, CharSequence text, @ITip.TriangleDirection int direction) {
        Rect rect = new Rect(0, 0, targetView.getWidth(), targetView.getHeight());
        int[] loc = new int[2];
        targetView.getLocationInWindow(loc);
        rect.offset(loc[0], loc[1] - statusHeight);

        tip.setTipText(text);
        tip.setTipTextColor(textColor);
        tip.setTipTextSize(textSize);
        tip.setTipTextPadding(textPadding.left, textPadding.top, textPadding.right, textPadding.bottom);
        tip.setTipBackgroundDrawable(background.getConstantState().newDrawable());
        tip.setTriangleDirection(direction);
        if (direction != ITip.Triangle.NONE)
            tip.setTriangleDrawable(triangles[direction - 1].getConstantState().newDrawable());
        return rect;
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

    /**
     * 释放资源，清除所有tip和view
     * 仅供{@link #unbindActivity(Activity)}调用
     */
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

    /**
     * 获取顶部导航栏高度，及屏幕宽高
     * 若此Activity为全屏展示，则导航栏高度为0
     * @param context
     */
    private void initActivityParams(Activity context) {
        if (statusHeight != -404) {
            return;
        }
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
