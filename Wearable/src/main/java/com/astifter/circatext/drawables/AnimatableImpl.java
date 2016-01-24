package com.astifter.circatext.drawables;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.astifter.circatext.datahelpers.Position;
import com.astifter.circatextutils.CTCs;

import java.util.ArrayList;
import java.util.HashMap;

public class AnimatableImpl implements Drawable, Animatable {
    private static final String TAG = "Drawable";

    private final CanvasWatchFaceService.Engine parent;
    private final Drawable drawable;
    private final HashMap<CTCs.Config, Position> configs;

    private Position currentPosition;

    public AnimatableImpl(CanvasWatchFaceService.Engine p, Drawable d) {
        configs = new HashMap<>();
        this.parent = p;
        drawable = d;
    }

    @Override
    public void setPosition(CTCs.Config c, Rect position, int alignment, Rect bounds) {
        Position p = new Position(position, alignment);
        configs.put(c, p);
        this.currentPosition = Position.percentagePosition(p, bounds);
    }

    private Animatable setConfig(CTCs.Config c, Position p) {
        configs.put(c, p);
        return this;
    }

    @Override
    public Animatable setConfig(CTCs.Config c, Rect position, int alignment) {
        return setConfig(c, new Position(position, alignment));
    }

    @Override
    public Animatable setConfig(CTCs.Config c, Rect position) {
        return setConfig(c, position, Drawable.Align.LEFT);
    }

    @Override
    public Animatable setConfig(CTCs.Config c, CTCs.Config o) {
        return setConfig(c, configs.get(o));
    }

    @Override
    public void animateToConfig(CTCs.Config c, Rect bounds) {
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
                    Log.d(TAG, "AnimatableImpl: -- started");
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "AnimatableImpl: -- ended");
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
    public void animateAlpha(int from, int to, Animator.AnimatorListener l) {
        PropertyValuesHolder animateAlpha = PropertyValuesHolder.ofInt("Alpha", from, to);

        ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, animateAlpha);
        anim.setDuration(100);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        if (l != null)
            anim.addListener(l);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    @Override
    public void setAlignment(int a) {
        this.currentPosition.setAlign(a);
        drawable.setAlignment(a);
    }

    @Override
    public void onDraw(Canvas canvas, Rect b) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
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
    public float getFutureWidth() {
        return getWidth();
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
    public int getTouchedText(int x, int y) {
        return drawable.getTouchedText(x, y);
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return drawable.getDrawnRects();
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
