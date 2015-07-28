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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.CircaTextUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sample digital watch face with blinking colons and seconds. In ambient mode, the seconds not
 * shown and the colons don't blink. On devices with low-bit ambient mode, the text is drawn without
 * anti-aliasing in ambient mode. On devices which require burn-in protection, the hours are drawn
 * in normal rather than bold. The time is drawn with less contrast and without seconds in mute
 * mode.
 */
public class CircaTextService extends CanvasWatchFaceService {
    private static final String TAG = "CircaTextService";

    private static final Typeface BOLD_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for normal (not ambient and not mute) mode. We update twice
     * a second to blink the colons.
     */
    private static final long NORMAL_UPDATE_RATE_MS = 500;

    /**
     * Update rate in milliseconds for mute mode. We update every minute, like in ambient mode.
     */
    private static final long MUTE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreateEngine()");
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine
                      implements DataApi.DataListener,
                                 GoogleApiClient.ConnectionCallbacks,
                                 GoogleApiClient.OnConnectionFailedListener {

        Engine() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Engine()");

            for (int i = 0; i < eTF_SIZE; i++) {
                mTextFields[i] = new DrawableText();
            }
            mExcludedCalendars = new HashSet<>();
        }

        private static final String COLON_STRING = ":";

        Calendar mCalendar;
        Date mDate;
        SimpleDateFormat mDayFormat;
        SimpleDateFormat mDateFormat;

        class EventInfo implements Comparable<EventInfo> {
            public final String Title;
            private final Date DtStart;

            EventInfo(String title, Date c) {
                Title = title;
                DtStart = c;
            }

            @Override
            public int compareTo(@NonNull EventInfo another) {
                long thistime = this.DtStart.getTime();
                long othertime = another.DtStart.getTime();

                if (othertime < thistime)
                    return 1;
                if (thistime < othertime)
                    return -1;
                return 0;
            }
        }
        ReadWriteLock mMeetingsLock = new ReentrantReadWriteLock();
        EventInfo mMeetings[];
        ReadWriteLock mExcludedCalendarsLock = new ReentrantReadWriteLock();
        private Set<String> mExcludedCalendars;

        class BatteryInfo {
            private final int mStatus;
            private final int mPlugged;
            private final float mPercent;
            private final int mTemperature;

            BatteryInfo(int status, int plugged, float pct, int temp) {
                mStatus = status;
                mPlugged = plugged;
                mPercent = pct;
                mTemperature = temp;
            }

            boolean isCharging() {
                return mStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                        mStatus == BatteryManager.BATTERY_STATUS_FULL;
            }

            boolean isPlugged() {
                return mPlugged == (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB | BatteryManager.BATTERY_PLUGGED_WIRELESS);
            }

            public float getPercent() {
                return mPercent;
            }

            public int getTemperature() {
                return mTemperature;
            }
        }
        ReadWriteLock mBatteryInfoLock = new ReentrantReadWriteLock();
        BatteryInfo mBatteryInfo;

        boolean mMute;
        boolean mShouldDrawColons;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        class DrawableText {
            class StackDirection {
                static final int NONE = -1;
                static final int HORIZONTAL = 0;
                static final int ABOVE = 1;
                static final int BELOW = 2;
                private final int dir;

                public StackDirection(int dir) {
                    this.dir = dir;
                }

                public int direction() {
                    return dir;
                }
            };

            private TextPaint createTextPaint(Typeface t, Paint.Align a) {
                TextPaint paint = new TextPaint();
                paint.setColor(this.color);
                paint.setTypeface(t);
                paint.setAntiAlias(true);
                paint.setTextAlign(a);
                return paint;
            }

            private float x;
            private float y;
            private float maxWidht = -1;
            private Paint paint;
            private int   color;
            private float textSize;
            private int alpha;
            WeakReference<DrawableText> stack;
            StackDirection stackDirection;
            private float drawnsize;

            public DrawableText() {
                this.paint = new Paint();
                this.stackDirection = new StackDirection(StackDirection.NONE);
            }

            public DrawableText(int c) {
                this.color = c;
                this.paint = createTextPaint(NORMAL_TYPEFACE, Paint.Align.LEFT);
            }

            public DrawableText(int c, Paint.Align a) {
                this.color = c;
                this.paint = createTextPaint(NORMAL_TYPEFACE, a);
            }

            public DrawableText(int c, Typeface t) {
                this.color = c;
                this.paint = createTextPaint(t, Paint.Align.LEFT);
            }

            public void draw(Canvas canvas, String text) {
                float x = this.x;
                float y = this.y;
                if (this.stack != null && this.stack.get() != null) {
                    switch (this.stackDirection.direction()) {
                        case StackDirection.HORIZONTAL:
                            x = this.stack.get().getRight();
                            break;
                        case StackDirection.BELOW:
                            y = this.stack.get().getBottom() + -this.paint.ascent();
                            break;
                        case StackDirection.ABOVE:
                            y = this.stack.get().getTop() - this.paint.descent();
                            break;
                    }
                }

                /**
                 * Some comments are in order:
                 * We first measure the text to be drawn. In case the maximum width is set and the
                 * text will exceed it do:
                 * - Get the font metrics and measure the overflow text "..." (ellipsis).
                 * - Draw the ellpsis right at the end of the allowed area (defined by x, y and
                 *   maxWidht).
                 * - Save the canvas and set a clipping rectangle for the text minus the width of
                 *   the ellipsis.
                 * - Adjust the actually used size (drawnSize) to the maxWidht.
                 */
                boolean hasSavedState = false;
                this.drawnsize = paint.measureText(text);
                if (this.maxWidht != -1 && this.drawnsize > this.maxWidht) {
                    Paint.FontMetrics fm = this.paint.getFontMetrics();
                    float ellipsisSize = paint.measureText("...");

                    canvas.drawText("...", x+this.maxWidht-ellipsisSize, y, paint);

                    canvas.save();
                    hasSavedState = true;
                    canvas.clipRect(x,y+fm.ascent,x+this.maxWidht-ellipsisSize,y+fm.descent);

                    this.drawnsize = this.maxWidht;
                }
                canvas.drawText(text, x, y, paint);
                /** In case the state was saved for clipping text, restore state. */
                if (hasSavedState) {
                    canvas.restore();
                }
                //{
                //    float ds = this.drawnsize;
                //    if (this.paint.getTextAlign() == Paint.Align.RIGHT)
                //        ds = -ds;
                //    canvas.drawLine(x, y, x + ds, y, this.paint);
                //    float a = this.paint.ascent();
                //    canvas.drawLine(x, y + a, x + ds, y + a, this.paint);
                //    float d = this.paint.descent();
                //    canvas.drawLine(x, y + d, x + ds, y + d, this.paint);
                //    canvas.drawLine(x, y + a, x, y + d, this.paint);
                //}
            }

            private float getRight() {
                if (this.stack != null && this.stack.get() != null) {
                    return this.stack.get().getRight() + this.drawnsize;
                } else {
                    return this.x + this.drawnsize;
                }
            }

            private float getHeigth() {
                Paint.FontMetrics fm = this.paint.getFontMetrics();
                return -fm.ascent + fm.descent;
            }

            private float getBottom() {
                if (this.stack != null && this.stack.get() != null) {
                    return this.stack.get().getBottom() + this.getHeigth();
                } else {
                    return this.y + this.paint.descent();
                }
            }

            private float getTop() {
                if (this.stack != null && this.stack.get() != null) {
                    return this.stack.get().getTop() - this.getHeigth();
                } else {
                    return this.y + this.paint.ascent();
                }
            }

            public void setCoord(float x, float y) {
                this.x = x;
                this.y = y;
            }

            public void setCoord(DrawableText t, float y) {
                this.y = y;
                this.stack = new WeakReference<>(t);
                this.stackDirection = new StackDirection(StackDirection.HORIZONTAL);
            }

            public void setCoord(float x, DrawableText t, int d) {
                this.x = x;
                this.stack = new WeakReference<>(t);
                this.stackDirection = new StackDirection(d);
            }

            public void setTextSize(float s) {
                this.paint.setTextSize(s);
            }

            public void setAmbientMode(boolean inAmbientMode) {
                this.paint.setAntiAlias(!inAmbientMode);
                if (inAmbientMode) {
                    this.paint.setColor(Color.WHITE);
                } else {
                    this.paint.setColor(this.color);
                }
            }

            public void setTypeface(Typeface t) {
                this.paint.setTypeface(t);
            }

            public void setColor(int c) {
                this.color = c;
                if (!isInAmbientMode()) {
                    this.paint.setColor(c);
                }
            }

            public void setAlpha(int a) {
                this.paint.setAlpha(a);
            }

            public void setMaxWidht(float maxWidht) {
                this.maxWidht = maxWidht;
            }
        }
        private static final int eTF_DAY_OF_WEEK = 0;
        private static final int eTF_DATE = eTF_DAY_OF_WEEK + 1;
        private static final int eTF_CALENDAR_1 = eTF_DATE + 1;
        private static final int eTF_CALENDAR_2 = eTF_CALENDAR_1  + 1;
        private static final int eTF_BATTERY = eTF_CALENDAR_2  + 1;
        private static final int eTF_HOUR = eTF_BATTERY  + 1;
        private static final int eTF_COLON_1 = eTF_HOUR  + 1;
        private static final int eTF_MINUTE = eTF_COLON_1  + 1;
        private static final int eTF_COLON_2 = eTF_MINUTE  + 1;
        private static final int eTF_SECOND = eTF_COLON_2  + 1;
        private static final int eTF_SIZE = eTF_SECOND  + 1;
        DrawableText[] mTextFields = new DrawableText[eTF_SIZE];
        ArrayList<DrawableText> mTextFieldsAnimated = new ArrayList<>();

        int mInteractiveBackgroundColor = CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;
        Paint mBackgroundPaint;

        float mXOffset;
        float mYOffset;
        float mCalendarOffset;
        float mLineHeight;

        private AsyncTask<Void, Void, Set<EventInfo>> mLoadMeetingsTask;
        static final int MSG_UPDATE_TIME = 0;
        static final int MSG_LOAD_MEETINGS = 1;
        long mInteractiveUpdateRateMs = NORMAL_UPDATE_RATE_MS;

        final Handler mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mUpdateHandler.handleMessage()");
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = mInteractiveUpdateRateMs - (timeMs % mInteractiveUpdateRateMs);
                            mUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;

                    case MSG_LOAD_MEETINGS:
                        restartLoadMeetingTask();
                        break;
                }
            }
        };

        final BroadcastReceiver mPowerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mPowerReceiver.onReceive()");

                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float pct = level / (float) scale;
                mBatteryInfoLock.writeLock().lock();
                try {
                    mBatteryInfo = new BatteryInfo(status, plugged, pct, temp);
                } finally {
                    mBatteryInfoLock.writeLock().unlock();
                }

                invalidate();
            }
        };

        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mReceiver.onReceive()");

                if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) ||
                    Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())      ) {
                    mCalendar.setTimeZone(TimeZone.getDefault());
                    initFormats();
                }
                if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction())     &&
                    WearableCalendarContract.CONTENT_URI.equals(intent.getData())    ) {
                    mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
                }
                invalidate();
            }
        };

        @Override // WatchFaceService.Engine
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate()");
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(CircaTextService.this)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)   // default
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)             // default
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)                                     // default
                    .build());

            Resources resources = CircaTextService.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mCalendarOffset = resources.getDimension(R.dimen.digital_calendar_offset);
            mLineHeight = resources.getDimension(R.dimen.digital_line_height);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);

            mTextFields[eTF_DAY_OF_WEEK] = new DrawableText(resources.getColor(R.color.digital_date));
            mTextFields[eTF_DATE] = new DrawableText(resources.getColor(R.color.digital_date), Paint.Align.RIGHT);
            mTextFields[eTF_CALENDAR_1] = new DrawableText(resources.getColor(R.color.digital_date));
            mTextFields[eTF_CALENDAR_2] = new DrawableText(resources.getColor(R.color.digital_date));
            mTextFields[eTF_BATTERY] = new DrawableText(resources.getColor(R.color.digital_colons), Paint.Align.RIGHT);
            mTextFields[eTF_HOUR] = new DrawableText(CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS, BOLD_TYPEFACE);
            mTextFields[eTF_COLON_1] = new DrawableText(resources.getColor(R.color.digital_colons));
            mTextFields[eTF_MINUTE] = new DrawableText(CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            mTextFields[eTF_COLON_2] = new DrawableText(resources.getColor(R.color.digital_colons));
            mTextFields[eTF_SECOND] = new DrawableText(CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS);
            mTextFieldsAnimated.add(mTextFields[eTF_CALENDAR_1]);
            mTextFieldsAnimated.add(mTextFields[eTF_CALENDAR_2]);
            mTextFieldsAnimated.add(mTextFields[eTF_COLON_2]);
            mTextFieldsAnimated.add(mTextFields[eTF_SECOND]);
            mTextFieldsAnimated.add(mTextFields[eTF_BATTERY]);

            mCalendar = Calendar.getInstance();
            mDate = new Date();
            initFormats();

            mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
        }

        @Override // WatchFaceService.Engine
        public void onDestroy() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onDestroy()");

            mUpdateHandler.removeMessages(MSG_UPDATE_TIME);
            mUpdateHandler.removeMessages(MSG_LOAD_MEETINGS);
            cancelLoadMeetingTask();
            super.onDestroy();
        }

        final GoogleApiClient mGoogleApiClient = CircaTextUtil.buildGoogleApiClient(CircaTextService.this, this, this);

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

        /**
         * Unregistering an unregistered receiver throws an exception. Keep track of the
         * registration state to prevent that.
         */
        boolean mRegisteredReceiver = false;

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
                CircaTextService.this.registerReceiver(mPowerReceiver, filter);
            }
        }

        private void unregisterReceiver() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "unregisterReceiver()");

            if (!mRegisteredReceiver) {
                return;
            }
            mRegisteredReceiver = false;
            CircaTextService.this.unregisterReceiver(mReceiver);
            CircaTextService.this.unregisterReceiver(mPowerReceiver);
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
            }
            mTextFields[eTF_DAY_OF_WEEK].setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mTextFields[eTF_DATE].setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mTextFields[eTF_CALENDAR_1].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTextFields[eTF_CALENDAR_2].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTextFields[eTF_BATTERY].setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));

            int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
            mTextFields[eTF_DATE].setCoord(width - mXOffset, mTextFields[eTF_HOUR], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_DAY_OF_WEEK].setCoord(mXOffset, mTextFields[eTF_HOUR], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_CALENDAR_1].setCoord(mXOffset, mTextFields[eTF_DAY_OF_WEEK], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_CALENDAR_1].setMaxWidht(width - 2 * mXOffset);
            mTextFields[eTF_CALENDAR_2].setCoord(mXOffset, mTextFields[eTF_CALENDAR_1], DrawableText.StackDirection.BELOW);
            mTextFields[eTF_CALENDAR_2].setMaxWidht(width - 2*mXOffset);
            mTextFields[eTF_BATTERY].setCoord(width - mXOffset, mTextFields[eTF_HOUR], DrawableText.StackDirection.ABOVE);
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
            mTextFields[eTF_HOUR].setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override // WatchFaceService.Engine
        public void onTimeTick() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onTimeTick()");

            super.onTimeTick();
            invalidate();
        }

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
                Iterator<DrawableText> it = mTextFieldsAnimated.iterator();
                while (it.hasNext()) {
                    createAnimation(it.next());
                }
            }

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        private void createAnimation(DrawableText t) {
            ValueAnimator anim = ObjectAnimator.ofInt(t, "alpha", 0, 255);
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
            setInteractiveUpdateRateMs(inMuteMode ? MUTE_UPDATE_RATE_MS : NORMAL_UPDATE_RATE_MS);

            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                invalidate();
            }
        }

        public void setInteractiveUpdateRateMs(long updateRateMs) {
            if (updateRateMs == mInteractiveUpdateRateMs) {
                return;
            }
            mInteractiveUpdateRateMs = updateRateMs;

            // Stop and restart the timer so the new update rate takes effect immediately.
            if (shouldTimerBeRunning()) {
                updateTimer();
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

            // Show colons for the first half of each second so the colons blink on when the time
            // updates.
            mShouldDrawColons = (System.currentTimeMillis() % 1000) < 500;

            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            // Draw the hours.
            String hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
            mTextFields[eTF_HOUR].draw(canvas, hourString);

            // In ambient and mute modes, always draw the first colon. Otherwise, draw the
            // first colon for the first half of each second.
            if (isInAmbientMode() || mMute || mShouldDrawColons) {
                mTextFields[eTF_COLON_1].draw(canvas, COLON_STRING);
            }

            // Draw the minutes.
            String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
            mTextFields[eTF_MINUTE].draw(canvas, minuteString);

            // In unmuted interactive mode, draw a second blinking colon followed by the seconds.
            // Otherwise, if we're in 12-hour mode, draw AM/PM
            if (!isInAmbientMode() && !mMute) {
                if (mShouldDrawColons) {
                    mTextFields[eTF_COLON_2].draw(canvas, COLON_STRING);
                }
                String secondString = formatTwoDigitNumber(mCalendar.get(Calendar.SECOND));
                mTextFields[eTF_SECOND].draw(canvas, secondString);

                mBatteryInfoLock.readLock().lock();
                try {
                    if (mBatteryInfo != null) {
                        String pctText = String.format("%3.0f%%", mBatteryInfo.getPercent() * 100);
                        mTextFields[eTF_BATTERY].draw(canvas, pctText);
                    }
                } finally {
                    mBatteryInfoLock.readLock().unlock();
                }
            }

            // Only render the day of week and date if there is no peek card, so they do not bleed
            // into each other in ambient mode.
            if (getPeekCardPosition().isEmpty()) {

                if (!mMute) {
                    mTextFields[eTF_DAY_OF_WEEK].draw(canvas, mDayFormat.format(mDate));
                    mTextFields[eTF_DATE].draw(canvas, mDateFormat.format(mDate));
                }

                if (!isInAmbientMode() && !mMute) {
                    mMeetingsLock.readLock().lock();
                    try {
                        if (mMeetings != null) {
                            if (mMeetings.length == 0) {
                                mTextFields[eTF_CALENDAR_1].draw(canvas, "no meetings");
                            } else {
                                int i = 0;
                                while (mMeetings[i].DtStart.getTime() < now) i++;
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                                mTextFields[eTF_CALENDAR_1].draw(canvas, sdf.format(mMeetings[i].DtStart) + " " + mMeetings[i].Title);

                                int additionalEvents = mMeetings.length - 1 - i;
                                if (additionalEvents == 1)
                                    mTextFields[eTF_CALENDAR_2].draw(canvas, "+" + additionalEvents + " additional event");
                                if (additionalEvents > 1)
                                    mTextFields[eTF_CALENDAR_2].draw(canvas, "+" + additionalEvents + " additional events");
                            }
                        }
                    } finally {
                        mMeetingsLock.readLock().unlock();
                    }
                }
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
                if (!dataItem.getUri().getPath().equals(CircaTextConsts.PATH_WITH_FEATURE)) {
                    continue;
                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                updateUiForConfigDataMap(config);
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
                        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnected().onConfigDataMapFetched()");

                        // If the DataItem hasn't been created yet or some keys are missing,
                        // use the default values.
                        setDefaultValuesForMissingConfigKeys(startupConfig);
                        CircaTextUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                        updateUiForConfigDataMap(startupConfig);
                    }
                }
            );
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "updateUiForConfigDataMap()");

            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                if (configKey.equals(CircaTextConsts.KEY_EXCLUDED_CALENDARS)) {
                    mExcludedCalendarsLock.writeLock().lock();
                    try {
                        mExcludedCalendars.clear();
                        String s[] = config.getString(CircaTextConsts.KEY_EXCLUDED_CALENDARS).split(",");
                        for (int i = 0; i < s.length; i++) {
                            mExcludedCalendars.add(s[i].trim());
                        }
                    } finally {
                        mExcludedCalendarsLock.writeLock().unlock();
                    }
                    restartLoadMeetingTask();
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

        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
            addConfigKeyIfMissing(config, CircaTextConsts.KEY_BACKGROUND_COLOR,
                    CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
            addConfigKeyIfMissing(config, CircaTextConsts.KEY_HOURS_COLOR,
                    CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS);
            addConfigKeyIfMissing(config, CircaTextConsts.KEY_MINUTES_COLOR,
                    CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            addConfigKeyIfMissing(config, CircaTextConsts.KEY_SECONDS_COLOR,
                    CircaTextUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS);
            addConfigKeyIfMissing(config, CircaTextConsts.KEY_EXCLUDED_CALENDARS, "");
        }

        private void addConfigKeyIfMissing(DataMap config, String key, int value) {
            if (!config.containsKey(key)) {
                config.putInt(key, value);
            }
        }

        private void addConfigKeyIfMissing(DataMap config, String key, String value) {
            if (!config.containsKey(key)) {
                config.putString(key, value);
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

        // Meeting Loader Module
        private void onMeetingsLoaded(Set<EventInfo> result) {
            if (result != null) {
                mMeetingsLock.writeLock().lock();
                try {
                    mMeetings = result.toArray(new EventInfo[result.size()]);
                    Arrays.sort(mMeetings);
                    invalidate();
                } finally {
                    mMeetingsLock.writeLock().unlock();
                }
            }
        }

        private void cancelLoadMeetingTask() {
            if (mLoadMeetingsTask != null) {
                mLoadMeetingsTask.cancel(true);
            }
        }

        private void restartLoadMeetingTask() {
            cancelLoadMeetingTask();
            mLoadMeetingsTask = new LoadMeetingsTask();
            mLoadMeetingsTask.execute();
        }


        /**
         * Asynchronous task to load the meetings from the content provider and report the number of
         * meetings back via {@link #onMeetingsLoaded}.
         */
        private class LoadMeetingsTask extends AsyncTask<Void, Void, Set<EventInfo>> {
            public final String[] EVENT_FIELDS = {
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.CALENDAR_ID,
                    CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            };
            private PowerManager.WakeLock mWakeLock;

            @Override
            protected Set<EventInfo> doInBackground(Void... voids) {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                mWakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, "CalendarWatchFaceWakeLock");
                mWakeLock.acquire();

                long begin = System.currentTimeMillis();
                Uri.Builder builder =
                        WearableCalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, begin);
                ContentUris.appendId(builder, begin + DateUtils.DAY_IN_MILLIS);
                final Cursor cursor = getContentResolver().query(builder.build(),
                        EVENT_FIELDS, null, null, null);

                Set<EventInfo> eis = new HashSet<>();
                while (cursor.moveToNext()) {
                    String cal_name = cursor.getString(3);

                    boolean useThisCalendar = true;
                    mExcludedCalendarsLock.readLock().lock();
                    try {
                        if (mExcludedCalendars.contains(cal_name))
                            useThisCalendar = false;
                    } finally {
                        mExcludedCalendarsLock.readLock().unlock();
                    }
                    if (!useThisCalendar)
                        continue;

                    String title = cursor.getString(0);
                    Date d = new Date(cursor.getLong(1));
                    String cal_id = cursor.getString(2);
                    EventInfo ei = new EventInfo(title, d);
                    eis.add(ei);
                }

                cursor.close();
                return eis;
            }

            @Override
            protected void onPostExecute(Set<EventInfo> result) {
                releaseWakeLock();
                onMeetingsLoaded(result);
            }

            @Override
            protected void onCancelled() {
                releaseWakeLock();
            }

            private void releaseWakeLock() {
                if (mWakeLock != null) {
                    mWakeLock.release();
                    mWakeLock = null;
                }
            }
        }
    }
}
