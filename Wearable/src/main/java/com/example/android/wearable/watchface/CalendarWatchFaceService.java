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

package com.example.android.wearable.watchface;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Proof of concept sample watch face that demonstrates how a watch face can load calendar data.
 */
public class CalendarWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "CalendarWatchFace";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        static final int BACKGROUND_COLOR = Color.BLACK;
        static final int FOREGROUND_COLOR = Color.WHITE;
        static final int TEXT_SIZE = 25;
        static final int MSG_LOAD_MEETINGS = 0;

        /** Editable string containing the text to draw with the number of meetings in bold. */
        final Editable mEditable = new SpannableStringBuilder();

        /** Width specified when {@link #mLayout} was created. */
        int mLayoutWidth;

        /** Layout to wrap {@link #mEditable} onto multiple lines. */
        DynamicLayout mLayout;

        /** Paint used to draw text. */
        final TextPaint mTextPaint = new TextPaint();

        Set<EventInfo> mMeetings;

        private AsyncTask<Void, Void, Set<EventInfo>> mLoadMeetingsTask;

        /** Handler to load the meetings once a minute in interactive mode. */
        final Handler mLoadMeetingsHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LOAD_MEETINGS:
                        cancelLoadMeetingTask();
                        mLoadMeetingsTask = new LoadMeetingsTask();
                        mLoadMeetingsTask.execute();
                        break;
                }
            }
        };

        private boolean mIsReceiverRegistered;

        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction())
                        && WearableCalendarContract.CONTENT_URI.equals(intent.getData())) {
                    cancelLoadMeetingTask();
                    mLoadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(CalendarWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            mTextPaint.setColor(FOREGROUND_COLOR);
            mTextPaint.setTextSize(TEXT_SIZE);

            mLoadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
        }

        @Override
        public void onDestroy() {
            mLoadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
            cancelLoadMeetingTask();
            super.onDestroy();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Create or update mLayout if necessary.
            if (mLayout == null || mLayoutWidth != bounds.width()) {
                mLayoutWidth = bounds.width();
                mLayout = new DynamicLayout(mEditable, mTextPaint, mLayoutWidth,
                        Layout.Alignment.ALIGN_NORMAL, 1 /* spacingMult */, 0 /* spacingAdd */,
                        false /* includePad */);
            }

            // Update the contents of mEditable.
            mEditable.clear();
            if (mMeetings != null) {
                Iterator<EventInfo> e = mMeetings.iterator();
                while (e.hasNext()) {
                    EventInfo ei = e.next();
                    DateFormat sdf = new SimpleDateFormat("H:mm");
                    mEditable.append(sdf.format(ei.DtStart) + " ");
                    mEditable.append(ei.Title + "\n");
                }
            }

            // Draw the text on a solid background.
            canvas.drawColor(BACKGROUND_COLOR);
            mLayout.draw(canvas);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
                filter.addDataScheme("content");
                filter.addDataAuthority(WearableCalendarContract.AUTHORITY, null);
                registerReceiver(mBroadcastReceiver, filter);
                mIsReceiverRegistered = true;

                mLoadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
            } else {
                if (mIsReceiverRegistered) {
                    unregisterReceiver(mBroadcastReceiver);
                    mIsReceiverRegistered = false;
                }
                mLoadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
            }
        }

        private void onMeetingsLoaded(Set<EventInfo> result) {
            if (result != null) {
                mMeetings = result;
                invalidate();
            }
        }

        private void cancelLoadMeetingTask() {
            if (mLoadMeetingsTask != null) {
                mLoadMeetingsTask.cancel(true);
            }
        }

        public class EventInfo {
            public final String Title;
            private final Date DtStart;

            EventInfo(String title, Date c) {
                Title = title;
                DtStart = c;
            }
        }

        /**
         * Asynchronous task to load the meetings from the content provider and report the number of
         * meetings back via {@link #onMeetingsLoaded}.
         */
        private class LoadMeetingsTask extends AsyncTask<Void, Void, Set<EventInfo>> {
            private PowerManager.WakeLock mWakeLock;

            public final String[] EVENT_FIELDS = {
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.CALENDAR_ID,
            };

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

                Set<EventInfo> eis = new HashSet<EventInfo>();
                while (cursor.moveToNext()) {
                    String title = cursor.getString(0);
                    Date d = new Date(cursor.getLong(1));
                    EventInfo ei = new EventInfo(title, d);
                    eis.add(ei);
                }
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