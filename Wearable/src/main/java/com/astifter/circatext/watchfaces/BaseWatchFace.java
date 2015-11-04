package com.astifter.circatext.watchfaces;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.WindowInsets;

import com.astifter.circatext.CircaTextService;
import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatextutils.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public abstract class BaseWatchFace implements WatchFace {
    final HashMap<Integer, String> mTexts = new HashMap<>();
    final CircaTextService.Engine parent;
    protected CalendarHelper.EventInfo[] mMeetings;
    protected Weather mWeather = null;
    protected Resources resources;
    Paint mBackgroundPaint;
    int mBackgroundPaintColor;
    Rect mBounds;
    Rect peekCardPosition = new Rect();
    boolean ambientMode;
    boolean mLowBitAmbient;
    boolean mMute;
    protected Calendar mCalendar;
    private SimpleDateFormat mDayFormat;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mShortDateFormat;
    private BatteryHelper.BatteryInfo mBatteryInfo;
    // DEBUG OPTIONS
    private boolean fixedDateTime = true;
    protected int peekCardDebug = -1;
    protected Drawable.RoundEmulation roundemulation = Drawable.RoundEmulation.NONE;

    BaseWatchFace(CircaTextService.Engine parent) {
        this.parent = parent;

        mBackgroundPaint = new Paint();

        mCalendar = Calendar.getInstance();
        localeChanged();

        setTexts();
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

    @Override
    public void setMetrics(Resources r, WindowInsets insets) {
        this.resources = r;
        int width = r.getDisplayMetrics().widthPixels;
        int height = r.getDisplayMetrics().heightPixels;
        mBounds = new Rect(0, 0, width, height);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBackgroundPaintColor = r.getColor(R.color.transparent, r.newTheme());
        } else {
            //noinspection deprecation
            mBackgroundPaintColor = r.getColor(R.color.transparent);
        }
        mBackgroundPaint.setColor(mBackgroundPaintColor);

        setDebugPeekCardRect(null);
    }

    private void setDebugPeekCardRect(Rect rect) {
        if (peekCardDebug > 0) {
            int top = ((100-peekCardDebug) * mBounds.height()) / 100;
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

        mBackgroundPaint.setColor(this.ambientMode ?
                Color.BLACK :
                mBackgroundPaintColor);
        updateVisibilty();
    }

    private void startAlphaAnimation(Drawable t, int start, int stop,
                                     Animator.AnimatorListener a) {
        ValueAnimator anim = ObjectAnimator.ofInt(t, "alpha", start, stop);
        anim.setDuration(50);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        if (a != null)
            anim.addListener(a);
        anim.start();
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

    void setTexts() {
        long now = System.currentTimeMillis();
        mCalendar.setTimeInMillis(now);
        if (fixedDateTime) {
            mCalendar.set(2015, 10, 30, 17, 6, 30);
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
            long age = now - mWeather.lastupdate.getTime();
            float ageFloat = age / (60 * 1000);
            String tempText = String.format("%2.0f°C", mWeather.temperature.getTemp());
            String ageText = String.format(" (%.0fm)", ageFloat);
            mTexts.put(eTF.WEATHER_TEMP, tempText);
            mTexts.put(eTF.WEATHER_AGE, ageText);
            mTexts.put(eTF.WEATHER_DESC, mWeather.currentCondition.getCondition());
        } else {
            mTexts.put(eTF.WEATHER_TEMP, "");
            mTexts.put(eTF.WEATHER_AGE, "");
            mTexts.put(eTF.WEATHER_DESC, "");
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
    public void setWeatherInfo(Weather weather) {
        mWeather = weather;
    }

    boolean haveWeather() {
        return mWeather != null;
    }

    void startTapHighlight(Drawable ct) {
        Animator.AnimatorListener listener = new ReverseListener(ct);
        startAlphaAnimation(ct, 255, 192, listener);
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
        public static final int SIZE = WEATHER_DESC + 1;
    }

    private class ReverseListener implements Animator.AnimatorListener {
        private final Drawable drawable;

        public ReverseListener(Drawable d) {
            this.drawable = d;
        }

        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            startAlphaAnimation(drawable, 0, 255, null);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
