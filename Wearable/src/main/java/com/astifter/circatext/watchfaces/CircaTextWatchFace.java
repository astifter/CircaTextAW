package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.WindowInsets;

import com.astifter.circatext.CircaTextService;
import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.datahelpers.CircaTextStringerV1;
import com.astifter.circatext.datahelpers.CircaTextStringerV2;
import com.astifter.circatext.drawables.Animatable;
import com.astifter.circatext.drawables.AnimatableImpl;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.DrawableIcon;
import com.astifter.circatext.drawables.DrawableText;
import com.astifter.circatext.drawables.StackHorizontal;
import com.astifter.circatext.screens.Schedule;
import com.astifter.circatext.screens.Screen;
import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.CTU;

import java.util.HashMap;

public class CircaTextWatchFace extends BaseWatchFace {
    private volatile CircaTextStringer cts;
    private final HashMap<Integer, AnimatableImpl> topDrawable;
    private CTCs.Config currentConfig;
    private CTCs.Config selectedConfig;
    private boolean isRound = false;
    private Screen showScreen;
    // DEBUG OPTIONS
    private Drawable.RoundEmulation roundemulation = Drawable.RoundEmulation.NONE;

    public CircaTextWatchFace(CircaTextService.Engine parent) {
        super(parent);

        this.cts = new CircaTextStringerV1();
        fillCircaTexts();
        setTexts();

        topDrawable = new HashMap<>();
        selectedConfig = CTCs.Config.PLAIN;
    }

    @Override
    public void setMetrics(Resources r, WindowInsets insets) {
        super.setMetrics(r, insets);

        this.isRound = (this.roundemulation != Drawable.RoundEmulation.NONE) || insets.isRound();
        if (isRound) {
            if (this.roundemulation != Drawable.RoundEmulation.NONE) {
                if (this.roundemulation == Drawable.RoundEmulation.CHIN)
                    this.mBounds.bottom = 290;
            } else {
                this.mBounds.bottom -= insets.getStableInsetBottom();
            }
        }

        if (this.isRound) {
            createAnimatable(eTF.SHORTCAL, new Rect(5, 8, 80, 20), Drawable.Align.RIGHT, r, R.drawable.calendar)
                    .setConfig(CTCs.Config.PEEK, new Rect(50, -20, 50, -20), Drawable.Align.RIGHT)
                    .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
            createAnimatable(eTF.WEATHER_TEMP, new Rect(20, 8, 95, 20), Drawable.Align.LEFT, r, R.drawable.thermometer)
                    .setConfig(CTCs.Config.PEEK, new Rect(50, -20, 50, -20), Drawable.Align.LEFT)
                    .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
            createAnimatable(eCT.FIRST, new Rect(0, 17, 100, 43), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.TIME, new Rect(105, 17, 195, 43), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.PEEK, CTCs.Config.TIME);
            createAnimatable(eCT.SECOND, new Rect(0, 37, 100, 63), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.TIME, new Rect(105, 37, 195, 63), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.PEEK, CTCs.Config.TIME);
            createAnimatable(eCT.THIRD, new Rect(0, 57, 100, 83), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.TIME, new Rect(105, 57, 195, 83), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.PEEK, CTCs.Config.TIME);
            createAnimatable(eTF.HOUR, new Rect(-95, 22, -5, 75))
                    .setConfig(CTCs.Config.TIME, new Rect(0, 22, 100, 75), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.PEEK, new Rect(2, 10, 98, 75), Drawable.Align.CENTER);
            createAnimatable(eTF.SHORTDATE, new Rect(5, 80, 95, 95), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.PEEK, new Rect(2, 70, 98, 98), Drawable.Align.CENTER)
                    .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
        } else {
            int offset = 12;
            int height = (100 - (2 * offset)) / 3;
            int io = 2;

            createAnimatable(eTF.SHORTCAL, new Rect(5, 5, 95, 20), Drawable.Align.RIGHT, r, R.drawable.calendar)
                    .setConfig(CTCs.Config.PEEK, new Rect(95, -20, 95, -20), Drawable.Align.RIGHT)
                    .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
            createAnimatable(eTF.WEATHER_TEMP, new Rect(5, 5, 95, 20), Drawable.Align.LEFT, r, R.drawable.thermometer)
                    .setConfig(CTCs.Config.PEEK, new Rect(5, -20, 5, -20))
                    .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
            createAnimatable(eCT.FIRST, new Rect(5, 20, 95, 44), Drawable.Align.RIGHT)
                    .setConfig(CTCs.Config.TIME, new Rect(5, 80, 95, 87), Drawable.Align.LEFT)
                    .setConfig(CTCs.Config.PEEK, new Rect(5, offset - io, 98, offset + height + io), Drawable.Align.RIGHT);
            createAnimatable(eCT.SECOND, new Rect(5, 38, 95, 62), Drawable.Align.RIGHT)
                    .setConfig(CTCs.Config.TIME, new Rect(5, 84, 95, 91), Drawable.Align.LEFT)
                    .setConfig(CTCs.Config.PEEK, new Rect(5, offset + height - io, 98, offset + (height * 2) + io), Drawable.Align.RIGHT);
            createAnimatable(eCT.THIRD, new Rect(5, 56, 95, 80), Drawable.Align.RIGHT)
                    .setConfig(CTCs.Config.TIME, new Rect(5, 88, 95, 95), Drawable.Align.LEFT)
                    .setConfig(CTCs.Config.PEEK, new Rect(5, offset + (height * 2) - io, 98, 100 - offset + io), Drawable.Align.RIGHT);
            createAnimatable(eTF.HOUR, new Rect(5, 80, 95, 95))
                    .setConfig(CTCs.Config.PEEK, new Rect(2, 10, 98, 55))
                    .setConfig(CTCs.Config.TIME, new Rect(5, 25, 95, 75), Drawable.Align.CENTER);
            createAnimatable(eTF.SHORTDATE, new Rect(5, 80, 95, 95), Drawable.Align.RIGHT)
                    .setConfig(CTCs.Config.PEEK, new Rect(2, 57, 98, 90))
                    .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
        }
    }

    private Animatable createAnimatable(int textid, Rect fp, int fpa, Resources r, int drawableid) {
        DrawableText dt = new DrawableText(textid, mTexts);
        dt.autoSize(true);

        Drawable d;
        if (drawableid >= 0) {
            StackHorizontal tempstack = new StackHorizontal();
            DrawableIcon icon = new DrawableIcon(textid, r.getDrawable(drawableid, r.newTheme()), fpa, 20);
            if (fpa == Drawable.Align.LEFT || fpa == Drawable.Align.CENTER) {
                tempstack.add(icon);
                tempstack.add(dt);
            } else {
                tempstack.add(dt);
                tempstack.add(icon);
            }
            d = tempstack;
        } else {
            d = dt;
        }

        AnimatableImpl adi = new AnimatableImpl(this.parent, d);
        adi.setPosition(CTCs.Config.PLAIN, fp, fpa, this.mBounds);
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

        CTCs.Config newConfig;
        Rect r = new Rect(this.mBounds);
        if (this.peekCardPosition.isEmpty()) {
            newConfig = selectedConfig;
        } else {
            newConfig = CTCs.Config.PEEK;
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
        if (showScreen != null) {
            int idx = showScreen.getTouchedText(x, y);
            if (idx == Drawable.Touched.CLOSEME) {
                showScreen = null;
            }
        } else {
            int idx = Drawable.Touched.UNKNOWN;
            for (Drawable a : topDrawable.values()) {
                idx = a.getTouchedText(x, y);
                if (idx >= Drawable.Touched.FIRST)
                    break;
            }
            if (eCT.FIRST <= idx && idx <= eCT.THIRD || idx == eTF.HOUR) {
                if (currentConfig == CTCs.Config.PEEK)
                    return Drawable.Touched.FINISHED;
                if (selectedConfig == CTCs.Config.PLAIN)
                    selectedConfig = CTCs.Config.TIME;
                else if (selectedConfig == CTCs.Config.TIME)
                    selectedConfig = CTCs.Config.PLAIN;
                CTU.overwriteKeysInConfigDataMap(parent.mGoogleApiClient, CTCs.KEY_WATCHFACE_CONFIG, selectedConfig.toString());
                currentConfig = selectedConfig;
                for (int i = eCT.FIRST; i <= eCT.THIRD; i++) {
                    topDrawable.get(i).animateToConfig(currentConfig, this.mBounds);
                }
                topDrawable.get(eTF.HOUR).animateToConfig(currentConfig, this.mBounds);
            } else if (idx == eTF.SHORTCAL) {
                showScreen = new Schedule(this.resources, this.isRound, this.mMeetings, this.mBackgroundPaint.getColor());
            } else if (idx == eTF.WEATHER_TEMP) {
                //showScreen = new WeatherScreen(this.resources, this.mWeather);
            } else {
                return Drawable.Touched.FINISHED;
            }
        }
        parent.invalidate();
        return Drawable.Touched.FINISHED;
    }

    @Override
    public void setRoundMode(Drawable.RoundEmulation b) {
        this.roundemulation = b;
    }

    @Override
    public void setSelectedConfig(CTCs.Config cfg) {
        this.selectedConfig = cfg;
        if (this.currentConfig != CTCs.Config.PEEK) {
            currentConfig = selectedConfig;
            for (int i = eCT.FIRST; i <= eCT.THIRD; i++) {
                topDrawable.get(i).animateToConfig(currentConfig, this.mBounds);
            }
            topDrawable.get(eTF.HOUR).animateToConfig(currentConfig, this.mBounds);
        }
    }

    @Override
    public void setStringer(CTCs.Stringer cfg) {
        this.cts = null;
        switch(cfg) {
            case CIRCA:
                this.cts = new CircaTextStringerV1(); break;
            case RELAXED:
                this.cts = new CircaTextStringerV2(); break;
            case PRECISE:
                this.cts = new CircaTextStringerV1(true); break;
        }
    }

    @Override
    public void startTapHighlight() {
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        if (this.roundemulation != Drawable.RoundEmulation.NONE) {
            Paint c = new Paint();
            c.setColor(Color.BLACK);
            c.setAntiAlias(true);
            canvas.drawRect(bounds, c);

            Path clippingpath = new Path();
            clippingpath.addCircle(160, 160, 160, Path.Direction.CW);
            canvas.clipPath(clippingpath);
            if (this.roundemulation == Drawable.RoundEmulation.CHIN) {
                canvas.clipRect(this.mBounds, Region.Op.INTERSECT);
            }
        }

        canvas.drawRect(bounds, this.mBackgroundPaint);
        setTexts();
        fillCircaTexts();

        if (showScreen != null) {
            showScreen.onDraw(canvas, bounds);
        } else {
            for (AnimatableImpl a : topDrawable.values()) {
                a.onDraw(canvas, this.mBounds);
            }
        }
    }

    private void fillCircaTexts() {
        String[] circaTexts = this.cts.getString(this.mCalendar);
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
