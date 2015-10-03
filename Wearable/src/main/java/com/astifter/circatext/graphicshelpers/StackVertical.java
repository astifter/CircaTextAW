package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.astifter.circatextutils.CircaTextConsts;

import java.util.ArrayList;

public class StackVertical extends Stack {
    int yCenter = -1;

    ArrayList<Drawable> aboveStack;
    ArrayList<Drawable> belowStack;

    public StackVertical() {
        aboveStack = new ArrayList<>();
        belowStack = new ArrayList<>();
    }

    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            this.bounds = new Rect(b.left, b.top, b.left, b.top);
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
            Rect newBounds = new Rect(this.bounds.left, this.bounds.top + heigth,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            heigth += t.getHeight();

            if (t.getWidth() > width)
                width = (int) t.getWidth();
        }
        this.bounds.bottom = Math.min(this.bounds.bottom, this.bounds.top + heigth);
        this.bounds.right = Math.min(this.bounds.right, this.bounds.left + width);

        if (CircaTextConsts.DEBUG) {
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
            int currentHeight = (int) t.getHeight();
            Rect newBounds = new Rect(this.bounds.left, yCenter - heigthAbove - currentHeight,
                                      this.bounds.right, yCenter - heigthAbove);
            t.onDraw(canvas, newBounds);
            heigthAbove += currentHeight;

            if (t.getWidth() > width)
                width = (int) t.getWidth();
        }
        int heigthBelow = 0;
        for (Drawable t : this.belowStack) {
            Rect newBounds = new Rect(this.bounds.left, yCenter + heigthBelow,
                                      this.bounds.right, this.bounds.bottom);
            t.onDraw(canvas, newBounds);
            heigthBelow += t.getHeight();

            if (t.getWidth() > width)
                width = (int) t.getWidth();
        }

        this.bounds.bottom = Math.min(this.bounds.bottom, yCenter + heigthBelow);
        this.bounds.right = Math.min(this.bounds.right, this.bounds.left + width);

        if (CircaTextConsts.DEBUG) {
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

    public void setOffset(int mYOffset) {
        yCenter = mYOffset;
    }
}