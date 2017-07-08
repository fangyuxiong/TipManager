package com.xfy.tipviewmanager.tip;

/**
 * Created by XiongFangyu on 2017/6/20.
 *
 * 拥有高级功能：
 *  文案转换
 */
public interface IAdvancedTip extends ITip {
    /**
     * 设置文案转换器
     * @param textDelegate
     * @return  this object
     */
    IAdvancedTip setTextDelegate(ITextDelegate textDelegate);
}
