package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.datahelpers.Position;

import java.util.ArrayList;

public class StaticDrawable implements Drawable {
    private final Drawable drawable;
    private final Position position;

    public StaticDrawable(Drawable d, Position p) {
        this.drawable = d;
        this.position = p;
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        Rect r = Position.percentagePosition(this.position, bounds).toRect();
        this.drawable.onDraw(canvas, r);
    }

    @Override
    public float getHeight() {
        return this.drawable.getHeight();
    }

    @Override
    public float getFutureHeight() {
        return this.drawable.getFutureHeight();
    }

    @Override
    public float getWidth() {
        return this.drawable.getWidth();
    }

    @Override
    public float getFutureWidth() {
        return this.drawable.getFutureWidth();
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        this.drawable.setAmbientMode(inAmbientMode);
    }

    @Override
    public void setAlpha(int a) {
        this.drawable.setAlpha(a);
    }

    @Override
    public int getColor() {
        return this.drawable.getColor();
    }

    @Override
    public void setColor(int c) {
        this.drawable.setColor(c);
    }

    @Override
    public void setAlignment(int a) {
        this.drawable.setAlignment(a);
    }

    @Override
    public int getTouchedText(int x, int y) {
        return this.drawable.getTouchedText(x, y);
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return this.drawable.getDrawnRects();
    }
}
