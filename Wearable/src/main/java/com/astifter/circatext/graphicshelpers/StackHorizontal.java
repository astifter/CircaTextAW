package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.astifter.circatextutils.CircaTextConsts;

public class StackHorizontal extends Stack {
    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            this.bounds = new Rect(b.left, b.top, b.left, b.top);
            return;
        }
        this.bounds = b;

        int width = 0;
        int height = 0;
        for (Drawable t : stack) {
            Rect newBounds = new Rect(this.bounds.left + width, this.bounds.top,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            width += t.getWidth();

            if (t.getHeight() > height)
                height = (int) t.getHeight();
        }
        this.bounds.right = Math.min(this.bounds.right, this.bounds.left + width);
        this.bounds.bottom = Math.min(this.bounds.bottom, this.bounds.top + height);

        if (CircaTextConsts.DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.GRAY);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(this.bounds, p);
        }
    }

    public void add(Drawable d) {
        this.stack.add(d);
    }

    public float getCurrentHeight() {
        int height = 0;
        for (Drawable t : stack) {
            if (t.getHeight() > height)
                height = (int) t.getHeight();
        }
        return height;
    }
}
