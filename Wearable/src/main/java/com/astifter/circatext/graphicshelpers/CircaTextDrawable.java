package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface CircaTextDrawable {
    /**
     * Draws the @ref CircaTextDrawable on the canvas inside the bounds.
     * @param canvas
     * @param bounds
     */
    void onDraw(Canvas canvas, Rect bounds);

    /**
     * Gets the drawn height after the last onDraw() command.
     * @return The height of the last drawn rectangle.
     */
    float getHeight();

    /**
     * As getHeight().
     * @return The width of the last drawn rectangle.
     */
    float getWidth();

    /**
     * Make sure @{CircaTextDrawable} is playing nicely with ambient mode.
     * @param inAmbientMode
     */
    void setAmbientMode(boolean inAmbientMode);

    void setAlpha(int a);

    /**
     * Disable drawing of @CircaTextDrawable.
     */
    void hide();

    /**
     * Enable drawing of @CircaTextDrawable.
     */
    void show();

    enum Align {
        LEFT, RIGHT, CENTER,
        TOP, MIDDLE, BOTTOM
    }

    enum Configurations {
        PLAIN, PEEK
    }
}

