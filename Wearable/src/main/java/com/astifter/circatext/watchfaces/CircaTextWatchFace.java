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

import java.util.HashMap;

public class CircaTextWatchFace extends BaseWatchFace {
    private final CircaTextStringer cts = new CircaTextStringerV2();
    private final HashMap<Integer, AnimatableImpl> topDrawable;
    private Drawable.Config currentConfig;
    private boolean roundemulation = true;

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
            this.mBounds.bottom = 280;
            DrawableText bt = createAnimatable(eTF.BATTERY, new Rect(55, 5, 95, 20),
                                                            new Rect(95, -20, 95, -5));
            createAnimatable(eTF.WEATHER_TEMP, new Rect(5, 5, 45, 20), Drawable.Align.RIGHT,
                                               new Rect(5, -20, 5, -20), Drawable.Align.RIGHT,
                                               resources, R.drawable.thermometer);

            int offset = 12; int height = (100 - (2 * offset)) / 3; int io = 2;
            DrawableText fl = createAnimatable(eCT.FIRST, new Rect(5, 20, 95, 40), new Rect(5, offset - io, 98, offset + height + io), Drawable.Align.CENTER);
            DrawableText sl = createAnimatable(eCT.SECOND, new Rect(5, 40, 95, 60), new Rect(5, offset + height - io, 98, offset + (height * 2) + io), Drawable.Align.CENTER);
            DrawableText tl = createAnimatable(eCT.THIRD, new Rect(5, 60, 95, 80), new Rect(5, offset + (height * 2) - io, 98, 100 - offset + io), Drawable.Align.CENTER);

            DrawableText hr = createAnimatable(eTF.HOUR, new Rect(5, 80, 45, 95), new Rect(2, 10, 95, 55), Drawable.Align.RIGHT);
            DrawableText sd = createAnimatable(eTF.SHORTDATE, new Rect(55, 80, 95, 95), Drawable.Align.LEFT,
                    new Rect(2, 57, 47, 90), Drawable.Align.LEFT);
        } else {
            DrawableText bt = createAnimatable(eTF.BATTERY, new Rect(5, 5, 95, 20),
                                                            new Rect(95, -20, 95, -5), Drawable.Align.RIGHT);
            createAnimatable(eTF.WEATHER_TEMP, new Rect(5, 5, 95, 20), Drawable.Align.LEFT,
                                               new Rect(5, -20, 5, -20), Drawable.Align.LEFT,
                                               resources, R.drawable.thermometer);

            int offset = 12; int height = (100 - (2 * offset)) / 3; int io = 2;
            DrawableText fl = createAnimatable(eCT.FIRST, new Rect(5, 20, 95, 40), new Rect(5, offset - io, 98, offset + height + io), Drawable.Align.RIGHT);
            DrawableText sl = createAnimatable(eCT.SECOND, new Rect(5, 40, 95, 60), new Rect(5, offset + height - io, 98, offset + (height * 2) + io), Drawable.Align.RIGHT);
            DrawableText tl = createAnimatable(eCT.THIRD, new Rect(5, 60, 95, 80), new Rect(5, offset + (height * 2) - io, 98, 100 - offset + io), Drawable.Align.RIGHT);

            DrawableText hr = createAnimatable(eTF.HOUR, new Rect(5, 80, 95, 95), new Rect(2, 10, 95, 55));
            DrawableText sd = createAnimatable(eTF.SHORTDATE, new Rect(5, 80, 95, 95), Drawable.Align.RIGHT,
                                                              new Rect(2, 57, 47, 90), Drawable.Align.LEFT);
        }
    }

    private DrawableText createAnimatable(int textid, Rect fp, int fpa, Rect sp, int spa, Resources res, int drawableid) {
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
        adi.setConfiguration(Drawable.Config.PEEK, sp, spa);
        topDrawable.put(textid, adi);

        return dt;
    }

    private DrawableText createAnimatable(int textid, Rect fp, int fpa, Rect sp, int spa) {
        return createAnimatable(textid, fp, fpa, sp, spa, null, -1);
    }
    private DrawableText createAnimatable(int textid, Rect fp, Rect sp, int a) {
        return createAnimatable(textid, fp, a, sp, a, null, -1);
    }
    private DrawableText createAnimatable(int textid, Rect fp, Rect sp) {
        return createAnimatable(textid, fp, sp, Drawable.Align.LEFT);
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
        Paint c = new Paint(); c.setColor(Color.BLACK); c.setAntiAlias(true);
        canvas.drawRect(bounds, c);
        if (this.roundemulation){
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
