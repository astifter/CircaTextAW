package com.astifter.circatext.watchfaces;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.graphicshelpers.Drawable;
import com.astifter.circatextutils.Weather;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public abstract class BaseWatchFace implements WatchFace {
    final HashMap<Integer, String> mTexts = new HashMap<>();
    final CanvasWatchFaceService.Engine parent;
    Paint mBackgroundPaint;
    int mBackgroundPaintColor;
    Rect mBounds;
    Rect peekCardPosition = new Rect();
    boolean ambientMode;
    boolean mLowBitAmbient;
    boolean mMute;
    private Calendar mCalendar;
    private Date mDate;
    private SimpleDateFormat mDayFormat;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mShortDateFormat;
    private BatteryHelper.BatteryInfo mBatteryInfo;
    private CalendarHelper.EventInfo[] mMeetings;
    private Weather mWeather = null;

    BaseWatchFace(CanvasWatchFaceService.Engine parent) {
        this.parent = parent;

        mBackgroundPaint = new Paint();

        mCalendar = Calendar.getInstance();
        mDate = new Date();
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
    public void setMetrics(Resources resources, WindowInsets insets) {
        int width = resources.getDisplayMetrics().widthPixels;
        int height = resources.getDisplayMetrics().heightPixels;
        mBounds = new Rect(0, 0, width, height);

        mBackgroundPaintColor = resources.getColor(R.color.transparent);
        mBackgroundPaint.setColor(mBackgroundPaintColor);
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
        mDate.setTime(now);

        {
            String sb = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                        formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
            mTexts.put(eTF.HOUR, sb);
        }
        {
            String sb = ":" + formatTwoDigitNumber(mCalendar.get(Calendar.SECOND));
            mTexts.put(eTF.SECOND, sb);
        }
        mTexts.put(eTF.DAY_OF_WEEK, mDayFormat.format(mDate));
        mTexts.put(eTF.DATE, mDateFormat.format(mDate));
        mTexts.put(eTF.SHORTDATE, mShortDateFormat.format(mDate));
        if (mBatteryInfo != null) {
            mTexts.put(eTF.BATTERY, String.format("%3.0f%%", mBatteryInfo.getPercent() * 100));
        } else {
            mTexts.put(eTF.BATTERY, "");
        }
        if (mMeetings != null) {
            int i = 0;
            while (i < mMeetings.length && mMeetings[i].DtStart.getTime() < now) i++;

            if (i >= mMeetings.length) {
                mTexts.put(eTF.SHORTCAL, "-");
                mTexts.put(eTF.CALENDAR_1, "no meetings");
                mTexts.put(eTF.CALENDAR_2, "");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                mTexts.put(eTF.SHORTCAL, sdf.format(mMeetings[i].DtStart));
                mTexts.put(eTF.CALENDAR_1, sdf.format(mMeetings[i].DtStart) + " " + mMeetings[i].Title);

                int additionalEvents = mMeetings.length - 1 - i;
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
