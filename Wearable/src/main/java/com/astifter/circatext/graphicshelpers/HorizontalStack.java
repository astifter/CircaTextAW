package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

public class HorizontalStack extends AbstractStack {
    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        if (hidden) return;
        this.bounds = bounds;

        int height = 0;
        for (CircaTextDrawable t : stack) {
            if (t.getHeight() > height)
                height = (int)t.getHeight();
        }
        this.bounds.bottom = this.bounds.top + height;

        int width = 0;
        for (CircaTextDrawable t : stack) {
            Rect newBounds = new Rect(this.bounds.left + width, this.bounds.top,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            width += t.getWidth();
        }
        this.bounds.right = this.bounds.left + width;
    }

    public void addRight(CircaTextDrawable d) {
        this.stack.add(d);
    }
}
