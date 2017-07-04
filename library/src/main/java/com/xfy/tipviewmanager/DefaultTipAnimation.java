package com.xfy.tipviewmanager;

import android.view.animation.Transformation;

/**
 * Created by XiongFangyu on 2017/7/4.
 */

public class DefaultTipAnimation implements ITipAnimation {
    @Override
    public void applyTransformation(float percent, Transformation transformation) {
        transformation.setAlpha(percent);
    }
}
