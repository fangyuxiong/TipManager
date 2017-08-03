package com.xfy.tipviewmanager.tip;

/**
 * Created by XiongFangyu on 2017/7/25.
 */
public interface LayoutListener<T> {
    /**
     * 需要重新计算obj的位置及大小
     * @param obj
     */
    void reqeustLayout(T obj);
}
