package com.astifter.circatext.screens;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface Screen {
    void onDraw(Canvas canvas, Rect bounds);

    int getTouchedText(int x, int y);
}
