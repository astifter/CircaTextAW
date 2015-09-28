package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.graphicshelpers.CircaTextDrawable;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatext.graphicshelpers.LayoutDrawable;

public class CircaTextWatchFace extends BaseWatchFace {
    LayoutDrawable topDrawable = null;

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);
        DrawableText hour = topDrawable.addText(mTexts, eTF.HOUR);
        hour.addPositionPerc(CircaTextDrawable.Positions.REGULAR, new Rect(5, 10, 95, 40));
    }

    @Override
    protected void updateVisibilty() {

    }

    @Override
    public void startTapHighlight() {

    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        if (topDrawable == null) return;
    }
}
