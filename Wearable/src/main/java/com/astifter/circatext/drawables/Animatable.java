package com.astifter.circatext.drawables;

import android.animation.Animator;
import android.graphics.Rect;

import com.astifter.circatextutils.CTCs;

public interface Animatable {
    void setPosition(CTCs.Config c, Rect position, int alignment, Rect bounds);

    Animatable setConfig(CTCs.Config c, Rect position, int alignment);

    Animatable setConfig(CTCs.Config c, Rect position);

    Animatable setConfig(CTCs.Config c, CTCs.Config o);

    void animateToConfig(CTCs.Config c, Rect bounds);

    void setLeft(int l);

    void setTop(int t);

    void setRight(int l);

    void setBottom(int t);

    void animateAlpha(int from, int to, Animator.AnimatorListener l);
}
