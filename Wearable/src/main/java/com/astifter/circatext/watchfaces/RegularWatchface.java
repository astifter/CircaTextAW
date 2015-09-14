package com.astifter.circatext.watchfaces;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.graphicshelpers.CircaTextDrawable;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatext.graphicshelpers.HorizontalStack;
import com.astifter.circatext.graphicshelpers.VerticalStack;
import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class RegularWatchFace implements WatchFace {
    CanvasWatchFaceService.Engine parent;

    class eTF {
        private static final int DAY_OF_WEEK = 0;
        private static final int DATE = DAY_OF_WEEK + 1;
        private static final int CALENDAR_1 = DATE + 1;
        private static final int CALENDAR_2 = CALENDAR_1 + 1;
        private static final int BATTERY = CALENDAR_2 + 1;
        private static final int HOUR = BATTERY + 1;
        private static final int COLON_1 = HOUR + 1;
        private static final int MINUTE = COLON_1 + 1;
        private static final int COLON_2 = MINUTE + 1;
        private static final int SECOND = COLON_2 + 1;
        private static final int WEATHER_TEMP = SECOND + 1;
        private static final int WEATHER_AGE = WEATHER_TEMP + 1;
        private static final int WEATHER_DESC = WEATHER_AGE + 1;
        private static final int SIZE = WEATHER_DESC + 1;
    }
    private final HashMap<Integer, DrawableText> mTF = new HashMap<>();
    private final HashMap<Integer, String> mTexts = new HashMap<>();
    private final ArrayList<DrawableText> mTFFading = new ArrayList<>();
    private final ArrayList<DrawableText> mTFAnimated = new ArrayList<>();

    int mInteractiveBackgroundColor = CircaTextConsts.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;
    Paint mBackgroundPaint;
    private final VerticalStack topDrawable = new VerticalStack();
    private Rect mBounds;
    float textScaleFactor = 1.55f;
    private Rect peekCardPosition = new Rect();

    private Calendar mCalendar;
    Date mDate;
    SimpleDateFormat mDayFormat;
    SimpleDateFormat mDateFormat;

    private boolean ambientMode;
    private boolean mLowBitAmbient;
    boolean mMute;

    private BatteryHelper.BatteryInfo mBatteryInfo;
    CalendarHelper.EventInfo[] mMeetings;
    private Weather mWeather = null;

    public RegularWatchFace(CanvasWatchFaceService.Engine parent) {
        this.parent = parent;
        mCalendar = Calendar.getInstance();

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mInteractiveBackgroundColor);

        for (int i = 0; i < eTF.SIZE; i++) {
            mTF.put(i, new DrawableText());
            mTF.get(i).setTextSource(i, mTexts);
        }

        mTF.get(eTF.BATTERY).setAlignment(DrawableText.Align.RIGHT);
        mTF.get(eTF.DATE).setAlignment(DrawableText.Align.RIGHT);
        mTF.get(eTF.WEATHER_DESC).setAlignment(DrawableText.Align.RIGHT);
        mTexts.put(eTF.COLON_1, ":");
        mTexts.put(eTF.COLON_2, ":");

        topDrawable.addBelow(mTF.get(eTF.BATTERY));
        HorizontalStack hours = new HorizontalStack();
        hours.addRight(mTF.get(eTF.HOUR));
        hours.addRight(mTF.get(eTF.COLON_1));
        hours.addRight(mTF.get(eTF.MINUTE));
        hours.addRight(mTF.get(eTF.COLON_2));
        hours.addRight(mTF.get(eTF.SECOND));
        topDrawable.addBelow(hours);
        HorizontalStack date = new HorizontalStack();
        date.addRight(mTF.get(eTF.DAY_OF_WEEK));
        date.addRight(mTF.get(eTF.DATE));
        topDrawable.addBelow(date);
        topDrawable.addBelow(mTF.get(eTF.CALENDAR_1));
        topDrawable.addBelow(mTF.get(eTF.CALENDAR_2));
        HorizontalStack weather = new HorizontalStack();
        weather.addRight(mTF.get(eTF.WEATHER_TEMP));
        weather.addRight(mTF.get(eTF.WEATHER_AGE));
        weather.addRight(mTF.get(eTF.WEATHER_DESC));
        topDrawable.addBelow(weather);

        mTFFading.add(mTF.get(eTF.CALENDAR_1));
        mTFFading.add(mTF.get(eTF.CALENDAR_2));
        mTFFading.add(mTF.get(eTF.COLON_2));
        mTFFading.add(mTF.get(eTF.SECOND));
        mTFFading.add(mTF.get(eTF.BATTERY));
        mTFFading.add(mTF.get(eTF.WEATHER_TEMP));
        mTFFading.add(mTF.get(eTF.WEATHER_AGE));
        mTFFading.add(mTF.get(eTF.WEATHER_DESC));

        mTFAnimated.add(mTF.get(eTF.HOUR));
        mTFAnimated.add(mTF.get(eTF.COLON_1));
        mTFAnimated.add(mTF.get(eTF.MINUTE));

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

        int mOffset = (int)resources.getDimension(insets.isRound() ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
        //int mYOffset = (int)resources.getDimension(R.dimen.digital_y_offset);
        mBounds = new Rect(mOffset, mOffset, width-mOffset, height-mOffset);

        float textSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "00:00:00", mBounds);
        float biggerTextSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "00:00", mBounds);
        textScaleFactor = biggerTextSize / textSize;

        for (Integer i : mTF.keySet()) {
            DrawableText t = mTF.get(i);
            t.setTextSize(textSize);
            t.setDefaultTextSize(textSize);
        }
        if (this.ambientMode) {
            for (DrawableText t : mTFAnimated) {
                t.setTextSize(textSize*textScaleFactor);
            }
        }
        mTF.get(eTF.DAY_OF_WEEK).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
        mTF.get(eTF.DATE).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
        mTF.get(eTF.CALENDAR_1).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.CALENDAR_2).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.BATTERY).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.WEATHER_TEMP).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.WEATHER_AGE).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size)/1.5f);
        mTF.get(eTF.WEATHER_DESC).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
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

        if (mLowBitAmbient) {
            for (Integer i : mTF.keySet()) {
                mTF.get(i).setAmbientMode(inAmbientMode);
            }
        }
        if (!inAmbientMode) {
            for (DrawableText dt : mTFFading) {
                createIntAnimation(dt, "alpha", 0, 255);
            }
            for (DrawableText dt : mTFAnimated) {
                createTextSizeAnimation(dt, dt.getDefaultTextSize() * textScaleFactor, dt.getDefaultTextSize());
            }
        } else {
            for (DrawableText dt : mTFAnimated) {
                createTextSizeAnimation(dt, dt.getDefaultTextSize(), dt.getDefaultTextSize() * textScaleFactor);
            }
        }

    }

   private void createIntAnimation(DrawableText t, String attribute, int start, int stop) {
        ValueAnimator anim = ObjectAnimator.ofInt(t, attribute, start, stop);
        startAnimation(anim);
    }

    private void createTextSizeAnimation(DrawableText t, float from, float to) {
        createFloatAnimation(t, "textSize", from, to);
    }

    private void createFloatAnimation(DrawableText t, String attribute, float start, float stop) {
        ValueAnimator anim = ObjectAnimator.ofFloat(t, attribute, start, stop);
        startAnimation(anim);
    }

    private void startAnimation(ValueAnimator anim) {
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        anim.start();
    }

    ValueAnimator tapAnimator;

    @Override
    public void startTapHighlight() {
        Animator.AnimatorListener listener = new ReverseListener();
        tapAnimator = startAnimation(topDrawable, "alpha", 255, 0, 100, listener);
    }

    private class ReverseListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            startAnimation(topDrawable, "alpha", 0, 255, 100, null);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    private ValueAnimator startAnimation(CircaTextDrawable t, String attribute, int start, int stop,
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
        return anim;
    }

    public void updateVisibilty() {
        for(Integer i : mTF.keySet()) {
            mTF.get(i).hide();
        }

        mTF.get(eTF.HOUR).show();
        mTF.get(eTF.COLON_1).show();
        mTF.get(eTF.MINUTE).show();

        // draw the rest only when not in mute mode
        if (mMute) return;

        if (this.peekCardPosition.isEmpty()) {
            mTF.get(eTF.DAY_OF_WEEK).show();
            mTF.get(eTF.DATE).show();
        }

        // draw the rest only when not in ambient mode
        if(this.ambientMode) return;

        mTF.get(eTF.COLON_2).show();
        mTF.get(eTF.SECOND).show();
        mTF.get(eTF.BATTERY).show();

        // if peek card is shown, exit
        if (this.peekCardPosition.isEmpty()) {
            mTF.get(eTF.CALENDAR_1).show();
            mTF.get(eTF.CALENDAR_2).show();

            if (mWeather != null) {
                mTF.get(eTF.WEATHER_TEMP).show();
                mTF.get(eTF.WEATHER_AGE).show();
                mTF.get(eTF.WEATHER_DESC).show();
            }
        }
    }

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

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        long now = System.currentTimeMillis();
        mCalendar.setTimeInMillis(now);
        mDate.setTime(now);

        mTexts.put(eTF.HOUR, formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY)));
        mTexts.put(eTF.MINUTE, formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE)));
        mTexts.put(eTF.SECOND, formatTwoDigitNumber(mCalendar.get(Calendar.SECOND)));
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

        // Draw the background.
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
        topDrawable.onDraw(canvas, mBounds);
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
}
