package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

public class CircaTextWatchFace extends BaseWatchFace {
    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);
   }

    @Override
    public void setPeekCardPosition(Rect rect) {
        super.setPeekCardPosition(rect);
    }

    @Override
    protected void updateVisibilty() {

    }

    @Override
    public void startTapHighlight() {

    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        //if (topDrawable == null) return;
        //topDrawable.onDraw(canvas, bounds);
    }
}
