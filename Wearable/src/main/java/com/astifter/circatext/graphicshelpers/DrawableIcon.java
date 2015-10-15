package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by astifter on 13.10.15.
 */
public class DrawableIcon implements Drawable {
    private final int index;
    private android.graphics.drawable.Drawable icon;
    private Rect dimensions;
    private Rect currentBounds;
    private int alignment;
    private boolean hidden;

    public DrawableIcon(int idx, android.graphics.drawable.Drawable drawable) {
        this.index = idx;
        create(drawable, Align.LEFT);
    }

    public DrawableIcon(int idx, android.graphics.drawable.Drawable drawable, int a) {
        this.index = idx;
        create(drawable, a);
    }

    private void create(android.graphics.drawable.Drawable drawable, int a) {
        icon = drawable;
        dimensions = new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        alignment = a;
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        currentBounds = new Rect(bounds);

        int targetWidth = currentBounds.height() * dimensions.width() / dimensions.height();
        if (hidden)
            targetWidth = 0;
        switch (this.alignment) {
            case Drawable.Align.CENTER:
                break;
            case Drawable.Align.LEFT:
                currentBounds.right = currentBounds.left + targetWidth;
                break;
            case Drawable.Align.RIGHT:
                currentBounds.left = currentBounds.right - targetWidth;
                break;
        }
        if (hidden) {
            currentBounds.bottom = currentBounds.top;
            return;
        }

        icon.setBounds(currentBounds);
        icon.draw(canvas);
    }

    @Override
    public float getHeight() {
        return currentBounds.height();
    }

    @Override
    public float getFutureHeight() {
        return 0;
    }

    @Override
    public float getWidth() {
        return currentBounds.width();
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {

    }

    @Override
    public void setAlpha(int a) {
        icon.setAlpha(a);
    }

    @Override
    public void hide(boolean hidden) {
        this.hidden = true;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public int getTouchedText(int x, int y) {
        if (this.currentBounds.contains(x, y))
            return index;
        return -1;
    }

    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public void setColor(int c) {

    }
}
