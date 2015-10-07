package com.astifter.circatext.graphicshelpers;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;

import java.util.HashMap;

public class AnimatableImpl implements Drawable, Animatable {
    final CanvasWatchFaceService.Engine parent;
    private final Drawable drawable;
    HashMap<Config, Rect> configs;

    Rect currentPosition;
    private boolean hidden;

    AnimatableImpl(CanvasWatchFaceService.Engine p, Drawable d) {
        configs = new HashMap<>();
        this.parent = p;
        drawable = d;
    }

    @Override
    public void setPosition(Config c, Rect position, Rect bounds) {
        configs.put(c, position);

        this.currentPosition = DrawingHelpers.percentageToRect(position, bounds);
    }

    @Override
    public void setConfiguration(Config c, Rect position) {
        configs.put(c, position);
    }

    @Override
    public void animateToConfig(Config c, Rect bounds) {
        Rect oldPosition = this.currentPosition;
        Rect newPosition = DrawingHelpers.percentageToRect(configs.get(c), bounds);

        PropertyValuesHolder animateLeft = PropertyValuesHolder.ofInt("Left", oldPosition.left, newPosition.left);
        PropertyValuesHolder animateTop = PropertyValuesHolder.ofInt("Top", oldPosition.top, newPosition.top);

        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, animateLeft, animateTop);
        anim.setDuration(250);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        anim.start();
    }

    public void setLeft(int l) {
        this.currentPosition.left = l;
    }

    public void setTop(int t) {
        this.currentPosition.top = t;
    }

    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            return;
        }
        drawable.onDraw(canvas, currentPosition);
    }

    @Override
    public float getHeight() {
        return this.currentPosition.height();
    }

    @Override
    public float getFutureHeight() {
        return getHeight();
    }

    @Override
    public float getWidth() {
        return this.currentPosition.width();
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        this.drawable.setAmbientMode(inAmbientMode);
    }

    @Override
    public void setAlpha(int a) {
        this.drawable.setAlpha(a);
    }

    @Override
    public void hide(boolean h) {
        this.hidden = h;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }
}
