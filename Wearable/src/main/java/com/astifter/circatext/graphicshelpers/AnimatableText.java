package com.astifter.circatext.graphicshelpers;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;

/**
 * Created by astifter on 01.10.15.
 */
public class AnimatableText extends AnimatableImpl {
    private final DrawableText drawable;

    public AnimatableText(CanvasWatchFaceService.Engine p, DrawableText d) {
        super(p, d);
        drawable = d;
    }

    @Override
    public void animateToConfig(Config c, Rect bounds) {
        Rect oldPosition = this.currentPosition;
        Rect newPosition = DrawingHelpers.percentageToRect(configs.get(c), bounds);
        float currentSize = drawable.getMaximumTextHeight(oldPosition);
        float targetSize = drawable.getMaximumTextHeight(newPosition);

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

    void setTextSize(float s) {
        drawable.setTextSize(s);
    }
}
