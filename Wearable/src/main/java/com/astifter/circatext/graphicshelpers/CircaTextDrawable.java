package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface CircaTextDrawable {
    void onDraw(Canvas canvas, Rect bounds);
    float getHeight();
    float getWidth();
    void setAmbientMode(boolean inAmbientMode);
    void setAlpha(int a);
    void hide();
    void show();
}

