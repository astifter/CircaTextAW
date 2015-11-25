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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatext.watchfaces.CircaTextWatchFace;
import com.astifter.circatext.watchfaces.WatchFace;
import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.CTU;
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

import java.util.Date;

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
    public static final int WEATHER_REQUEST_TIMEOUT = 15 * 60 * 1000;

    @Override
    public Engine onCreateEngine() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreateEngine()");
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine
                      implements DataApi.DataListener,
                                 GoogleApiClient.ConnectionCallbacks,
                                 GoogleApiClient.OnConnectionFailedListener {
        static final int MSG_UPDATE_TIME = 0;
        static final int MSG_LOAD_MEETINGS = 1;
        public final GoogleApiClient mGoogleApiClient = CTU.buildGoogleApiClient(CircaTextService.this, this, this);
        private final CalendarHelper mCalendarHelper = new CalendarHelper(this, CircaTextService.this);
        private final BatteryHelper mBatteryHelper = new BatteryHelper(this);
        WatchFace wtf;
        long lastInvalidated = 0;
        long nonUpdate = 0;
        boolean mRegisteredReceiver = false;
        WindowInsets currentWindowInsets;
        boolean lowBitAmbientMode;
        boolean inMuteMode;
        Weather mWeather;
        private Date mWeatherRequested;
        private int  mWeatherRequestTimeOut = 1 * 60 * 1000;
        private Date mWeatherReceived;
        private boolean updateEnabled = true;
        @SuppressLint("HandlerLeak")
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
                    Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())      ) {
                    wtf.localeChanged();
                }
                if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction())     &&
                    WearableCalendarContract.CONTENT_URI.equals(intent.getData())    ) {
                    mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
                }
                invalidate();
            }
        };

        Engine() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Engine()");

            DrawingHelpers.NORMAL_TYPEFACE =
                    Typeface.createFromAsset(getResources().getAssets(),
                                             "fonts/RobotoCondensed-Light.ttf");
            DrawingHelpers.BOLD_TYPEFACE =
                    Typeface.createFromAsset(getResources().getAssets(),
                                             "fonts/RobotoCondensed-Bold.ttf");

            wtf = new CircaTextWatchFace(this);
            wtf.localeChanged();
        }

        @Override
        public synchronized void invalidate() {
            final int FPS = 15;
            final int updateRate = 1000 / FPS;
            long timeMs = System.currentTimeMillis();
            if (((lastInvalidated + updateRate) < timeMs) && updateEnabled) {
                if (lastInvalidated == 0)
                    lastInvalidated = timeMs;
                else
                    lastInvalidated += updateRate;
                super.invalidate();
            } else {
                nonUpdate++;
            }
        }

        @Override // WatchFaceService.Engine
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate()");
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(CircaTextService.this)
                    .setAcceptsTapEvents(true)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_OPAQUE)
                    .setShowSystemUiTime(false)
                    .setShowUnreadCountIndicator(true)
                    .setStatusBarGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .setViewProtectionMode(WatchFaceStyle.PROTECT_WHOLE_SCREEN)
                    .build());

            mUpdateHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            wtf.setPeekCardPosition(rect);
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
                wtf.localeChanged();

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
            currentWindowInsets = insets;

            wtf.setMetrics(getResources(), insets);
            wtf.setPeekCardPosition(getPeekCardPosition());
        }

        @Override // WatchFaceService.Engine
        public void onPropertiesChanged(Bundle properties) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onPropertiesChanged()");

            super.onPropertiesChanged(properties);

            // TODO make sure we conform to the burn-in-protectoin guidelines
            //boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            lowBitAmbientMode = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            wtf.setLowBitAmbientMode(lowBitAmbientMode);
        }

        @Override // WatchFaceService.Engine
        public void onTimeTick() {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onTimeTick()");

            long now = System.currentTimeMillis();

            boolean reRequest = false;
            if (mWeatherRequested == null || ((now - mWeatherRequested.getTime()) > mWeatherRequestTimeOut)) {
                mWeatherRequested = new Date(now);
                reRequest = true;
                if (mWeatherRequestTimeOut < WEATHER_REQUEST_TIMEOUT)
                    mWeatherRequestTimeOut *= 2;
            }
            wtf.setWeatherInfo(mWeather, mWeatherRequested, mWeatherReceived);
            if (reRequest) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onTimeTick() requesting weather update");
                Wearable.MessageApi.sendMessage(mGoogleApiClient, "", CTCs.REQUIRE_WEATHER_MESSAGE, null);
            }

            super.onTimeTick();
            invalidate();
        }

        @Override // WatchFaceService.Engine
        public void onAmbientModeChanged(boolean inAmbientMode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onAmbientModeChanged()");

            super.onAmbientModeChanged(inAmbientMode);
            wtf.setAmbientMode(inAmbientMode);

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override // WatchFaceService.Engine
        public void onInterruptionFilterChanged(int interruptionFilter) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onInterruptionFilterChanged()");

            super.onInterruptionFilterChanged(interruptionFilter);

            inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE;
            wtf.setMuteMode(inMuteMode);
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

            wtf.setBatteryInfo(mBatteryHelper.getBatteryInfo());
            wtf.setEventInfo(mCalendarHelper.getMeetings());

            wtf.onDraw(canvas, bounds);
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
                if (dataItem.getUri().getPath().equals(CTCs.PATH_WITH_FEATURE)) {
                    updateUiForConfigDataMap(config);
                } else if (dataItem.getUri().getPath().equals(CTCs.SEND_WEATHER_MESSAGE)) {
                    if (config.containsKey("weather")) {
                        try {
                            mWeatherReceived = new Date(System.currentTimeMillis());
                            mWeather = (Weather) Serializer.deserialize(config.getByteArray("weather"));

                            wtf.setWeatherInfo(mWeather, mWeatherRequested, mWeatherReceived);
                            if (Log.isLoggable(TAG, Log.DEBUG))
                                Log.d(TAG, "onDataChanged(): weather=" + mWeather.toString());
                        } catch (Throwable t) {
                            if (Log.isLoggable(TAG, Log.DEBUG))
                                Log.d(TAG, "onDataChanged(): failed to fetch weather");
                        }
                    }
                }
            }
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnected()");

            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);

            CTU.fetchConfigDataMap(mGoogleApiClient,
                    new CTU.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            if (Log.isLoggable(TAG, Log.DEBUG))
                                Log.d(TAG, "onConnected().onConfigDataMapFetched()");

                            CTCs.setDefaultValuesForMissingConfigKeys(startupConfig);
                            CTU.putConfigDataItem(mGoogleApiClient, CTCs.PATH_WITH_FEATURE, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
            {
                DataMap blankWeather = new DataMap();
                CTU.putConfigDataItem(mGoogleApiClient, CTCs.SEND_WEATHER_MESSAGE, blankWeather);
            }
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "updateUiForConfigDataMap()");

            boolean uiUpdated = false;
            if (config.containsKey(CTCs.KEY_EXCLUDED_CALENDARS)) {
                mCalendarHelper.setExcludedCalendars(config.getString(CTCs.KEY_EXCLUDED_CALENDARS));
                uiUpdated = true;
            }
            if (config.containsKey(CTCs.KEY_WATCHFACE_CONFIG)) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "updateUiForConfigDataMap(): case CTCs.KEY_WATCHFACE_CONFIG: " + config.getString(CTCs.KEY_WATCHFACE_CONFIG));
                CTCs.Config cfg;
                try {
                    cfg = CTCs.Config.valueOf(config.getString(CTCs.KEY_WATCHFACE_CONFIG));
                } catch (Throwable t) {
                    cfg = CTCs.Config.PLAIN;
                }
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "updateUiForConfigDataMap(): case CTCs.KEY_WATCHFACE_CONFIG: " + cfg.toString());
                wtf.setSelectedConfig(cfg);
                uiUpdated = true;
            }
            if (config.containsKey(CTCs.KEY_WATCHFACE_STRINGER)) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "updateUiForConfigDataMap(): case CTCs.KEY_STRINGER: " + config.getString(CTCs.KEY_WATCHFACE_STRINGER));
                CTCs.Stringer cfg;
                try {
                    cfg = CTCs.Stringer.valueOf(config.getString(CTCs.KEY_WATCHFACE_STRINGER));
                } catch (Throwable t) {
                    cfg = CTCs.Stringer.CIRCA;
                }
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "updateUiForConfigDataMap(): case CTCs.KEY_STRINGER: " + cfg.toString());
                wtf.setStringer(cfg);
                uiUpdated = true;
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionSuspended()");
        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult result) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionFailed()");
            if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE)
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onConnectionFailed(): API_UNAVAILABLE");
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    wtf.startTapHighlight();
                    wtf.getTouchedText(x, y);
                    break;
                case WatchFaceService.TAP_TYPE_TOUCH:
                    break;
                case WatchFaceService.TAP_TYPE_TOUCH_CANCEL:
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                String tapTypeStr = "";
                switch (tapType) {
                    case TAP_TYPE_TAP:
                        tapTypeStr = "TAP_TYPE_TAP";
                        break;
                    case TAP_TYPE_TOUCH:
                        tapTypeStr = "TAP_TYPE_TOUCH";
                        break;
                    case TAP_TYPE_TOUCH_CANCEL:
                        tapTypeStr = "TAP_TYPE_TOUCH_CANCEL";
                        break;
                }
                String output = String.format("%s: x:%d|y:%d", tapTypeStr, x, y);
                Log.d(TAG, output);
            }
        }
    }
}
