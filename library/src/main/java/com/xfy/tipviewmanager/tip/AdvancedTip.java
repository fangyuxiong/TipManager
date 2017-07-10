package com.xfy.tipviewmanager.tip;


/**
 * Created by XiongFangyu on 2017/7/8.
 *
 * 高级tip
 * 可修改文字显示方式，使用{@link AdvancedTextDrawable}实现文字渲染
 */
public class AdvancedTip extends NormalTip implements IAdvancedTip {

    private ITextDelegate textDelegate;

    public AdvancedTip() {
        super();
    }

    @Override
    public IAdvancedTip setTextDelegate(ITextDelegate textDelegate) {
        this.textDelegate = textDelegate;
        return this;
    }

    @Override
    protected TextDrawable newTextDrawable() {
        return new AdvancedTextDrawable();
    }

    @Override
    public IAdvancedTip setTipText(CharSequence text) {
        if (textDelegate != null && text != null) {
            super.setTipText(textDelegate.parseText(text));
        } else {
            super.setTipText(text);
        }
        return this;
    }
}
