package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

public interface Animatable {
    void setPosition(Drawable.Config c, Rect position, int alignment, Rect bounds);

    void setConfig(Drawable.Config c, Rect position, int alignment);
    void setConfig(Drawable.Config c, Rect position);

    void animateToConfig(Drawable.Config c, Rect bounds);

    void setLeft(int l);
    void setTop(int t);
    void setRight(int l);
    void setBottom(int t);
}
