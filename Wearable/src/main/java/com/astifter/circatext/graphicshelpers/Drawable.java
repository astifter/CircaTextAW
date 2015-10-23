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

    float getFutureWidth();

    /**
     * Make sure {@link Drawable} is playing nicely with ambient mode.
     *
     * @param inAmbientMode Wether or not watch is in ambient mode.
     */
    void setAmbientMode(boolean inAmbientMode);

    void setAlpha(int a);

    int getColor();

    void setColor(int c);

    void setAlignment(int a);

    /**
     * Disable drawing of {@link Drawable}.
     */
    void hide(boolean hidden);

    boolean isHidden();

    int getTouchedText(int x, int y);

    enum Config {
        PLAIN, PEEK, TIME
    }

    interface Align {
        int LEFT = 0;
        int CENTER = 1;
        int RIGHT = 2;
    }

    interface Touched {
        int CLOSEME = -2;
        int UNKNOWN = -1;
        int FINISHED = -3;
        int FIRST = 0;
    }
}

