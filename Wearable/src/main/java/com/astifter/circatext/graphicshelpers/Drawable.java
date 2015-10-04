package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface Drawable {
    /**
     * Draws the {@link Drawable} on the canvas inside the bounds.
     *
     * @param canvas The canvas to draw on.
     * @param bounds The rectangle to draw in.
     */
    void onDraw(Canvas canvas, Rect bounds);

    /**
     * Gets the drawn height after the last onDraw() command.
     *
     * @return The height of the last drawn rectangle.
     */
    float getHeight();

    float getFutureHeight();

    /**
     * As getHeight().
     *
     * @return The width of the last drawn rectangle.
     */
    float getWidth();

    /**
     * Make sure {@link Drawable} is playing nicely with ambient mode.
     *
     * @param inAmbientMode Wether or not watch is in ambient mode.
     */
    void setAmbientMode(boolean inAmbientMode);

    void setAlpha(int a);

    /**
     * Disable drawing of {@link Drawable}.
     */
    void hide();

    /**
     * Enable drawing of {@link Drawable}.
     */
    void show();

    enum Align {
        LEFT, RIGHT, CENTER
    }

    enum Config {
        PLAIN, PEEK
    }
}

