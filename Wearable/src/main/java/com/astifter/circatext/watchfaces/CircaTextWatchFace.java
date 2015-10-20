package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.datahelpers.CircaTextStringerV2;
import com.astifter.circatext.graphicshelpers.AnimatableImpl;
import com.astifter.circatext.graphicshelpers.Drawable;
import com.astifter.circatext.graphicshelpers.DrawableIcon;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatext.graphicshelpers.StackHorizontal;
import com.astifter.circatext.graphicshelpers.Animatable;

import java.util.HashMap;

public class CircaTextWatchFace extends BaseWatchFace {
    private final CircaTextStringer cts = new CircaTextStringerV2();
    private final HashMap<Integer, AnimatableImpl> topDrawable;
    private Drawable.Config currentConfig;
    private boolean roundemulation = false;

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);

        fillCircaTexts();
        setTexts();
        topDrawable = new HashMap<>();
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);
        if (this.roundemulation) {
//            this.mBounds.bottom = 280;
//            createAnimatable(eTF.BATTERY, new Rect(55, 5, 95, 20), new Rect(95, -20, 95, -5));
//            createAnimatable(eTF.WEATHER_TEMP, new Rect(5, 5, 45, 20), Drawable.Align.RIGHT,
//                                               new Rect(5, -20, 5, -20), Drawable.Align.RIGHT,
//                                               resources, R.drawable.thermometer);
//
//            int offset = 12; int height = (100 - (2 * offset)) / 3; int io = 2;
//            createAnimatable(eCT.FIRST, new Rect(5, 20, 95, 40), new Rect(5, offset - io, 98, offset + height + io), Drawable.Align.CENTER);
//            createAnimatable(eCT.SECOND, new Rect(5, 40, 95, 60), new Rect(5, offset + height - io, 98, offset + (height * 2) + io), Drawable.Align.CENTER);
//            createAnimatable(eCT.THIRD, new Rect(5, 60, 95, 80), new Rect(5, offset + (height * 2) - io, 98, 100 - offset + io), Drawable.Align.CENTER);
//
//            createAnimatable(eTF.HOUR, new Rect(5, 80, 45, 95), new Rect(2, 10, 95, 55), Drawable.Align.RIGHT);
//            createAnimatable(eTF.SHORTDATE, new Rect(55, 80, 95, 95), Drawable.Align.LEFT,
//                                            new Rect(2, 57, 47, 90), Drawable.Align.LEFT);
        } else {
            int offset = 12; int height = (100 - (2 * offset)) / 3; int io = 2;

            createAnimatable(eTF.BATTERY,        new Rect(5, 5, 95, 20), Drawable.Align.RIGHT)
                .setConfig(Drawable.Config.PEEK, new Rect(95, -20, 95, -5), Drawable.Align.RIGHT);
            createAnimatable(eTF.WEATHER_TEMP,   new Rect(5, 5, 95, 20), Drawable.Align.LEFT, resources, R.drawable.thermometer)
                .setConfig(Drawable.Config.PEEK, new Rect(5, -20, 5, -20));
            createAnimatable(eCT.FIRST,          new Rect(5, 20, 95, 40), Drawable.Align.RIGHT)
                .setConfig(Drawable.Config.PEEK, new Rect(5, offset - io, 98, offset + height + io), Drawable.Align.RIGHT);
            createAnimatable(eCT.SECOND,         new Rect(5, 40, 95, 60), Drawable.Align.RIGHT)
                .setConfig(Drawable.Config.PEEK, new Rect(5, offset + height - io, 98, offset + (height * 2) + io), Drawable.Align.RIGHT);
            createAnimatable(eCT.THIRD,          new Rect(5, 60, 95, 80), Drawable.Align.RIGHT)
                .setConfig(Drawable.Config.PEEK, new Rect(5, offset + (height * 2) - io, 98, 100 - offset + io), Drawable.Align.RIGHT);
            createAnimatable(eTF.HOUR,           new Rect(5, 80, 95, 95))
                .setConfig(Drawable.Config.PEEK, new Rect(2, 10, 95, 55));
            createAnimatable(eTF.SHORTDATE,      new Rect(5, 80, 95, 95), Drawable.Align.RIGHT)
                .setConfig(Drawable.Config.PEEK, new Rect(2, 57, 47, 90));
        }
    }

    private Animatable createAnimatable(int textid, Rect fp, int fpa, Resources res, int drawableid) {
        DrawableText dt = new DrawableText(textid, mTexts);
        dt.autoSize(true);

        Drawable d;
        if (drawableid >= 0) {
            StackHorizontal tempstack = new StackHorizontal();
            DrawableIcon icon = new DrawableIcon(textid, res.getDrawable(drawableid, res.newTheme()));
            tempstack.add(icon);
            tempstack.add(dt);
            d = tempstack;
        } else {
            d = dt;
        }

        AnimatableImpl adi = new AnimatableImpl(this.parent, d);
        adi.setPosition(Drawable.Config.PLAIN, fp, fpa, this.mBounds);
        topDrawable.put(textid, adi);

        return adi;
    }

    private Animatable createAnimatable(int textid, Rect fp, int fpa) {
        return createAnimatable(textid, fp, fpa, null, -1);
    }
    private Animatable createAnimatable(int textid, Rect fp) {
        return createAnimatable(textid, fp, Drawable.Align.LEFT);
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
            parent.invalidate();
        }
        return -1;
    }

    @Override
    public void startTapHighlight() {
        // TODO
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        if (this.roundemulation){
            Paint c = new Paint();
            c.setColor(Color.BLACK);
            c.setAntiAlias(true);
            canvas.drawRect(bounds, c);

            Path clippingpath = new Path();
            clippingpath.addCircle(160, 160, 160, Path.Direction.CW);
            canvas.clipPath(clippingpath);
            canvas.clipRect(this.mBounds, Region.Op.INTERSECT);
        }

        canvas.drawRect(bounds, this.mBackgroundPaint);
        setTexts();
        fillCircaTexts();
        for (AnimatableImpl a : topDrawable.values()) {
            a.onDraw(canvas, this.mBounds);
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
