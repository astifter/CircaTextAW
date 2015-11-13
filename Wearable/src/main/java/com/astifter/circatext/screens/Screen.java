package com.astifter.circatext.screens;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.StaticText;

import java.util.ArrayList;

public interface Screen {
    void onDraw(Canvas canvas, Rect bounds);

    int getTouchedText(int x, int y);

    ArrayList<Rect> getDrawnRects();
}
