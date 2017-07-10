package com.xfy.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.xfy.tipviewmanager.TipManager;
import com.xfy.tipviewmanager.tip.ITextDelegate;
import com.xfy.tipviewmanager.tip.ITip;
import com.xfy.tipviewmanager.tip.OnTipHideListener;

/**
 * Created by XiongFangyu on 2017/6/26.
 */

public class TestTipActivity extends Activity implements View.OnClickListener{

    private Button button;
    private ImageView imageView;
    private ImageView imageView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_tip_activity);
        initView();
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        button.setOnClickListener(this);
        imageView.setOnClickListener(this);
        imageView2.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        if (v == button) {
            TipManager.bindActivity(this).showTipView(v, "呵呵呵呵e", ITip.Triangle.TOP).autoHide(1000).setOnTipHideListener(new OnTipHideListener() {
                @Override
                public void onHide(ITip tip) {
                    TipManager.bindActivity(TestTipActivity.this).showTipView(v, "呵呵呵呵e", ITip.Triangle.BOTTOM);
                }
            });
            return;
        }
        if (TipManager.bindActivity(this).isTipShowing(v)) {
            TipManager.bindActivity(this).hideTipView(v);
        } else {
            TipManager.bindActivity(this).showAdvancedTip(v, "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", textDelegate, ITip.Triangle.TOP);
        }
    }

    private static final ITextDelegate textDelegate = new TextDelegateImpl();

    private static class TextDelegateImpl implements ITextDelegate {
        ForegroundColorSpan redSpan;
        TextDelegateImpl() {
            redSpan = new ForegroundColorSpan(Color.BLUE);
        }

        @Override
        public CharSequence parseText(CharSequence src) {
            SpannableStringBuilder stringBuilder = SpannableStringBuilder.valueOf(src);
            stringBuilder.setSpan(redSpan, 0, src.length() >> 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return stringBuilder;
        }
    }
}
