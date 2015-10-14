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
    HashMap<Config, Rect> configs;

    MyRect currentPosition;
    private boolean hidden;
    private boolean debugging;

    public AnimatableImpl(CanvasWatchFaceService.Engine p, Drawable d) {
        configs = new HashMap<>();
        this.parent = p;
        drawable = d;
    }

    @Override
    public void setPosition(Config c, Rect position, Rect bounds) {
        configs.put(c, position);
        this.currentPosition = new MyRect(DrawingHelpers.percentageToRect(position, bounds));
        if (Log.isLoggable(TAG, Log.DEBUG) && debugging) {
            Log.d(TAG, String.format("AnimatableImpl: setPosition: %s", currentPosition.toString()));
        }
    }

    @Override
    public void setConfiguration(Config c, Rect position) {
        configs.put(c, position);
    }

    @Override
    public void animateToConfig(Config c, Rect bounds) {
        Rect oldPosition = this.currentPosition.toRect();
        Rect newPosition = DrawingHelpers.percentageToRect(configs.get(c), bounds);

        if (Log.isLoggable(TAG, Log.DEBUG) && debugging) {
            Log.d(TAG, String.format("AnimatableImpl: from %s to %s", oldPosition.toString(), newPosition.toString()));
        }
        PropertyValuesHolder animateLeft = PropertyValuesHolder.ofInt("Left", oldPosition.left, newPosition.left);
        PropertyValuesHolder animateTop = PropertyValuesHolder.ofInt("Top", oldPosition.top, newPosition.top);
        PropertyValuesHolder animateRight = PropertyValuesHolder.ofInt("Right", oldPosition.right, newPosition.right);
        PropertyValuesHolder animateBottom = PropertyValuesHolder.ofInt("Bottom", oldPosition.bottom, newPosition.bottom);

        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, animateLeft, animateTop, animateRight, animateBottom);
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
                if (Log.isLoggable(TAG, Log.DEBUG) && debugging) {
                    Log.d(TAG, String.format("AnimatableImpl: -- started"));
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Log.isLoggable(TAG, Log.DEBUG) && debugging) {
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

    public void setLeft(int l) {
        this.currentPosition.left = l;
    }

    public void setTop(int t) {
        this.currentPosition.top = t;
    }

    public void setRight(int l) {
        this.currentPosition.right = l;
    }

    public void setBottom(int l) {
        if (Log.isLoggable(TAG, Log.DEBUG) && debugging) {
            Log.d(TAG, String.format("AnimatableImpl: setBottom(int %d)", l));
        }
        this.currentPosition.bottom = l;
    }

    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            return;
        }
        if (Log.isLoggable(TAG, Log.DEBUG) && debugging) {
            Log.d(TAG, String.format("AnimatableImpl: onDraw: %s", currentPosition.toString()));
        }
        drawable.onDraw(canvas, currentPosition.toRect());
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

    public void enableDebug(boolean debug) {
        this.debugging = debug;
    }

    private class MyRect {
        private int right;
        private int top;
        private int bottom;
        private int left;

        public MyRect(Rect rect) {
            this.left = rect.left;
            this.right = rect.right;
            this.top = rect.top;
            this.bottom = rect.bottom;
        }

        public Rect toRect() {
            return new Rect(left, top, right, bottom);
        }

        public int height() {
            return bottom - top;
        }

        public int width() {
            return right - left;
        }

        public String toString() {
            return String.format("MyRect(%d,%d - %d,%d)", left, top, right, bottom);
        }
    }
}
