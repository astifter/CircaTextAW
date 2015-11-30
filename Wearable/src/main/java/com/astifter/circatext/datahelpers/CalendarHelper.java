package com.astifter.circatext.datahelpers;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CalendarHelper {
    private final CanvasWatchFaceService.Engine engine;
    private final Context context;
    private final ReadWriteLock mMeetingsLock = new ReentrantReadWriteLock();
    private final ReadWriteLock mExcludedCalendarsLock = new ReentrantReadWriteLock();
    private final Set<String> mExcludedCalendars = new HashSet<>();
    private EventInfo mMeetings[];
    private AsyncTask<Void, Void, Set<EventInfo>> mLoadMeetingsTask;

    public CalendarHelper(CanvasWatchFaceService.Engine engine, Context applicationContext) {
        this.engine = engine;
        this.context = applicationContext;
    }

    public EventInfo[] getMeetings() {
        EventInfo[] returnValue = new EventInfo[0];
        mMeetingsLock.readLock().lock();
        try {
            if (mMeetings != null) {
                returnValue = mMeetings;
            }
        } finally {
            mMeetingsLock.readLock().unlock();
        }
        return returnValue;
    }

    public void setExcludedCalendars(String configString) {
        mExcludedCalendarsLock.writeLock().lock();
        try {
            mExcludedCalendars.clear();
            for (String s : configString.split(",")) {
                mExcludedCalendars.add(s.trim());
            }
        } finally {
            mExcludedCalendarsLock.writeLock().unlock();
        }
        restartLoadMeetingTask();
    }

    public void cancelLoadMeetingTask() {
        if (mLoadMeetingsTask != null) {
            mLoadMeetingsTask.cancel(true);
        }
    }

    public void restartLoadMeetingTask() {
        cancelLoadMeetingTask();
        mLoadMeetingsTask = new LoadMeetingsTask();
        mLoadMeetingsTask.execute();
    }

    private void onMeetingsLoaded(Set<EventInfo> result) {
        if (result != null) {
            mMeetingsLock.writeLock().lock();
            try {
                mMeetings = result.toArray(new EventInfo[result.size()]);
                Arrays.sort(mMeetings);
                engine.invalidate();
            } finally {
                mMeetingsLock.writeLock().unlock();
            }
        }
    }

    public class EventInfo implements Comparable<EventInfo> {
        public final String Title;
        public final Date DtStart;
        public final boolean Hidden;
        public final boolean Disabled;
        public final String Description;
        public final String Location;
        private final Date DtEnd;
        private final Locale locale;
        public int Color;

        public EventInfo(Cursor cursor, boolean h, boolean d) {
            Title = cursor.getString(0);
            DtStart = new Date(cursor.getLong(1));
            Description = cursor.getString(4);
            Color = cursor.getInt(5);
            DtEnd = new Date(cursor.getLong(6));
            Location = cursor.getString(7);

            Hidden = h;
            Disabled = d;
            locale = Locale.GERMAN;
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

        public String formatDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", locale);
            return sdf.format(this.DtStart);
        }

        public String formatStart() {
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm", locale);
            return sdf.format(this.DtStart);
        }

        public String formatEnd() {
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm", locale);
            return sdf.format(this.DtEnd);
        }
    }

    private class LoadMeetingsTask extends AsyncTask<Void, Void, Set<EventInfo>> {
        private final String[] EVENT_FIELDS = {
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.CALENDAR_ID,
                CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Instances.END,
                CalendarContract.Instances.EVENT_LOCATION,
        };
        private PowerManager.WakeLock mWakeLock;

        @Override
        protected Set<EventInfo> doInBackground(Void... voids) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, "CalendarWatchFaceWakeLock");
            mWakeLock.acquire();

            long begin = System.currentTimeMillis();
            Uri.Builder builder =
                    WearableCalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, begin + DateUtils.DAY_IN_MILLIS);

            Cursor cursor = null;
            Set<EventInfo> eis = new HashSet<>();
            try {
                cursor = context.getContentResolver().query(builder.build(), EVENT_FIELDS, null, null, null);

                while (cursor != null && cursor.moveToNext()) {
                    String cal_name = cursor.getString(3);

                    boolean disabled = false;
                    mExcludedCalendarsLock.readLock().lock();
                    try {
                        if (mExcludedCalendars.contains(cal_name))
                            disabled = true;
                    } finally {
                        mExcludedCalendarsLock.readLock().unlock();
                    }

                    boolean hidden = false;
                    String event_desc = cursor.getString(4);
                    if (event_desc.contains("#nowatch"))
                        hidden = true;

                    EventInfo ei = new EventInfo(cursor, hidden, disabled);
                    eis.add(ei);
                }
            } catch (Throwable t) {
                // log
            } finally {
                if (cursor != null)
                    cursor.close();
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
