package com.xfy.tipviewmanager.tip;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by XiongFangyu on 2017/7/25.
 */
class NormalTipInfo {

    public NormalTip tip;

    public Rect viewRect;

    public
    @ITip.TriangleDirection
    int direction;

    public NormalTipInfo() {
    }

    public NormalTipInfo(NormalTip tip) {
        this.tip = tip;
    }

    public NormalTipInfo(NormalTip tip, Rect viewRect, int dir) {
        this.tip = tip;
        this.viewRect = viewRect;
        this.direction = dir;
    }

    public void draw(Canvas canvas) {
        if (tip != null) {
            tip.draw(canvas);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NormalTipInfo that = (NormalTipInfo) o;

        return tip != null ? tip.equals(that.tip) : that.tip == null;

    }
}
