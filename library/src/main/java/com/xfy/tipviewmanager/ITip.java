package com.xfy.tipviewmanager;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by XiongFangyu on 2017/6/20.
 *
 * 基础tip
 */
public interface ITip {
    int SHOW_ANIMATION_DURATION = 200;
    int HIDE_ANIMATION_DURATION = 150;
    interface Triangle {
        int NONE = 0;   //没有三角形
        int LEFT = 1;   //三角形指向左侧
        int TOP = 2;    //三角形指向上方
        int RIGHT = 3;  //三角形指向右侧
        int BOTTOM = 4; //三角形指向下方
    }

    @IntDef({Triangle.NONE, Triangle.LEFT, Triangle.TOP, Triangle.RIGHT, Triangle.BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    @interface TriangleDirection {}

    /**
     * 设置{@link #show()}或{@link #hide()}时是否使用动画
     * 默认使用
     * @param need 是否使用动画
     * @return     this object
     */
    ITip setNeedAnimation(boolean need);

    /**
     * 若使用动画，则可自定义动画
     * @param tipAnimation  动画实现
     * @return  this object
     */
    ITip setTipAnimation(ITipAnimation tipAnimation);

    /**
     * 设置显示和隐藏动画时间
     * @param showTime 显示动画时间
     * @param hideTime 隐藏动画时间
     * @return  this object
     */
    ITip setAnimationTime(long showTime, long hideTime);

    ITip show();

    ITip hide();

    /**
     * 设置delay ms后自动消失
     * @param delay 延时，单位ms
     * @return      this object
     */
    ITip autoHide(long delay);

    /**
     * 是否显示，在动画中会返回true
     * @return true: 显示
     */
    boolean isShowing();

    /**
     * 设置文案，暂时支持静态表情
     * @param text  显示文案
     * @return  this object
     */
    ITip setText(CharSequence text);

    /**
     * 设置文字大小
     * @param size  单位 px
     * @return this object
     */
    ITip setTextSize(float size);

    /**
     * 设置文字颜色
     * @param color 文字颜色
     * @return  this object
     */
    ITip setTextColor(@ColorInt int color);

    /**
     * 设置文字和背景之间的距离
     * @param pl
     * @param pt
     * @param pr
     * @param pb
     * @return  this object
     */
    ITip setTextPadding(int pl, int pt, int pr, int pb);

    /**
     * 设置箭头左边距或上边距
     * @param margin
     * @return  this object
     */
    ITip setTriangleMargin(int margin);

    /**
     * 设置背景
     * @param drawable
     * @return  this object
     */
    ITip setBackgroundDrawable(Drawable drawable);

    /**
     * 设置三角形，需要跟三角形方向对应
     * @param drawable
     * @return
     */
    ITip setTriangleDrawable(Drawable drawable);

    /**
     * 设置三角形方向
     * @param direction 可选值：
     *                  {@link Triangle#NONE}    没有三角形
     *                  {@link Triangle#LEFT}    三角形指向左侧
     *                  {@link Triangle#TOP}     三角形指向上方
     *                  {@link Triangle#RIGHT}   三角形指向右侧
     *                  {@link Triangle#BOTTOM}  三角形指向下方
     * @return      this object
     */
    ITip setTriangleDirection(@TriangleDirection int direction);

    /**
     * 设置消失监听
     * @param onTipHideListener
     * @return this object
     */
    ITip setOnTipHideListener(OnTipHideListener onTipHideListener);
}
