package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface Drawable {
    /**
     * Draws the @ref Drawable on the canvas inside the bounds.
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
     * Make sure @{Drawable} is playing nicely with ambient mode.
     * @param inAmbientMode
     */
    void setAmbientMode(boolean inAmbientMode);

    void setAlpha(int a);

    /**
     * Disable drawing of @Drawable.
     */
    void hide();

    /**
     * Enable drawing of @Drawable.
     */
    void show();

    enum Align {
        LEFT, RIGHT, CENTER,
        TOP, MIDDLE, BOTTOM
    }

    enum Config {
        PLAIN, PEEK
    }
}

