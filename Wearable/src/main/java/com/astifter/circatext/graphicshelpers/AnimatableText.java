package com.astifter.circatext.graphicshelpers;

import android.animation.Animator;
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
public class AnimatableText implements CircaTextDrawable {
    private final DrawableText drawableText;
    private final CanvasWatchFaceService.Engine parent;
    HashMap<CircaTextDrawable.Configurations, Rect> configs;
    private CircaTextDrawable.Configurations currentConfig;
    private Rect currentPosition;

    public AnimatableText(CanvasWatchFaceService.Engine p, int i, HashMap<Integer, String> t) {
        configs = new HashMap<>();
        this.parent = p;
        drawableText = new DrawableText(i, t);
    }

    public void setPosition(Configurations c, Rect position, Rect bounds) {
        this.currentConfig = c;
        configs.put(c, position);

        this.currentPosition = DrawingHelpers.percentageToRect(position, bounds);
    }

    public void setConfiguration(Configurations c, Rect position) {
        configs.put(c, position);
    }

    public void animateToConfig(Configurations c, Rect bounds) {
        Rect oldPosition = this.currentPosition;
        Rect newPosition = DrawingHelpers.percentageToRect(configs.get(c), bounds);
        float currentSize = drawableText.getMaximumTextHeight(oldPosition);
        float targetSize = drawableText.getMaximumTextHeight(newPosition);

        PropertyValuesHolder animateSize = PropertyValuesHolder.ofFloat("TextSize", new float[]{currentSize, targetSize});
        PropertyValuesHolder animateLeft = PropertyValuesHolder.ofInt("Left", oldPosition.left, newPosition.left);
        PropertyValuesHolder animateTop = PropertyValuesHolder.ofInt("Top", oldPosition.top, newPosition.top);

        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, animateSize, animateLeft, animateTop);
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

    public void setTextSize(float t) {
        drawableText.setTextSize(t);
    }
    public void setLeft(int l) {
        this.currentPosition.left = l;
    }
    public void setTop(int t) {
        this.currentPosition.top = t;
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        drawableText.onDraw(canvas, currentPosition);
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {

    }

    @Override
    public void setAlpha(int a) {

    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }
}
