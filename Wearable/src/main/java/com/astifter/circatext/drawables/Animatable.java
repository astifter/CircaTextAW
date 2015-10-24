package com.astifter.circatext.drawables;

import android.animation.Animator;
import android.graphics.Rect;

public interface Animatable {
    void setPosition(Drawable.Config c, Rect position, int alignment, Rect bounds);

    Animatable setConfig(Drawable.Config c, Rect position, int alignment);

    Animatable setConfig(Drawable.Config c, Rect position);

    Animatable setConfig(Drawable.Config c, Drawable.Config o);

    void animateToConfig(Drawable.Config c, Rect bounds);

    void setLeft(int l);

    void setTop(int t);

    void setRight(int l);

    void setBottom(int t);

    void animateAlpha(int from, int to);
    void animateAlpha(int from, int to, Animator.AnimatorListener l);
}
