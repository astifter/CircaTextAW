package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.HashMap;

public interface CircaTextDrawable {
    enum Align {
        LEFT, RIGHT, CENTER
    };

    enum Positions {
        REGULAR, AMBIENT, PEEKCARD, AMBIENTPEEKCARD
    };

    enum Configurations {
        PLAIN, PEEK
    }

    void onDraw(Canvas canvas, Rect bounds);
    float getHeight();
    float getWidth();
    void setAmbientMode(boolean inAmbientMode);
    void setAlpha(int a);
    void hide();
    void show();
}

