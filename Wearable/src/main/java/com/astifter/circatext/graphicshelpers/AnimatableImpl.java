package com.astifter.circatext.graphicshelpers;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.util.HashMap;

public class AnimatableImpl implements Drawable, Animatable {
    private static final String TAG = "Drawable";

    final CanvasWatchFaceService.Engine parent;
    private final Drawable drawable;
    HashMap<Config, Position> configs;

    Position currentPosition;
    private boolean hidden;

    public AnimatableImpl(CanvasWatchFaceService.Engine p, Drawable d) {
        configs = new HashMap<>();
        this.parent = p;
        drawable = d;
    }

    @Override
    public void setPosition(Config c, Rect position, int alignment, Rect bounds) {
        Position p = new Position(position, alignment);
        configs.put(c, p);
        this.currentPosition = Position.percentagePosition(p, bounds);
    }

    @Override
    public void setConfiguration(Config c, Rect position, int alignment) {
        configs.put(c, new Position(position, alignment));
    }

    @Override
    public void animateToConfig(Config c, Rect bounds) {
        Position op = this.currentPosition;
        Position np = Position.percentagePosition(configs.get(c), bounds);

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format("AnimatableImpl: from %s to %s", op.toString(), np.toString()));
        }
        PropertyValuesHolder animateLeft = PropertyValuesHolder.ofInt("Left", op.left(), np.left());
        PropertyValuesHolder animateTop = PropertyValuesHolder.ofInt("Top", op.top(), np.top());
        PropertyValuesHolder animateRight = PropertyValuesHolder.ofInt("Right", op.right(), np.right());
        PropertyValuesHolder animateBottom = PropertyValuesHolder.ofInt("Bottom", op.bottom(), np.bottom());
        PropertyValuesHolder animateAlign = PropertyValuesHolder.ofInt("Alignment", op.align(), np.align());

        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, animateLeft, animateTop, animateRight, animateBottom, animateAlign);
        anim.setDuration(100);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, String.format("AnimatableImpl: -- started"));
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, String.format("AnimatableImpl: -- ended"));
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    @Override
    public void setLeft(int l) {
        this.currentPosition.setLeft(l);
    }

    @Override
    public void setTop(int l) {
        this.currentPosition.setTop(l);
    }

    @Override
    public void setRight(int l) {
        this.currentPosition.setRight(l);
    }

    @Override
    public void setBottom(int l) {
        this.currentPosition.setBottom(l);
    }

    @Override
    public void setAlignment(int a) {
        this.currentPosition.setAlign(a);
        drawable.setAlignment(a);
    }

    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            return;
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format("AnimatableImpl: onDraw: %s", currentPosition.toString()));
        }
        drawable.onDraw(canvas, currentPosition.toRect());
    }

    @Override
    public float getHeight() { return this.currentPosition.height(); }

    @Override
    public float getFutureHeight() {
        return getHeight();
    }

    @Override
    public float getWidth() { return this.currentPosition.width(); }

    @Override
    public float getFutureWidth() { return getWidth(); }

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

    @Override
    public int getTouchedText(int x, int y) {
        return drawable.getTouchedText(x, y);
    }

    @Override
    public int getColor() {
        return drawable.getColor();
    }

    @Override
    public void setColor(int c) {
        drawable.setColor(c);
    }
}
