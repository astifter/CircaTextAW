package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;

public class DrawableIcon implements Drawable {
    private final int index;
    private final int scale;
    private android.graphics.drawable.Drawable icon;
    private Rect dimensions;
    private Rect currentBounds;
    private int alignment;
    private boolean hidden;

    public DrawableIcon(int idx, android.graphics.drawable.Drawable drawable, int align, int scale) {
        this.index = idx;
        this.scale = scale;
        create(drawable, align);
    }

    private void create(android.graphics.drawable.Drawable drawable, int align) {
        icon = drawable;
        dimensions = new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        alignment = align;
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        currentBounds = new Rect(bounds);

        int targetWidth = currentBounds.height() * dimensions.width() / dimensions.height();
        if (hidden)
            targetWidth = 0;
        switch (this.alignment) {
            case Align.LEFT:
            case Align.CENTER:
                currentBounds.right = currentBounds.left + targetWidth;
                break;
            case Align.RIGHT:
                currentBounds.left = currentBounds.right - targetWidth;
                break;
        }
        if (hidden) {
            currentBounds.bottom = currentBounds.top;
            return;
        }
        Rect scaledbounds = new Rect(currentBounds);
        if (scale != 0) {
            int verticalinset = currentBounds.height() * scale / 200;
            int horizontalinset = currentBounds.width() * scale / 200;
            scaledbounds.inset(horizontalinset, verticalinset);
        }
        icon.setBounds(scaledbounds);
        icon.draw(canvas);
    }

    @Override
    public float getHeight() {
        if (currentBounds == null) return 0;
        return currentBounds.height();
    }

    @Override
    public float getFutureHeight() {
        return getHeight();
    }

    @Override
    public float getWidth() {
        if (currentBounds == null) return 0;
        return currentBounds.width();
    }

    public float getFutureWidth() {
        return getWidth();
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
        return Touched.UNKNOWN;
    }

    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public void setColor(int c) {

    }

    @Override
    public void setAlignment(int a) {
        // TODO not implemented
    }
}
