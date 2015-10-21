package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

public class Position {
    private int right;
    private int top;
    private int bottom;
    private int left;
    private int alignment;

    public Position() {
    }

    public Position(Rect r) {
        this.left = r.left;
        this.right = r.right;
        this.top = r.top;
        this.bottom = r.bottom;
        this.alignment = Drawable.Align.LEFT;
    }

    public Position(Rect r, int a) {
        this.left = r.left;
        this.right = r.right;
        this.top = r.top;
        this.bottom = r.bottom;
        this.alignment = a;
    }

    public static Position percentagePosition(Position p, Rect b) {
        Position newp = new Position();
        newp.left = (int) (b.width() * p.left / 100f);
        newp.top = (int) (b.height() * p.top / 100f);
        newp.right = (int) (b.width() * p.right / 100f);
        newp.bottom = (int) (b.height() * p.bottom / 100f);
        newp.alignment = p.alignment;
        return newp;
    }

    public Rect toRect() {
        return new Rect(left, top, right, bottom);
    }

    public int height() {
        return bottom - top;
    }

    public int width() {
        return right - left;
    }

    public String toString() {
        return String.format("Position(%d,%d - %d,%d)", left, top, right, bottom);
    }

    public int left() {
        return this.left;
    }

    public int top() {
        return this.top;
    }

    public int right() {
        return this.right;
    }

    public int bottom() {
        return this.bottom;
    }

    public int align() {
        return this.alignment;
    }

    public void setLeft(int l) {
        this.left = l;
    }

    public void setTop(int l) {
        this.top = l;
    }

    public void setRight(int l) {
        this.right = l;
    }

    public void setBottom(int l) {
        this.bottom = l;
    }

    public void setAlign(int a) {
        this.alignment = a;
    }
}