package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

/**
 * Created by astifter on 02.10.15.
 */
public interface Animatable {
    void setPosition(Drawable.Config c, Rect position, Rect bounds);

    void setConfiguration(Drawable.Config c, Rect position);

    void animateToConfig(Drawable.Config c, Rect bounds);

    void setLeft(int l);

    void setTop(int t);
}
