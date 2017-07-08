package com.xfy.tipviewmanager.triangle;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by XiongFangyu on 2017/6/26.
 *
 * 默认指向上方的三角形
 */
public class TopTriangleDrawable extends TriangleDrawable {

    public TopTriangleDrawable() {}

    protected TopTriangleDrawable(CS cs) {
        super(cs);
    }

    @Override
    protected CS newCs() {
        return new TCS();
    }

    @Override
    protected float getRotateDegree() {
        return 0;
    }

    private static class TCS extends CS {

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new TopTriangleDrawable(this);
        }

        @Override
        protected int[] getPoint(Rect rect) {
            return new int[] {
                    rect.left, rect.bottom,
                    rect.right, rect.bottom,
                    rect.centerX(), rect.top
            };
        }
    }
}
