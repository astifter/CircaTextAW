package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Created by astifter on 09.09.15.
 */
public interface CircaTextDrawable {
    void onDraw(Canvas canvas, Rect bounds);
    float getHeight();
    float getWidth();
    void setAmbientMode(boolean inAmbientMode);
    void setAlpha(int a);
    void hide();
    void show();
}

