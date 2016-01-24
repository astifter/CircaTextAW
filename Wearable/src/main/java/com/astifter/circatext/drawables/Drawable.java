package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;

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

    int getTouchedText(int x, int y);

    ArrayList<Rect> getDrawnRects();

    enum RoundEmulation {
        NONE,
        CIRCULAR,
        CHIN
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