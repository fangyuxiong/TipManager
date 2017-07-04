package com.xfy.tipviewmanager;

/**
 * Created by XiongFangyu on 2017/6/20.
 *
 * 文字转换，比如可将文字转换为{@link android.text.SpannableString}
 * 来支持图片展示
 */
public interface ITextDelegate {
    /**
     * 转换文字
     * @param src   原文案
     * @return      转换后文案
     */
    CharSequence parseText(CharSequence src);
}
