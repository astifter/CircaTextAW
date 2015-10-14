package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by astifter on 13.10.15.
 */
public class DrawableIcon implements Drawable {
    private android.graphics.drawable.Drawable icon;
    private Rect dimensions;
    private Rect currentBounds;
    private Drawable.Align alignment;
    private boolean hidden;

    public DrawableIcon(android.graphics.drawable.Drawable drawable) {
        create(drawable, Align.LEFT);
    }

    public DrawableIcon(android.graphics.drawable.Drawable drawable, Align a) {
        create(drawable, a);
    }

    private void create(android.graphics.drawable.Drawable drawable, Align a) {
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
            case CENTER:
                break;
            case LEFT:
                currentBounds.right = currentBounds.left + targetWidth;
                break;
            case RIGHT:
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
