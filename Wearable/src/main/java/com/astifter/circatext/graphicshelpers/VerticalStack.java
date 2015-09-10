package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;

public class VerticalStack extends AbstractStack {
    float yCenter = -1;

    ArrayList<CircaTextDrawable> aboveStack;
    ArrayList<CircaTextDrawable> belowStack;
    int aboveStackHeight = 0;
    int belowStackHeight = 0;

    public VerticalStack() {
        aboveStack = new ArrayList<>();
        belowStack = new ArrayList<>();
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        if (hidden) return;
        this.bounds = bounds;

        int width = 0;
        int heigth = 0;
        for (CircaTextDrawable t : this.belowStack) {
            Rect newBounds = new Rect(this.bounds.left, this.bounds.top + heigth,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            heigth += t.getHeight();

            if (t.getWidth() > width)
                width = (int)t.getWidth();
        }
        this.bounds.bottom = Math.min(this.bounds.bottom, this.bounds.top + heigth);
        this.bounds.right = Math.min(this.bounds.right, this.bounds.left + width);

        Paint p = new Paint();
        p.setColor(Color.WHITE); p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(this.bounds, p);
    }

    private void onDrawWithOffset(Canvas canvas, Rect bounds) {

    }

    public void addBelow(CircaTextDrawable d) {
        this.belowStack.add(d);
    }
}
