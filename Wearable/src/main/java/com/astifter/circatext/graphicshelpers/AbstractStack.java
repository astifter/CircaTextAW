package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

import java.util.ArrayList;

abstract class AbstractStack implements CircaTextDrawable {
    protected boolean hidden = false;
    Rect bounds = new Rect(0, 0, 0, 0);;
    ArrayList<CircaTextDrawable> stack;
    private boolean inAmbientMode = false;

    protected AbstractStack() {
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
        for (CircaTextDrawable t : stack) {
            t.setAmbientMode(inAmbientMode);
        }
    }

    @Override
    public void setAlpha(int a) {
        for (CircaTextDrawable t : stack) {
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