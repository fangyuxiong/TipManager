package com.xfy.tipviewmanager.triangle;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by XiongFangyu on 2017/6/26.
 *
 * 默认指向下方的三角形
 */
public class BottomTriangleDrawable extends TriangleDrawable {

    public BottomTriangleDrawable() {}

    protected BottomTriangleDrawable(CS cs) {
        super(cs);
    }

    @Override
    protected CS newCs() {
        return new BCS();
    }

    @Override
    protected float getRotateDegree() {
        return 180;
    }

    private static class BCS extends CS {

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new BottomTriangleDrawable(this);
        }

        @Override
        protected int[] getPoint(Rect rect) {
            return new int[] {
                    rect.left, rect.top,
                    rect.right, rect.top,
                    rect.centerX(), rect.bottom
            };
        }
    }
}
