package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

import java.util.ArrayList;

abstract class Stack implements Drawable {
    boolean hidden = false;
    Rect bounds = new Rect(0, 0, 0, 0);
    ArrayList<Drawable> stack;

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
    public int getTouchedText(int x, int y) {
        for (Drawable t : this.stack) {
            int idx = t.getTouchedText(x, y);
            if (idx != -1)
                return idx;
        }
        return -1;
    }

    @Override
    public int getColor() {
        // TODO how to implement
        return -1;
    }

    @Override
    public void setColor(int c) {
        for (Drawable t : this.stack) {
            t.setColor(c);
        }
    }
}