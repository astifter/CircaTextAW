package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.datahelpers.CircaTextStringerV2;
import com.astifter.circatext.graphicshelpers.AnimatableImpl;
import com.astifter.circatext.graphicshelpers.AnimatableText;
import com.astifter.circatext.graphicshelpers.Drawable;
import com.astifter.circatext.graphicshelpers.DrawableText;

import java.util.ArrayList;
import java.util.HashMap;

public class CircaTextWatchFace extends BaseWatchFace {
    private final HashMap<Integer, String> mCirca = new HashMap<>();
    private final CircaTextStringer cts = new CircaTextStringerV2();
    private final ArrayList<AnimatableImpl> topDrawable;
    private Drawable.Config currentConfig;

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);
        for (int i = eCT.FIRST; i < eCT.SIZE; i++) {
            mCirca.put(i, "");
        }
        topDrawable = new ArrayList<>();
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);

        {
            AnimatableImpl a = new AnimatableText(this.parent, new DrawableText(eTF.HOUR, mTexts));
            a.setPosition(Drawable.Config.PLAIN, new Rect(5, 5, 95, 20), this.mBounds);
            a.setConfiguration(Drawable.Config.PEEK, new Rect(5, 20, 95, 80));
            topDrawable.add(a);
        }
        {
            DrawableText dt = new DrawableText(eTF.SHORTDATE, mTexts);
            dt.setAlignment(Drawable.Align.RIGHT);
            AnimatableImpl a = new AnimatableText(this.parent, dt);
            a.setPosition(Drawable.Config.PLAIN, new Rect(5, 80, 95, 95), this.mBounds);
            a.setConfiguration(Drawable.Config.PEEK, new Rect(5, 72, 95, 95));
            topDrawable.add(a);
        }
        {
            DrawableText dt = new DrawableText(eCT.FIRST, mCirca);
            dt.setAlignment(Drawable.Align.RIGHT);
            AnimatableImpl a = new AnimatableText(this.parent, dt);
            a.setPosition(Drawable.Config.PLAIN, new Rect(5, 15, 95, 42), this.mBounds);
            a.setConfiguration(Drawable.Config.PEEK, new Rect(5, 5, 95, 28));
            topDrawable.add(a);
        }
        {
            DrawableText dt = new DrawableText(eCT.SECOND, mCirca);
            dt.setAlignment(Drawable.Align.RIGHT);
            AnimatableImpl a = new AnimatableText(this.parent, dt);
            a.setPosition(Drawable.Config.PLAIN, new Rect(5, 36, 95, 63), this.mBounds);
            a.setConfiguration(Drawable.Config.PEEK, new Rect(5, 27, 95, 50));
            topDrawable.add(a);
        }
        {
            DrawableText dt = new DrawableText(eCT.THIRD, mCirca);
            dt.setAlignment(Drawable.Align.RIGHT);
            AnimatableImpl a = new AnimatableText(this.parent, dt);
            a.setPosition(Drawable.Config.PLAIN, new Rect(5, 57, 95, 84), this.mBounds);
            a.setConfiguration(Drawable.Config.PEEK, new Rect(5, 50, 95, 73));
            topDrawable.add(a);
        }
    }

    @Override
    public void updateVisibilty() {
        for (AnimatableImpl a : topDrawable) {
            a.setAmbientMode(this.ambientMode);
        }

        Drawable.Config newConfig;
        Rect r = new Rect(this.mBounds);
        if (this.peekCardPosition.isEmpty()) {
            newConfig = Drawable.Config.PLAIN;
        } else {
            newConfig = Drawable.Config.PEEK;
            r.bottom = this.peekCardPosition.top;
        }
        if (newConfig != currentConfig) {
            currentConfig = newConfig;
            for (AnimatableImpl a : topDrawable) {
                a.animateToConfig(currentConfig, r);
            }
        }
    }

    @Override
    public int getTouchedText(int x, int y) {
        for (Drawable a : topDrawable) {
            int idx = a.getTouchedText(x, y);
            if (idx != -1)
                return idx;
        }
        return -1;
    }

    @Override
    public void startTapHighlight() {
        // TODO
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        canvas.drawRect(this.mBounds, this.mBackgroundPaint);
        setTexts();
        fillCircaTexts();
        for (AnimatableImpl a : topDrawable) {
            a.onDraw(canvas, bounds);
        }
    }

    private void fillCircaTexts() {
        String[] circaTexts = cts.getString();
        for (int i = eCT.FIRST; i < eCT.SIZE; i++) {
            mCirca.put(i, "");
        }
        if (circaTexts.length == 1) {
            mCirca.put(eCT.SECOND, circaTexts[0]);
        } else {
            for (int i = eCT.FIRST; i < circaTexts.length; i++) {
                mCirca.put(i, circaTexts[i]);
            }
        }
    }

    class eCT {
        public static final int FIRST = 0;
        public static final int SECOND = FIRST + 1;
        public static final int THIRD = SECOND + 1;
        public static final int SIZE = THIRD + 1;
    }
}
