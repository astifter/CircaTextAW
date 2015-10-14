package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.datahelpers.CircaTextStringerV2;
import com.astifter.circatext.graphicshelpers.AnimatableImpl;
import com.astifter.circatext.graphicshelpers.AnimatableText;
import com.astifter.circatext.graphicshelpers.Drawable;
import com.astifter.circatext.graphicshelpers.DrawableText;

import java.util.HashMap;

public class CircaTextWatchFace extends BaseWatchFace {
    private final CircaTextStringer cts = new CircaTextStringerV2();
    private final HashMap<Integer, AnimatableImpl> topDrawable;
    private Drawable.Config currentConfig;

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);

        fillCircaTexts();
        setTexts();
        topDrawable = new HashMap<>();
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);

        DrawableText hr = createAnimatable(eTF.HOUR,      new Rect(5, 80, 95, 95), new Rect(5, 25, 95, 75));
        DrawableText sd = createAnimatable(eTF.SHORTDATE, new Rect(5, 80, 95, 95), new Rect(5, 72, 95, 95));
        sd.setAlignment(Drawable.Align.RIGHT);
        DrawableText fl = createAnimatable(eCT.FIRST,     new Rect(5, 15, 95, 42), new Rect(5, 5, 95, 28));
        fl.setAlignment(Drawable.Align.RIGHT);
        DrawableText sl = createAnimatable(eCT.SECOND,    new Rect(5, 36, 95, 63), new Rect(5, 27, 95, 50));
        sl.setAlignment(Drawable.Align.RIGHT);
        DrawableText tl = createAnimatable(eCT.THIRD,     new Rect(5, 57, 95, 84), new Rect(5, 50, 95, 73));
        tl.setAlignment(Drawable.Align.RIGHT);
        DrawableText bt = createAnimatable(eTF.BATTERY,      new Rect(5, 5, 95, 20), new Rect(5, -20, 5, -5));
        DrawableText wt = createAnimatable(eTF.WEATHER_TEMP, new Rect(5, 5, 95, 20), new Rect(95, -20, 95, -5));
        wt.setAlignment(Drawable.Align.RIGHT);
    }

    private DrawableText createAnimatable(int idx, Rect fp, Rect sp) {
        DrawableText dt = new DrawableText(idx, mTexts);
        AnimatableImpl a = new AnimatableText(this.parent, dt);
        a.setPosition(Drawable.Config.PLAIN, fp, this.mBounds);
        a.setConfiguration(Drawable.Config.PEEK, sp);
        topDrawable.put(idx, a);
        return dt;
    }

    @Override
    public void updateVisibilty() {
        for (AnimatableImpl a : topDrawable.values()) {
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
            for (AnimatableImpl a : topDrawable.values()) {
                a.animateToConfig(currentConfig, r);
            }
        }
    }

    @Override
    public int getTouchedText(int x, int y) {
        int idx = -1;
        for (Drawable a : topDrawable.values()) {
            idx = a.getTouchedText(x, y);
            if (idx >= 0)
                break;
        }
        if (idx >= 0) {
            AnimatableImpl dt = topDrawable.get(idx);
            if (dt.getColor() == Color.GREEN) {
                dt.setColor(Color.WHITE);
            } else {
                dt.setColor(Color.GREEN);
            }
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
        for (AnimatableImpl a : topDrawable.values()) {
            a.onDraw(canvas, bounds);
        }
    }

    private void fillCircaTexts() {
        String[] circaTexts = cts.getString();
        for (int i = eCT.FIRST; i < eCT.SIZE; i++) {
            mTexts.put(i, "");
        }
        if (circaTexts.length == 1) {
            mTexts.put(eCT.SECOND, circaTexts[0]);
        } else {
            for (int i = 0; i < circaTexts.length; i++) {
                mTexts.put(eCT.FIRST + i, circaTexts[i]);
            }
        }
    }

    class eCT {
        public static final int FIRST = eTF.SIZE;
        public static final int SECOND = FIRST + 1;
        public static final int THIRD = SECOND + 1;
        public static final int SIZE = THIRD + 1;
    }
}
