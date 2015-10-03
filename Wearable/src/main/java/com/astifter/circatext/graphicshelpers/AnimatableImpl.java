package com.astifter.circatext.graphicshelpers;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;

import java.util.HashMap;

/**
 * Created by astifter on 01.10.15.
 */
public class AnimatableImpl implements Drawable, Animatable {
    protected final Drawable drawable;
    protected final CanvasWatchFaceService.Engine parent;

    protected HashMap<Config, Rect> configs;
    protected Config currentConfig;

    protected Rect currentPosition;
    private boolean hidden;

    public AnimatableImpl(CanvasWatchFaceService.Engine p, Drawable d) {
        configs = new HashMap<>();
        this.parent = p;
        drawable = d;
    }

    @Override
    public void setPosition(Config c, Rect position, Rect bounds) {
        this.currentConfig = c;
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

        currentConfig = c;
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
            this.currentPosition.bottom = this.currentPosition.top;
            this.currentPosition.right = this.currentPosition.left;
            return;
        }
        drawable.onDraw(canvas, currentPosition);
    }

    @Override
    public float getHeight() {
        return this.currentPosition.height();
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
    public void hide() {
        this.hidden = true;
    }

    @Override
    public void show() {
        this.hidden = false;

    }
}
