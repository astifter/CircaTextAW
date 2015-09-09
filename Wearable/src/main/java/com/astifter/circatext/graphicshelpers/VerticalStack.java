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

        if (yCenter == -1) {
            belowStackHeight = 0;
            for (CircaTextDrawable t : belowStack) {
                Rect newBounds = new Rect(this.bounds.left, this.bounds.top + belowStackHeight,
                                          this.bounds.right, this.bounds.bottom);
                t.onDraw(canvas, newBounds);
                belowStackHeight += t.getHeight();
            }
        }

        this.bounds.bottom = belowStackHeight;
    }

    public void addBelow(CircaTextDrawable d) {
        this.belowStack.add(d);
    }
}
