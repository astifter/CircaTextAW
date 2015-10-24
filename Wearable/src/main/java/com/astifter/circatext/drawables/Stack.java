package com.astifter.circatext.drawables;

import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;

import java.util.ArrayList;

abstract class Stack implements Drawable {
    boolean hidden = false;
    Rect bounds = new Rect(0, 0, 0, 0);
    ArrayList<Drawable> stack;
    int alignment;
    private int color;

    Stack() {
        stack = new ArrayList<>();
    }

    @Override
    public float getHeight() {
        return bounds.height();
    }

    @Override
    public float getWidth() {
        return bounds.width();
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        for (Drawable t : stack) {
            t.setAmbientMode(inAmbientMode);
        }
    }

    @Override
    public void setAlpha(int a) {
        for (Drawable t : stack) {
            t.setAlpha(a);
        }
    }

    @Override
    public void hide(boolean h) {
        this.hidden = h;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public float getFutureHeight() {
        int height = 0;
        for (Drawable t : stack) {
            if (t.getFutureHeight() > height)
                height = (int) t.getHeight();
        }
        return height;
    }

    @Override
    public float getFutureWidth() {
        int height = 0;
        for (Drawable t : stack) {
            height += t.getFutureWidth();
        }
        return height;
    }

    @Override
    public int getTouchedText(int x, int y) {
        return DrawingHelpers.getTouchedText(x, y, this.stack);
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setColor(int c) {
        this.color = c;
        for (Drawable t : this.stack) {
            t.setColor(c);
        }
    }

    @Override
    public void setAlignment(int a) {
        this.alignment = a;
    }
}