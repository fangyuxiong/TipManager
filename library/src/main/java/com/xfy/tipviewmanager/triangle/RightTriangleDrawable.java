package com.xfy.tipviewmanager.triangle;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by XiongFangyu on 2017/6/26.
 */

public class RightTriangleDrawable extends TriangleDrawable {

    public RightTriangleDrawable() {}

    protected RightTriangleDrawable(CS cs) {
        super(cs);
    }

    @Override
    protected CS newCs() {
        return new RCS();
    }

    @Override
    protected float getRotateDegree() {
        return 90;
    }

    private static class RCS extends CS {

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new RightTriangleDrawable(this);
        }

        @Override
        protected int[] getPoint(Rect rect) {
            return new int[] {
                    rect.left, rect.top,
                    rect.left, rect.bottom,
                    rect.right, rect.centerY()
            };
        }
    }
}
