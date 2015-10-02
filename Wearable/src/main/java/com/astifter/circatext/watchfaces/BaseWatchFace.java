package com.astifter.circatext.watchfaces;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.graphicshelpers.CircaTextDrawable;
import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.Weather;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public abstract class BaseWatchFace implements WatchFace {
    protected final HashMap<Integer, String> mTexts = new HashMap<>();
    protected final CanvasWatchFaceService.Engine parent;
    protected Paint mBackgroundPaint;
    protected Rect mBounds;
    protected Rect peekCardPosition = new Rect();
    protected boolean ambientMode;
    protected boolean mLowBitAmbient;
    protected boolean mMute;
    private int mInteractiveBackgroundColor = CircaTextConsts.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;
    private Calendar mCalendar;
    private Date mDate;
    private SimpleDateFormat mDayFormat;
    private SimpleDateFormat mDateFormat;
    private BatteryHelper.BatteryInfo mBatteryInfo;
    private CalendarHelper.EventInfo[] mMeetings;
    private Weather mWeather = null;

    public BaseWatchFace(CanvasWatchFaceService.Engine parent) {
        this.parent = parent;

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mInteractiveBackgroundColor);

        mCalendar = Calendar.getInstance();
        mDate = new Date();
        localeChanged();
    }

    @Override
    public void localeChanged() {
        mCalendar.setTimeZone(TimeZone.getDefault());
        mDayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        mDayFormat.setCalendar(mCalendar);
        mDateFormat = new SimpleDateFormat("MMM d yyyy", Locale.getDefault());
        mDateFormat.setCalendar(mCalendar);
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        int width = resources.getDisplayMetrics().widthPixels;
        int height = resources.getDisplayMetrics().heightPixels;
        mBounds = new Rect(0, 0, width, height);
    }

    @Override
    public void setLowBitAmbientMode(boolean aBoolean) {
        this.mLowBitAmbient = aBoolean;
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        this.ambientMode = inAmbientMode;

        mBackgroundPaint.setColor(this.ambientMode ?
                mInteractiveBackgroundColor :
                CircaTextConsts.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
        updateVisibilty();
    }

    protected void startAnimation(CircaTextDrawable t, String attribute, int start, int stop,
                                  int duration, Animator.AnimatorListener a) {
        ValueAnimator anim = ObjectAnimator.ofInt(t, attribute, start, stop);
        anim.setDuration(duration);
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

    abstract protected void updateVisibilty();

    @Override
    public void setBackgroundColor(int color) {
        mInteractiveBackgroundColor = color;
        if (this.ambientMode && mBackgroundPaint != null) {
            mBackgroundPaint.setColor(color);
        }
        parent.invalidate();
    }

    @Override
    public void setPeekCardPosition(Rect rect) {
        this.peekCardPosition = rect;
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

    protected void setTexts() {
        long now = System.currentTimeMillis();
        mCalendar.setTimeInMillis(now);
        mDate.setTime(now);

        {
            StringBuilder sb = new StringBuilder();
            sb.append(formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY)));
            sb.append(":");
            sb.append(formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE)));
            mTexts.put(eTF.HOUR, sb.toString());
        }
        {
            StringBuilder sb = new StringBuilder();
            sb.append(":");
            sb.append(formatTwoDigitNumber(mCalendar.get(Calendar.SECOND)));
            mTexts.put(eTF.SECOND, sb.toString());
        }
        mTexts.put(eTF.DAY_OF_WEEK, mDayFormat.format(mDate));
        mTexts.put(eTF.DATE, mDateFormat.format(mDate));
        if (mBatteryInfo != null) {
            mTexts.put(eTF.BATTERY, String.format("%3.0f%%", mBatteryInfo.getPercent() * 100));
        } else {
            mTexts.put(eTF.BATTERY, "");
        }
        int i = 0;
        while (i < mMeetings.length && mMeetings[i].DtStart.getTime() < now) i++;

        if (i >= mMeetings.length) {
            mTexts.put(eTF.CALENDAR_1, "no meetings");
            mTexts.put(eTF.CALENDAR_2, "");
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            mTexts.put(eTF.CALENDAR_1, sdf.format(mMeetings[i].DtStart) + " " + mMeetings[i].Title);

            int additionalEvents = mMeetings.length - 1 - i;
            if (additionalEvents == 1)
                mTexts.put(eTF.CALENDAR_2, "+" + additionalEvents + " additional event");
            if (additionalEvents > 1)
                mTexts.put(eTF.CALENDAR_2, "+" + additionalEvents + " additional events");
        }
        if (mWeather != null) {
            long age = now - mWeather.lastupdate.getTime();
            float ageFloat = age / (60 * 1000);
            String tempText = String.format("%2.0f°C", mWeather.temperature.getTemp());
            String ageText = String.format(" (%.0fm)", ageFloat);
            mTexts.put(eTF.WEATHER_TEMP, tempText);
            mTexts.put(eTF.WEATHER_AGE, ageText);
            mTexts.put(eTF.WEATHER_DESC, mWeather.currentCondition.getCondition());
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

    protected boolean haveWeather() {
        return mWeather != null;
    }

    protected void startTapHighlight(CircaTextDrawable ct) {
        Animator.AnimatorListener listener = new ReverseListener(ct);
        startAnimation(ct, "alpha", 255, 0, 100, listener);
    }

    protected class eTF {
        public static final int DAY_OF_WEEK = 0;
        public static final int DATE = DAY_OF_WEEK + 1;
        public static final int CALENDAR_1 = DATE + 1;
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
        private final CircaTextDrawable drawable;

        public ReverseListener(CircaTextDrawable d) {
            this.drawable = d;
        }

        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            startAnimation(drawable, "alpha", 0, 255, 100, null);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
