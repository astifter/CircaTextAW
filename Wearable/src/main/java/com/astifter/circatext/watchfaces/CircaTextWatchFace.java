package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.view.WindowInsets;

import com.astifter.circatext.CircaTextService;
import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.datahelpers.CircaTextStringerV1;
import com.astifter.circatext.datahelpers.CircaTextStringerV2;
import com.astifter.circatext.drawables.Animatable;
import com.astifter.circatext.drawables.AnimatableImpl;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.DrawableHelpers;
import com.astifter.circatext.drawables.DrawableIcon;
import com.astifter.circatext.drawables.DrawableText;
import com.astifter.circatext.drawables.StackHorizontal;
import com.astifter.circatext.screens.Schedule;
import com.astifter.circatext.screens.Screen;
import com.astifter.circatext.screens.WeatherScreen;
import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.CTU;
import com.astifter.circatextutils.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class CircaTextWatchFace implements WatchFace {
    private final HashMap<Integer, String> mTexts = new HashMap<>();
    private final CircaTextService.Engine parent;
    private final HashMap<Integer, AnimatableImpl> topDrawable = new HashMap<>();
    private CalendarHelper.EventInfo[] mMeetings;
    private Weather mWeather = null;
    private Date mWeatherReq;
    private Date mWeatherRec;
    private Resources resources;
    private final Calendar mCalendar = Calendar.getInstance();
    private final Paint mBackgroundPaint = new Paint();
    private int mBackgroundPaintColor;
    private Rect mBounds;
    private Rect peekCardPosition = new Rect();
    private boolean ambientMode = false;
    private boolean mLowBitAmbient;
    private boolean mMute;
    private volatile CircaTextStringer cts;
    private CTCs.Config currentConfig;
    private CTCs.Config selectedConfig = CTCs.Config.PLAIN;
    private boolean isRound = false;
    private Screen showScreen;
    private Rect currentPeekCardPosition;
    private SimpleDateFormat mDayFormat;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mShortDateFormat;
    private BatteryHelper.BatteryInfo mBatteryInfo;
    // DEBUG OPTIONS
    private int debugUseFixedDate = -1;             // default -1
    private int debugPeekCardPercentage = -1;       // default -1
    private boolean debugPeekCardPercentageUp = true;
    private final Drawable.RoundEmulation debugUseRoundEmulation = Drawable.RoundEmulation.NONE;
    private final boolean debugOverdraws = false;   // default false

    public CircaTextWatchFace(CircaTextService.Engine p) {
        parent = p;

        localeChanged();
        this.cts = new CircaTextStringerV1();

        setTexts();
    }

    @Override
    public void setMetrics(Resources r, WindowInsets insets) {
        this.resources = r;
        mBounds = new Rect(0, 0, r.getDisplayMetrics().widthPixels, r.getDisplayMetrics().heightPixels);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBackgroundPaintColor = r.getColor(R.color.transparent, r.newTheme());
        } else {
            mBackgroundPaintColor = r.getColor(R.color.transparent);
        }
        setBackgroundPaint();

        setDebugPeekCardRect(null);
        setupRoundMode(insets);

        if (this.isRound) {
            setupRoundScreen(r);
        } else {
            setupRectScreen(r);
        }
    }

    private void setupRoundMode(WindowInsets insets) {
        this.isRound = (this.debugUseRoundEmulation != Drawable.RoundEmulation.NONE) || insets.isRound();
        if (isRound) {
            if (this.debugUseRoundEmulation != Drawable.RoundEmulation.NONE) {
                if (this.debugUseRoundEmulation == Drawable.RoundEmulation.CHIN)
                    this.mBounds.bottom = 290;
            } else {
                this.mBounds.bottom -= insets.getStableInsetBottom();
            }
        }
    }

    private void setupRectScreen(Resources r) {
        int offset = 12;
        int height = (100 - (2 * offset)) / 3;
        int io = 2;

        createAnimatable(eTF.SHORTCAL, new Rect(5, 5, 95, 20), Drawable.Align.RIGHT, r, R.drawable.calendar)
                .setConfig(CTCs.Config.PEEK, new Rect(95, -20, 95, -20), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN)
                .setConfig(CTCs.Config.PEEKSMALL, CTCs.Config.PEEK);
        createAnimatable(eTF.WEATHER_TEMP, new Rect(5, 5, 95, 20), Drawable.Align.LEFT, r, R.drawable.thermometer)
                .setConfig(CTCs.Config.PEEK, new Rect(5, -20, 5, -20))
                .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN)
                .setConfig(CTCs.Config.PEEKSMALL, CTCs.Config.PEEK);
        createAnimatable(eTF.FIRST_LINE, new Rect(5, 20, 95, 42), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.TIME, new Rect(5, 80, 95, 87), Drawable.Align.LEFT)
                .setConfig(CTCs.Config.PEEKSMALL, new Rect(5, offset - io, 98, offset + height + io), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.PEEK, new Rect(5, 60, 95, 75), Drawable.Align.RIGHT);
        createAnimatable(eTF.SECOND_LINE, new Rect(5, 39, 95, 61), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.TIME, new Rect(5, 84, 95, 91), Drawable.Align.LEFT)
                .setConfig(CTCs.Config.PEEKSMALL, new Rect(5, offset + height - io, 98, offset + (height * 2) + io), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.PEEK, new Rect(5, 70, 95, 85), Drawable.Align.RIGHT);
        createAnimatable(eTF.THIRD_LINE, new Rect(5, 58, 95, 80), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.TIME, new Rect(5, 88, 95, 95), Drawable.Align.LEFT)
                .setConfig(CTCs.Config.PEEKSMALL, new Rect(5, offset + (height * 2) - io, 98, 100 - offset + io), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.PEEK, new Rect(5, 80, 95, 95), Drawable.Align.RIGHT);
        createAnimatable(eTF.HOUR, new Rect(5, 80, 95, 95))
                .setConfig(CTCs.Config.PEEKSMALL, new Rect(2, 10, 98, 55))
                .setConfig(CTCs.Config.TIME, new Rect(5, 25, 95, 75), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.PEEK, new Rect(5, 10, 95, 65), Drawable.Align.CENTER);
        createAnimatable(eTF.SHORTDATE, new Rect(5, 80, 95, 95), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.PEEKSMALL, new Rect(2, 57, 98, 90))
                .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN)
                .setConfig(CTCs.Config.PEEK, new Rect(5, 75, 95, 95), Drawable.Align.LEFT);
    }

    private void setupRoundScreen(Resources r) {
        createAnimatable(eTF.SHORTCAL, new Rect(5, 8, 80, 20), Drawable.Align.RIGHT, r, R.drawable.calendar)
                .setConfig(CTCs.Config.PEEK, new Rect(50, -20, 50, -20), Drawable.Align.RIGHT)
                .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
        createAnimatable(eTF.WEATHER_TEMP, new Rect(20, 8, 95, 20), Drawable.Align.LEFT, r, R.drawable.thermometer)
                .setConfig(CTCs.Config.PEEK, new Rect(50, -20, 50, -20), Drawable.Align.LEFT)
                .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
        createAnimatable(eTF.FIRST_LINE, new Rect(0, 17, 100, 43), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.TIME, new Rect(105, 17, 195, 43), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.PEEK, CTCs.Config.TIME);
        createAnimatable(eTF.SECOND_LINE, new Rect(0, 37, 100, 63), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.TIME, new Rect(105, 37, 195, 63), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.PEEK, CTCs.Config.TIME);
        createAnimatable(eTF.THIRD_LINE, new Rect(0, 57, 100, 83), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.TIME, new Rect(105, 57, 195, 83), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.PEEK, CTCs.Config.TIME);
        createAnimatable(eTF.HOUR, new Rect(-95, 22, -5, 75))
                .setConfig(CTCs.Config.TIME, new Rect(0, 22, 100, 75), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.PEEK, new Rect(2, 10, 98, 75), Drawable.Align.CENTER);
        createAnimatable(eTF.SHORTDATE, new Rect(5, 80, 95, 95), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.PEEK, new Rect(2, 70, 98, 98), Drawable.Align.CENTER)
                .setConfig(CTCs.Config.TIME, CTCs.Config.PLAIN);
        for (Animatable d : topDrawable.values()) {
            d.setConfig(CTCs.Config.PEEKSMALL, CTCs.Config.PEEK);
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

        AnimatableImpl adi = new AnimatableImpl(parent, d);
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
        if (this.ambientMode)
            showScreen = null;

        for (Drawable a : topDrawable.values()) {
            a.setAmbientMode(this.ambientMode);
        }

        CTCs.Config newConfig;
        Rect r = new Rect(this.mBounds);
        if (this.peekCardPosition.isEmpty()) {
            newConfig = selectedConfig;
        } else {
            if (this.peekCardPosition.top > (this.mBounds.height() * 50 / 100)) {
                newConfig = CTCs.Config.PEEK;
            } else {
                newConfig = CTCs.Config.PEEKSMALL;
            }
            r.bottom = this.peekCardPosition.top;
        }
        if (newConfig != currentConfig || r.bottom != currentPeekCardPosition.bottom) {
            currentConfig = newConfig;
            currentPeekCardPosition = r;
            for (Animatable a : topDrawable.values()) {
                a.animateToConfig(currentConfig, r);
            }
        }
    }

    @Override
    public void getTouchedText(int x, int y) {
        if (getTouchedTextHandleDebug(x, y)) return;

        if (showScreen != null) {
            int idx = showScreen.getTouchedText(x, y);
            if (idx == Drawable.Touched.CLOSEME) {
                showScreen = null;
            }
        } else {
            if (currentConfig == CTCs.Config.PEEK || currentConfig == CTCs.Config.PEEKSMALL)
                return;

            int idx = DrawableHelpers.getTouchedText(x, y, new ArrayList<Drawable>(topDrawable.values()));
            if (eTF.FIRST_LINE <= idx && idx <= eTF.THIRD_LINE || idx == eTF.HOUR) {
                if (selectedConfig == CTCs.Config.PLAIN)
                    selectedConfig = CTCs.Config.TIME;
                else if (selectedConfig == CTCs.Config.TIME)
                    selectedConfig = CTCs.Config.PLAIN;
                setSelectedConfig(selectedConfig);

                parent.storeConfig(CTCs.KEY_WATCHFACE_CONFIG, selectedConfig.toString());
            } else if (idx == eTF.SHORTCAL) {
                showScreen = new Schedule(this.resources, this.isRound, this.mMeetings, this.mBackgroundPaint.getColor());
            } else if (idx == eTF.WEATHER_TEMP) {
                showScreen = new WeatherScreen(this.resources, this.isRound, mWeather, mWeatherReq, mWeatherRec, this.mBackgroundPaint.getColor());
            } else {
                return;
            }
        }

        parent.invalidate();
    }

    private boolean getTouchedTextHandleDebug(int x, int y) {
        if (debugPeekCardPercentage > 0 && this.peekCardPosition.contains(x, y)) {
            if (debugPeekCardPercentageUp) {
                debugPeekCardPercentage += 5;
                if (debugPeekCardPercentage > 70) {
                    debugPeekCardPercentage -= 10;
                    debugPeekCardPercentageUp = false;
                }
            } else {
                debugPeekCardPercentage -= 5;
                if (debugPeekCardPercentage < 10) {
                    debugPeekCardPercentage += 10;
                    debugPeekCardPercentageUp = true;
                }
            }
            this.setPeekCardPosition(null);
            return true;
        }
        if (debugUseFixedDate >= 0) {
            debugUseFixedDate++;
            if (debugUseFixedDate >= 60)
                debugUseFixedDate = 0;
            parent.invalidate();
        }
        return false;
    }

    @Override
    public void setSelectedConfig(CTCs.Config cfg) {
        this.selectedConfig = cfg;
        if (currentConfig != CTCs.Config.PEEK && currentConfig != CTCs.Config.PEEKSMALL) {
            currentConfig = selectedConfig;
            for (int i = eTF.FIRST_LINE; i <= eTF.THIRD_LINE; i++) {
                topDrawable.get(i).animateToConfig(currentConfig, this.mBounds);
            }
            topDrawable.get(eTF.HOUR).animateToConfig(currentConfig, this.mBounds);
        }
    }

    @Override
    public void setStringer(CTCs.Stringer cfg) {
        this.cts = null;
        switch (cfg) {
            case CIRCA:
                this.cts = new CircaTextStringerV1();
                break;
            case RELAXED:
                this.cts = new CircaTextStringerV2();
                break;
            case PRECISE:
                this.cts = new CircaTextStringerV1(true);
                break;
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        onDrawRoundEmulation(canvas, bounds);

        canvas.drawRect(bounds, mBackgroundPaint);
        onDrawPeekCardEmulation(canvas);

        setTexts();

        if (showScreen != null) {
            showScreen.onDraw(canvas, bounds);
        } else {
            for (AnimatableImpl a : topDrawable.values()) {
                a.onDraw(canvas, this.mBounds);
            }
        }

        onDrawOverdraws(canvas);
    }

    private void onDrawOverdraws(Canvas canvas) {
        if (debugOverdraws) {
            Paint p = new Paint();
            p.setColor(Color.RED);
            p.setStyle(Paint.Style.STROKE);
            ArrayList<Rect> rl = new ArrayList<>();
            if (showScreen != null) {
                rl.addAll(showScreen.getDrawnRects());
            } else {
                for (AnimatableImpl a : topDrawable.values()) {
                    rl.addAll(a.getDrawnRects());
                }
            }
            while (rl.size() > 0) {
                Rect drawn = rl.remove(0);
                for (Rect r : rl) {
                    Rect where = new Rect(r);
                    if (where.intersect(drawn)) {
                        canvas.drawRect(where, p);
                    }
                }
            }
        }
    }

    private void onDrawPeekCardEmulation(Canvas canvas) {
        if (this.debugPeekCardPercentage > 0) {
            Paint c = new Paint();
            c.setColor(Color.WHITE);
            c.setAntiAlias(true);
            c.setTextAlign(Paint.Align.CENTER);
            canvas.drawRect(this.peekCardPosition, c);
            c.setColor(Color.BLACK);
            String value = String.valueOf(this.debugPeekCardPercentage) + "%, " + currentConfig.toString();
            canvas.drawText(value, this.peekCardPosition.centerX(), this.peekCardPosition.centerY(), c);
        }
    }

    private void onDrawRoundEmulation(Canvas canvas, Rect bounds) {
        if (this.debugUseRoundEmulation != Drawable.RoundEmulation.NONE) {
            Paint c = new Paint();
            c.setColor(Color.BLACK);
            c.setAntiAlias(true);
            canvas.drawRect(bounds, c);

            Path clippingpath = new Path();
            clippingpath.addCircle(160, 160, 160, Path.Direction.CW);
            canvas.clipPath(clippingpath);
            if (this.debugUseRoundEmulation == Drawable.RoundEmulation.CHIN) {
                canvas.clipRect(this.mBounds, Region.Op.INTERSECT);
            }
        }
    }

    @Override
    public void localeChanged() {
        mCalendar.setTimeZone(TimeZone.getDefault());
        Locale de = Locale.GERMANY;
        mDayFormat = new SimpleDateFormat("EEE", de);
        mDayFormat.setCalendar(mCalendar);
        mDateFormat = new SimpleDateFormat("d. MMM yyyy", de);
        mDateFormat.setCalendar(mCalendar);
        mShortDateFormat = new SimpleDateFormat("d. MMM", de);
        mShortDateFormat.setCalendar(mCalendar);
    }

    private void setDebugPeekCardRect(Rect rect) {
        if (debugPeekCardPercentage > 0) {
            int top = ((100 - debugPeekCardPercentage) * mBounds.height()) / 100;
            if (rect != null && !rect.isEmpty())
                top = Math.min(top, rect.top);

            this.peekCardPosition = new Rect(mBounds);
            this.peekCardPosition.top = top;
        }
    }

    @Override
    public void setLowBitAmbientMode(boolean aBoolean) {
        this.mLowBitAmbient = aBoolean;
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        this.ambientMode = inAmbientMode;

        setBackgroundPaint();
        updateVisibilty();
    }

    private void setBackgroundPaint() {
        mBackgroundPaint.setColor(this.ambientMode ? Color.BLACK : mBackgroundPaintColor);
    }

    @Override
    public void setPeekCardPosition(Rect rect) {
        this.peekCardPosition = rect;
        setDebugPeekCardRect(rect);
        updateVisibilty();
        parent.invalidate();
    }

    @Override
    public void setMuteMode(boolean inMuteMode) {
        if (mMute != inMuteMode) {
            mMute = inMuteMode;
            updateVisibilty();
            parent.invalidate();
        }
    }

    private void setTexts() {
        long now = System.currentTimeMillis();
        mCalendar.setTimeInMillis(now);
        if (debugUseFixedDate >= 0) {
            mCalendar.set(2015, 10, 30, 17, debugUseFixedDate, 30);
        }

        {
            String sb = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                        formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
            mTexts.put(eTF.HOUR, sb);
        }
        {
            String sb = ":" + formatTwoDigitNumber(mCalendar.get(Calendar.SECOND));
            mTexts.put(eTF.SECOND, sb);
        }
        Date d = mCalendar.getTime();
        mTexts.put(eTF.DAY_OF_WEEK, mDayFormat.format(d));
        mTexts.put(eTF.DATE, mDateFormat.format(d));
        mTexts.put(eTF.SHORTDATE, mShortDateFormat.format(d));
        if (mBatteryInfo != null) {
            mTexts.put(eTF.BATTERY, String.format("%3.0f%%", mBatteryInfo.getPercent() * 100));
        } else {
            mTexts.put(eTF.BATTERY, "");
        }
        if (mMeetings != null) {
            ArrayList<CalendarHelper.EventInfo> m = new ArrayList<>();
            for (CalendarHelper.EventInfo ei : mMeetings) {
                if (ei.DtStart.getTime() >= now && !ei.Hidden && !ei.Disabled)
                    m.add(ei);
            }

            if (m.size() == 0) {
                mTexts.put(eTF.SHORTCAL, "-");
                mTexts.put(eTF.CALENDAR_1, "no meetings");
                mTexts.put(eTF.CALENDAR_2, "");
            } else {
                CalendarHelper.EventInfo first = m.get(0);
                m.remove(0);
                mTexts.put(eTF.SHORTCAL, first.formatStart());
                mTexts.put(eTF.CALENDAR_1, first.formatEnd() + " " + first.Title);

                int additionalEvents = m.size();
                if (additionalEvents == 1)
                    mTexts.put(eTF.CALENDAR_2, "+" + additionalEvents + " additional event");
                if (additionalEvents > 1)
                    mTexts.put(eTF.CALENDAR_2, "+" + additionalEvents + " additional events");
            }
        } else {
            mTexts.put(eTF.SHORTCAL, "-");
            mTexts.put(eTF.CALENDAR_1, "");
            mTexts.put(eTF.CALENDAR_2, "");
        }
        if (mWeather != null) {
            mTexts.put(eTF.WEATHER_TEMP, String.format("%2.0f°C", mWeather.temperature.getTemp()));
            mTexts.put(eTF.WEATHER_AGE, CTU.getAge(now, mWeather.lastupdate));
            mTexts.put(eTF.WEATHER_DESC, mWeather.currentCondition.getCondition());
        } else {
            mTexts.put(eTF.WEATHER_TEMP, "");
            mTexts.put(eTF.WEATHER_AGE, "");
            mTexts.put(eTF.WEATHER_DESC, "");
        }

        String[] circaTexts = this.cts.getString(this.mCalendar);
        for (int i = eTF.FIRST_LINE; i < eTF.SIZE; i++) {
            mTexts.put(i, "");
        }
        if (circaTexts.length == 1) {
            mTexts.put(eTF.SECOND_LINE, circaTexts[0]);
        } else {
            for (int i = 0; i < circaTexts.length; i++) {
                mTexts.put(eTF.FIRST_LINE + i, circaTexts[i]);
            }
        }
    }

    private String formatTwoDigitNumber(int hour) {
        return String.format("%02d", hour);
    }

    @Override
    public void setBatteryInfo(BatteryHelper.BatteryInfo batteryInfo) {
        mBatteryInfo = batteryInfo;
    }

    @Override
    public void setEventInfo(CalendarHelper.EventInfo[] meetings) {
        mMeetings = meetings;
    }

    @Override
    public void setWeatherInfo(Weather weather, Date req, Date rec) {
        mWeatherReq = req;
        mWeatherRec = rec;
        mWeather = weather;
    }

    class eTF {
        public static final int DAY_OF_WEEK = 0;
        public static final int DATE = DAY_OF_WEEK + 1;
        public static final int SHORTDATE = DATE + 1;
        public static final int SHORTCAL = SHORTDATE + 1;
        public static final int CALENDAR_1 = SHORTCAL + 1;
        public static final int CALENDAR_2 = CALENDAR_1 + 1;
        public static final int BATTERY = CALENDAR_2 + 1;
        public static final int HOUR = BATTERY + 1;
        public static final int SECOND = HOUR + 1;
        public static final int WEATHER_TEMP = SECOND + 1;
        public static final int WEATHER_AGE = WEATHER_TEMP + 1;
        public static final int WEATHER_DESC = WEATHER_AGE + 1;
        public static final int FIRST_LINE = WEATHER_DESC + 1;
        public static final int SECOND_LINE = FIRST_LINE + 1;
        public static final int THIRD_LINE = SECOND_LINE + 1;
        public static final int SIZE = THIRD_LINE + 1;
    }
}
