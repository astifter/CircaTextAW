package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.astifter.circatextutils.CTCs;

import java.util.ArrayList;

public class StackVertical extends Stack {
    private int yCenter = -1;

    private final ArrayList<Drawable> aboveStack;
    private final ArrayList<Drawable> belowStack;

    public StackVertical() {
        aboveStack = new ArrayList<>();
        belowStack = new ArrayList<>();
    }

    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            return;
        }
        this.bounds = b;

        if (yCenter >= 0) {
            onDrawWithOffset(canvas);
            return;
        }

        int width = 0;
        int heigth = 0;
        for (Drawable t : this.belowStack) {
            if (t.isHidden()) continue;
            Rect newBounds = new Rect(this.bounds.left, this.bounds.top + heigth,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            heigth += t.getHeight();

            if (t.getWidth() > width)
                width = (int) t.getWidth();
        }
        this.bounds.bottom = Math.min(this.bounds.bottom, this.bounds.top + heigth);
        this.bounds.right = Math.min(this.bounds.right, this.bounds.left + width);

        if (CTCs.DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(this.bounds, p);
        }
    }

    private void onDrawWithOffset(Canvas canvas) {
        int width = 0;

        int heigthAbove = 0;
        for (Drawable t : this.aboveStack) {
            if (t.isHidden()) continue;
            int currentHeight = (int) t.getFutureHeight();
            Rect newBounds = new Rect(this.bounds.left, yCenter - heigthAbove - currentHeight,
                                      this.bounds.right, yCenter - heigthAbove);
            t.onDraw(canvas, newBounds);
            heigthAbove += currentHeight;

            if (t.getWidth() > width)
                width = (int) t.getWidth();
        }
        int heigthBelow = 0;
        for (Drawable t : this.belowStack) {
            if (t.isHidden()) continue;
            Rect newBounds = new Rect(this.bounds.left, yCenter + heigthBelow,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            heigthBelow += t.getHeight();

            if (t.getWidth() > width)
                width = (int) t.getWidth();
        }

        Rect newBounds = new Rect();
        newBounds.left = this.bounds.left;
        newBounds.right = Math.min(this.bounds.right, this.bounds.left + width);
        newBounds.top = Math.max(this.bounds.top, this.yCenter - heigthAbove);
        newBounds.bottom = Math.min(this.bounds.bottom, this.yCenter + heigthBelow);
        this.bounds = newBounds;

        if (CTCs.DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(this.bounds, p);
        }
    }

    public void addBelow(Drawable d) {
        this.stack.add(d);
        this.belowStack.add(d);
    }

    public void addAbove(Drawable d) {
        this.stack.add(0, d);
        this.aboveStack.add(d);
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return DrawableHelpers.getDrawnRects(stack);
    }

    public void setOffset(int mYOffset) {
        yCenter = mYOffset;
    }
}
