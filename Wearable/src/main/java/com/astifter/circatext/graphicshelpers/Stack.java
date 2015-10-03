package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

import java.util.ArrayList;

abstract class Stack implements Drawable {
    protected boolean hidden = false;
    Rect bounds = new Rect(0, 0, 0, 0);;
    ArrayList<Drawable> stack;
    private boolean inAmbientMode = false;

    protected Stack() {
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
        this.inAmbientMode = inAmbientMode;
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
    public void hide() {
        this.hidden = true;
    }

    @Override
    public void show() {
        this.hidden = false;
    }
}