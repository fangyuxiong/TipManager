package com.xfy.tipviewmanager.anim;

import android.view.animation.Transformation;

/**
 * Created by XiongFangyu on 2017/7/4.
 *
 * 动画接口，tip显示隐藏动画调用
 */
public interface ITipAnimation {
    /**
     * 动画更新
     * @param percent 0 ~ 1
     * @param transformation 可调用{@link Transformation#setAlpha(float)} 或设置其中的matrix
     */
    void applyTransformation(float percent, Transformation transformation);
}
