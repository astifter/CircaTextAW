package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.graphicshelpers.AnimatableText;
import com.astifter.circatext.graphicshelpers.CircaTextDrawable;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;

public class CircaTextWatchFace extends BaseWatchFace {
    private final AnimatableText topDrawable;
    private CircaTextDrawable.Configurations currentConfig;

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);
        topDrawable = new AnimatableText(this.parent, eTF.HOUR, mTexts);
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);
        topDrawable.setPosition(CircaTextDrawable.Configurations.PLAIN,
                                new Rect(5,15,95,62),
                                this.mBounds);
        topDrawable.setConfiguration(CircaTextDrawable.Configurations.PEEK,
                                     new Rect(45,5,95,55));
    }

    @Override
    protected void updateVisibilty() {
        CircaTextDrawable.Configurations newConfig;
        Rect r = new Rect(this.mBounds);
        if (this.peekCardPosition.isEmpty()) {
            newConfig = CircaTextDrawable.Configurations.PLAIN;
        } else {
            newConfig = CircaTextDrawable.Configurations.PEEK;
            r.bottom = this.peekCardPosition.top;
        }
        if (newConfig != currentConfig) {
            currentConfig = newConfig;
            topDrawable.animateToConfig(currentConfig, r);
        }
    }

    @Override
    public void startTapHighlight() {

    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        canvas.drawRect(this.mBounds, this.mBackgroundPaint);
        setTexts();
        topDrawable.onDraw(canvas, bounds);
    }
}
