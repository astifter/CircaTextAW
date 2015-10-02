package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;

/**
 * Created by astifter on 02.10.15.
 */
public interface CircaTextAnimatable {
    void setPosition(CircaTextDrawable.Configurations c, Rect position, Rect bounds);

    void setConfiguration(CircaTextDrawable.Configurations c, Rect position);

    void animateToConfig(CircaTextDrawable.Configurations c, Rect bounds);

    void setLeft(int l);

    void setTop(int t);
}
