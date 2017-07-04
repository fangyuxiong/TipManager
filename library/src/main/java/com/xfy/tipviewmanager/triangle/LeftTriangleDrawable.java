package com.xfy.tipviewmanager.triangle;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by XiongFangyu on 2017/6/26.
 */

public class LeftTriangleDrawable extends TriangleDrawable {

    public LeftTriangleDrawable() {}

    protected LeftTriangleDrawable(TriangleDrawable.CS cs) {
        super(cs);
    }
    @Override
    protected TriangleDrawable.CS newCs() {
        return new CS();
    }

    @Override
    protected float getRotateDegree() {
        return -90;
    }

    private static class CS extends TriangleDrawable.CS {

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new LeftTriangleDrawable(this);
        }

        @Override
        protected int[] getPoint(Rect rect) {
            return new int[] {
                    rect.right, rect.top,
                    rect.right, rect.bottom,
                    rect.left, rect.centerY()
            };
        }
    }
}
