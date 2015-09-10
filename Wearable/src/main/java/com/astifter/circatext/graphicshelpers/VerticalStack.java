package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
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
        for (CircaTextDrawable t : this.belowStack) {
            if (t.getWidth() > width)
                width = (int)t.getWidth();
        }
        this.bounds.right = this.bounds.left + width;

        int heigth = 0;
        for (CircaTextDrawable t : this.belowStack) {
            Rect newBounds = new Rect(this.bounds.left, this.bounds.top + heigth,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            heigth += t.getHeight();
        }
        this.bounds.bottom = this.bounds.top + heigth;
    }

    private void onDrawWithOffset(Canvas canvas, Rect bounds) {

    }

    public void addBelow(CircaTextDrawable d) {
        this.belowStack.add(d);
    }
}
