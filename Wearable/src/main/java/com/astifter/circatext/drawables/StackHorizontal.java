package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.astifter.circatextutils.CTCs;

public class StackHorizontal extends Stack {
    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            return;
        }
        this.bounds = b;

        int futureWidth = 0;
        for (Drawable t : stack) {
            futureWidth += t.getFutureWidth();
        }
        switch (this.alignment) {
            case Align.CENTER:
                int offset = (this.bounds.width() - futureWidth) / 2;
                this.bounds.left += offset;
                break;
            case Align.RIGHT:
                this.bounds.left = this.bounds.right - futureWidth;
                break;
        }

        int width = 0;
        int height = 0;
        for (Drawable t : stack) {
            if (t.isHidden()) continue;
            Rect newBounds = new Rect(this.bounds.left + width, this.bounds.top,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            width += t.getWidth();

            if (t.getHeight() > height)
                height = (int) t.getHeight();
        }
        this.bounds.right = Math.min(this.bounds.right, this.bounds.left + width);
        this.bounds.bottom = Math.min(this.bounds.bottom, this.bounds.top + height);

        if (CTCs.DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.GRAY);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(this.bounds, p);
        }
    }

    public void add(Drawable d) {
        this.stack.add(d);
    }
}
