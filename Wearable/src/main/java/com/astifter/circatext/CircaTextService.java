/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.astifter.circatext;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.CircaTextUtil;
import com.astifter.circatextutils.Serializer;
import com.astifter.circatextutils.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Sample digital watch face with blinking colons and seconds. In ambient mode, the seconds not
 * shown and the colons don't blink. On devices with low-bit ambient mode, the text is drawn without
 * anti-aliasing in ambient mode. On devices which require burn-in protection, the hours are drawn
 * in normal rather than bold. The time is drawn with less contrast and without seconds in mute
 * mode.
 */
public class CircaTextService extends CanvasWatchFaceService {
    private static final String TAG = "CircaTextService";

    /**
     * Update rate in milliseconds for normal (not ambient and not mute) mode. We update twice
     * a second to blink the colons.
     */
    private static final long NORMAL_UPDATE_RATE_MS = 1000;

    @Override
    public Engine onCreateEngine() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreateEngine()");
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine
                      implements DataApi.DataListener,
                                 GoogleApiClient.ConnectionCallbacks,
                                 GoogleApiClient.OnConnectionFailedListener {

        static final int MSG_UPDATE_TIME = 0;
        static final int MSG_LOAD_MEETINGS = 1;
        private static final String COLON_STRING = ":";
        private static final int eTF_DAY_OF_WEEK = 0;
        private static final int eTF_DATE = eTF_DAY_OF_WEEK + 1;
        private static final int eTF_CALENDAR_1 = eTF_DATE + 1;
        private static final int eTF_CALENDAR_2 = eTF_CALENDAR_1 + 1;
        private static final int eTF_BATTERY = eTF_CALENDAR_2 + 1;
        private static final int eTF_HOUR = eTF_BATTERY + 1;
        private static final int eTF_COLON_1 = eTF_HOUR + 1;
        private static final int eTF_MINUTE = eTF_COLON_1 + 1;
        private static final int eTF_COLON_2 = eTF_MINUTE + 1;
        private static final int eTF_SECOND = eTF_COLON_2 + 1;
        private static final int eTF_WEATHER_TEMP = eTF_SECOND + 1;
        private static final int eTF_WEATHER_AGE = eTF_WEATHER_TEMP + 1;
        private static final int eTF_WEATHER_DESC = eTF_WEATHER_AGE + 1;
        private static final int eTF_SIZE = eTF_WEATHER_DESC + 1;
        final GoogleApiClient mGoogleApiClient = CircaTextUtil.buildGoogleApiClient(CircaTextService.this, this, this);
        private final CalendarHelper mCalendarHelper = new CalendarHelper(this, CircaTextService.this);
        private final BatteryHelper mBatteryHelper = new BatteryHelper(this);
        private Weather mWeather = null;
        private Date    mWeatherRequested = null;
        private final DrawableText[] mTextFields = new DrawableText[eTF_SIZE];
        private final ArrayList<DrawableText> mTextFieldsAnimated = new ArrayList<>();
        private final ArrayList<DrawableText> mHoursAnimated = new ArrayList<>();
        Calendar mCalendar;
        Date mDate;
        SimpleDateFormat mDayFormat;
        SimpleDateFormat mDateFormat;
        boolean mMute;
        boolean minAmbientMode;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        int mInteractiveBackgroundColor = CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;
        Paint mBackgroundPaint;
        float mXOffset;
        float mYOffset;
        final Handler mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mUpdateHandler.handleMessage()");
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = NORMAL_UPDATE_RATE_MS - (timeMs % NORMAL_UPDATE_RATE_MS);
                            mUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;

                    case MSG_LOAD_MEETINGS:
                        mCalendarHelper.restartLoadMeetingTask();
                        break;
                }
            }
        };
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mReceiver.onReceive()");

                if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) ||
                        Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())) {
                    mCalendar.setTimeZone(TimeZone.getDefault());
                    initFormats();
                }
                if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction()) &&
                        WearableCalendarContract.CONTENT_URI.equals(intent.getData())) {
                    mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
                }
                invalidate();
            }
        };

        /**
         * Unregistering an unregistered receiver throws an exception. Keep track of the
         * registration state to prevent that.
         */
        boolean mRegisteredReceiver = false;

        Engine() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Engine()");

            for (int i = 0; i < eTF_SIZE; i++) {
                mTextFields[i] = new DrawableText(this);
            }
        }

        @Override // WatchFaceService.Engine
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate()");
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(CircaTextService.this).build());

            Resources resources = CircaTextService.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);

            mTextFields[eTF_DAY_OF_WEEK] = new DrawableText(this, resources.getColor(R.color.digital_date));
            mTextFields[eTF_DATE] = new DrawableText(this, resources.getColor(R.color.digital_date), Paint.Align.RIGHT);
            mTextFields[eTF_CALENDAR_1] = new DrawableText(this, resources.getColor(R.color.digital_date));
            mTextFields[eTF_CALENDAR_2] = new DrawableText(this, resources.getColor(R.color.digital_date));
            mTextFields[eTF_BATTERY] = new DrawableText(this, resources.getColor(R.color.digital_colons), Paint.Align.RIGHT);
            mTextFields[eTF_WEATHER_TEMP] = new DrawableText(this, resources.getColor(R.color.digital_colons), Paint.Align.LEFT);
            mTextFields[eTF_WEATHER_AGE] = new DrawableText(this, resources.getColor(R.color.digital_colons), Paint.Align.LEFT);
            mTextFields[eTF_WEATHER_DESC] = new DrawableText(this, resources.getColor(R.color.digital_colons), Paint.Align.RIGHT);
            mTextFields[eTF_HOUR] = new DrawableText(this, CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS, DrawableText.BOLD_TYPEFACE);
            mTextFields[eTF_COLON_1] = new DrawableText(this, resources.getColor(R.color.digital_colons));
            mTextFields[eTF_MINUTE] = new DrawableText(this, CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            mTextFields[eTF_COLON_2] = new DrawableText(this, resources.getColor(R.color.digital_colons));
            mTextFields[eTF_SECOND] = new DrawableText(this, CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS);
            mTextFieldsAnimated.add(mTextFields[eTF_CALENDAR_1]);
            mTextFieldsAnimated.add(mTextFields[eTF_CALENDAR_2]);
            mTextFieldsAnimated.add(mTextFields[eTF_COLON_2]);
            mTextFieldsAnimated.add(mTextFields[eTF_SECOND]);
            mTextFieldsAnimated.add(mTextFields[eTF_BATTERY]);
            mTextFieldsAnimated.add(mTextFields[eTF_WEATHER_TEMP]);
            mTextFieldsAnimated.add(mTextFields[eTF_WEATHER_AGE]);
            mTextFieldsAnimated.add(mTextFields[eTF_WEATHER_DESC]);
            mHoursAnimated.add(mTextFields[eTF_HOUR]);
            mHoursAnimated.add(mTextFields[eTF_COLON_1]);
            mHoursAnimated.add(mTextFields[eTF_MINUTE]);
            mTextFields[eTF_COLON_1].setText(COLON_STRING);
            mTextFields[eTF_COLON_2].setText(COLON_STRING);

            mCalendar = Calendar.getInstance();
            mDate = new Date();
            initFormats();

            mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
        }

        @Override
        public void onPeekCardPositionUpdate (Rect rect) {
            updateVisibility();
            invalidate();
        }

        @Override // WatchFaceService.Engine
        public void onDestroy() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onDestroy()");

            mUpdateHandler.removeMessages(MSG_UPDATE_TIME);
            mUpdateHandler.removeMessages(MSG_LOAD_MEETINGS);
            mCalendarHelper.cancelLoadMeetingTask();
            super.onDestroy();
        }

        @Override // WatchFaceService.Engine
        public void onVisibilityChanged(boolean visible) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onVisibilityChanged()");
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();

                // Update time zone and date formats, in case they changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                initFormats();

                mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
            } else {
                mUpdateHandler.removeMessages(MSG_LOAD_MEETINGS);

                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void initFormats() {
            mDayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            mDayFormat.setCalendar(mCalendar);
            mDateFormat = new SimpleDateFormat("MMM d yyyy", Locale.getDefault());
            mDateFormat.setCalendar(mCalendar);
        }

        private void registerReceiver() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "registerReceiver()");

            if (mRegisteredReceiver) {
                return;
            }
            mRegisteredReceiver = true;
            {
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                filter.addAction(Intent.ACTION_LOCALE_CHANGED);
                CircaTextService.this.registerReceiver(mReceiver, filter);
            }
            {
                IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
                filter.addDataScheme("content");
                filter.addDataAuthority(WearableCalendarContract.AUTHORITY, null);
                CircaTextService.this.registerReceiver(mReceiver, filter);
            }
            {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_POWER_CONNECTED);
                filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                filter.addAction(Intent.ACTION_BATTERY_LOW);
                filter.addAction(Intent.ACTION_BATTERY_OKAY);
                CircaTextService.this.registerReceiver(mBatteryHelper.mPowerReceiver, filter);
            }
        }

        private void unregisterReceiver() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "unregisterReceiver()");

            if (!mRegisteredReceiver) {
                return;
            }
            mRegisteredReceiver = false;
            CircaTextService.this.unregisterReceiver(mReceiver);
            CircaTextService.this.unregisterReceiver(mBatteryHelper.mPowerReceiver);
        }

        @Override // WatchFaceService.Engine
        public void onApplyWindowInsets(WindowInsets insets) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onApplyWindowInsets()");

            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = CircaTextService.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            for (DrawableText t : mTextFields) {
                t.setTextSize(textSize);
                t.setDefaultTextSize(textSize);
            }
            if (isInAmbientMode()) {
                for (DrawableText t : mHoursAnimated) {
                    t.setTextSize(textSize*textScaleFactor);
                }
            }
            mTextFields[eTF_DAY_OF_WEEK].setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mTextFields[eTF_DATE].setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mTextFields[eTF_CALENDAR_1].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTextFields[eTF_CALENDAR_2].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTextFields[eTF_BATTERY].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTextFields[eTF_WEATHER_TEMP].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTextFields[eTF_WEATHER_AGE].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size)/1.5f);
            mTextFields[eTF_WEATHER_DESC].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));

            int width = resources.getDisplayMetrics().widthPixels;
            mTextFields[eTF_DATE].setCoord(width - mXOffset, mTextFields[eTF_HOUR], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_DAY_OF_WEEK].setCoord(mXOffset, mTextFields[eTF_HOUR], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_CALENDAR_1].setCoord(mXOffset, mTextFields[eTF_DAY_OF_WEEK], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_CALENDAR_1].setMaxWidth(width - 2 * mXOffset);
            mTextFields[eTF_CALENDAR_2].setCoord(mXOffset, mTextFields[eTF_CALENDAR_1], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_CALENDAR_2].setMaxWidth(width - 2 * mXOffset);
            mTextFields[eTF_BATTERY].setCoord(width - mXOffset, mTextFields[eTF_HOUR], DrawableText.StackDirection.ABOVE);
            mTextFields[eTF_WEATHER_TEMP].setCoord(mXOffset, mTextFields[eTF_CALENDAR_2], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_WEATHER_AGE].setCoord(mTextFields[eTF_WEATHER_TEMP], mTextFields[eTF_CALENDAR_2], DrawableText.StackDirection.NEXTTO);
            mTextFields[eTF_WEATHER_DESC].setCoord(width - mXOffset, mTextFields[eTF_CALENDAR_2], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_HOUR].setCoord(mXOffset, mYOffset);
            mTextFields[eTF_COLON_1].setCoord(mTextFields[eTF_HOUR], mYOffset);
            mTextFields[eTF_MINUTE].setCoord(mTextFields[eTF_COLON_1], mYOffset);
            mTextFields[eTF_COLON_2].setCoord(mTextFields[eTF_MINUTE], mYOffset);
            mTextFields[eTF_SECOND].setCoord(mTextFields[eTF_COLON_2], mYOffset);
        }

        @Override // WatchFaceService.Engine
        public void onPropertiesChanged(Bundle properties) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onPropertiesChanged()");

            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mTextFields[eTF_HOUR].setTypeface(burnInProtection ? DrawableText.NORMAL_TYPEFACE : DrawableText.BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override // WatchFaceService.Engine
        public void onTimeTick() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onTimeTick()");

            long now = System.currentTimeMillis();
            if (mWeatherRequested == null || (now - mWeatherRequested.getTime() > 15*60*1000)) {
                mWeatherRequested = new Date(now);
                Wearable.MessageApi.sendMessage(mGoogleApiClient, "", CircaTextConsts.REQUIRE_WEATHER_MESSAGE, null);
            }

            super.onTimeTick();
            invalidate();
        }

        float textScaleFactor = 1.55f;

        @Override // WatchFaceService.Engine
        public void onAmbientModeChanged(boolean inAmbientMode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onAmbientModeChanged()");

            super.onAmbientModeChanged(inAmbientMode);

            mBackgroundPaint.setColor(isInAmbientMode() ? mInteractiveBackgroundColor :
                                                          CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);

            if (mLowBitAmbient) {
                for (DrawableText t : mTextFields) {
                    t.setAmbientMode(inAmbientMode);
                }
            }
            if (!inAmbientMode) {
                for (DrawableText dt : mTextFieldsAnimated) {
                    createIntAnimation(dt, "alpha", 0, 255);
                }
                for (DrawableText dt : mHoursAnimated) {
                    createTextSizeAnimation(dt, dt.getDefaultTextSize() * textScaleFactor, dt.getDefaultTextSize());
                }
            } else {
                for (DrawableText dt : mHoursAnimated) {
                    createTextSizeAnimation(dt, dt.getDefaultTextSize(), dt.getDefaultTextSize() * textScaleFactor);
                }
            }

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
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
                    invalidate();
                }
            });
            anim.start();
        }

        @Override // WatchFaceService.Engine
        public void onInterruptionFilterChanged(int interruptionFilter) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onInterruptionFilterChanged()");

            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE;
            updateTimer();

            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                updateVisibility();
                invalidate();
            }
        }

        private void updateVisibility() {
            for(DrawableText t : mTextFields) {
                t.hide();
            }

            mTextFields[eTF_HOUR].show();
            mTextFields[eTF_COLON_1].show();
            mTextFields[eTF_MINUTE].show();

            // draw the rest only when not in mute mode
            if (mMute) return;

            if (getPeekCardPosition().isEmpty()) {
                mTextFields[eTF_DAY_OF_WEEK].show();
                mTextFields[eTF_DATE].show();
            }

            // draw the rest only when not in ambient mode
            if(isInAmbientMode()) return;

            mTextFields[eTF_COLON_2].show();
            mTextFields[eTF_SECOND].show();
            mTextFields[eTF_BATTERY].show();

            // if peek card is shown, exit
            if (getPeekCardPosition().isEmpty()) {
                mTextFields[eTF_CALENDAR_1].show();
                mTextFields[eTF_CALENDAR_2].show();

                if (mWeather != null) {
                    mTextFields[eTF_WEATHER_TEMP].show();
                    mTextFields[eTF_WEATHER_AGE].show();
                    mTextFields[eTF_WEATHER_DESC].show();
                }
            }
        }

        private void updateTimer() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "updateTimer()");

            mUpdateHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override // WatchFaceService.Engine
        public void onDraw(Canvas canvas, Rect bounds) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onDraw()");

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            mDate.setTime(now);

            {
                String hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
                mTextFields[eTF_HOUR].setText(hourString);
                String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
                mTextFields[eTF_MINUTE].setText(minuteString);
                String secondString = formatTwoDigitNumber(mCalendar.get(Calendar.SECOND));
                mTextFields[eTF_SECOND].setText(secondString);
                BatteryHelper.BatteryInfo mBatteryInfo = mBatteryHelper.getBatteryInfo();
                if (mBatteryInfo != null) {
                    String pctText = String.format("%3.0f%%", mBatteryInfo.getPercent() * 100);
                    mTextFields[eTF_BATTERY].setText(pctText);
                } else {
                    mTextFields[eTF_BATTERY].setText("");
                }
                mTextFields[eTF_DAY_OF_WEEK].setText(mDayFormat.format(mDate));
                mTextFields[eTF_DATE].setText(mDateFormat.format(mDate));
            }
            {
                CalendarHelper.EventInfo[] mMeetings = mCalendarHelper.getMeetings();
                int i = 0;
                while (i < mMeetings.length && mMeetings[i].DtStart.getTime() < now) i++;

                mTextFields[eTF_CALENDAR_2].setText("");
                if (i >= mMeetings.length) {
                    mTextFields[eTF_CALENDAR_1].setText("no meetings");
                } else {
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                    mTextFields[eTF_CALENDAR_1].setText(sdf.format(mMeetings[i].DtStart) + " " + mMeetings[i].Title);

                    int additionalEvents = mMeetings.length - 1 - i;
                    if (additionalEvents == 1)
                        mTextFields[eTF_CALENDAR_2].setText("+" + additionalEvents + " additional event");
                    if (additionalEvents > 1)
                        mTextFields[eTF_CALENDAR_2].setText("+" + additionalEvents + " additional events");
                }
            }
            if (mWeather != null) {
                long age = now - mWeather.lastupdate.getTime();
                float ageFloat = age / (60 * 1000);
                String tempText = String.format("%2.1f", mWeather.temperature.getTemp());
                String ageText = String.format("(%.0fm)", ageFloat);
                mTextFields[eTF_WEATHER_TEMP].setText(tempText);
                mTextFields[eTF_WEATHER_AGE].setText(ageText);
                mTextFields[eTF_WEATHER_DESC].setText(mWeather.currentCondition.getCondition());
            }
            if (minAmbientMode != isInAmbientMode()) {
                minAmbientMode = isInAmbientMode();
                updateVisibility();
            }

            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            for (DrawableText t : mTextFields) {
                t.onDraw(canvas, bounds);
            }

        }

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        @Override // DataApi.DataListener
        public void onDataChanged(DataEventBuffer dataEvents) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onDataChanged()");

            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                    continue;
                }

                DataItem dataItem = dataEvent.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                if (dataItem.getUri().getPath().equals(CircaTextConsts.PATH_WITH_FEATURE)) {
                    updateUiForConfigDataMap(config);
                }
                if (dataItem.getUri().getPath().equals(CircaTextConsts.SEND_WEATHER_MESSAGE)) {
                    if (config.containsKey("weather")) {
                        try {
                            mWeather = (Weather) Serializer.deserialize(config.getByteArray("weather"));
                            if (Log.isLoggable(TAG, Log.DEBUG))
                                Log.d(TAG, "onDataChanged(): weather=" + mWeather.toString());
                        } catch (Throwable t) {
                        }
                    }
                }
            }
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnected()");

            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);

            CircaTextUtil.fetchConfigDataMap(mGoogleApiClient,
                    new CircaTextUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            if (Log.isLoggable(TAG, Log.DEBUG))
                                Log.d(TAG, "onConnected().onConfigDataMapFetched()");

                            // If the DataItem hasn't been created yet or some keys are missing,
                            // use the default values.
                            setDefaultValuesForMissingConfigKeys(startupConfig);
                            CircaTextUtil.putConfigDataItem(mGoogleApiClient, CircaTextConsts.PATH_WITH_FEATURE, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
            {
                DataMap blankWeather = new DataMap();
                CircaTextUtil.putConfigDataItem(mGoogleApiClient, CircaTextConsts.SEND_WEATHER_MESSAGE, blankWeather);
            }
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "updateUiForConfigDataMap()");

            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                if (configKey.equals(CircaTextConsts.KEY_EXCLUDED_CALENDARS)) {
                    mCalendarHelper.setExcludedCalendars(config.getString(CircaTextConsts.KEY_EXCLUDED_CALENDARS));
                    uiUpdated = true;
                } else {
                    int color = config.getInt(configKey);
                    if (updateUiForKey(configKey, color)) {
                        uiUpdated = true;
                    }
                }
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        private boolean updateUiForKey(String configKey, int color) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "updateUiForKey()");

            switch (configKey) {
                case CircaTextConsts.KEY_BACKGROUND_COLOR:
                    mInteractiveBackgroundColor = color;
                    if (!isInAmbientMode() && mBackgroundPaint != null) {
                        mBackgroundPaint.setColor(color);
                    }
                    break;
                case CircaTextConsts.KEY_HOURS_COLOR:
                    mTextFields[eTF_HOUR].setColor(color);
                    break;
                case CircaTextConsts.KEY_MINUTES_COLOR:
                    mTextFields[eTF_MINUTE].setColor(color);
                    break;
                case CircaTextConsts.KEY_SECONDS_COLOR:
                    mTextFields[eTF_SECOND].setColor(color);
                    break;
                default:
                    return false;
            }
            return true;
        }

            }
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionSuspended()");
        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult result) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionFailed()");
        }


    }
}
