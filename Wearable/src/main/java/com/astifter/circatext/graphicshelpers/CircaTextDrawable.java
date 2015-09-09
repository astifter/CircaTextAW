package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by astifter on 09.09.15.
 */
public interface CircaTextDrawable {
    void onDraw(Canvas canvas, Rect bounds);
    float getHeight();
    float getWidth();
    void setAmbientMode(boolean inAmbientMode);
    void setColor(int c);
    void setAlpha(int a);
}

