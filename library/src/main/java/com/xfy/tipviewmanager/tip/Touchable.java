package com.xfy.tipviewmanager.tip;

/**
 * Created by XiongFangyu on 2017/7/4.
 */
public interface Touchable {
    /**
     * 是否被触摸到了
     * @param x
     * @param y
     * @return
     */
    boolean isTouched(float x, float y);
}
