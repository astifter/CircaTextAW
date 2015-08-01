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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CalendarHelper {
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

    public class EventInfo implements Comparable<EventInfo> {
        public final String Title;
        public final Date DtStart;

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

    private final CanvasWatchFaceService.Engine engine;
    private final Context context;

    private final ReadWriteLock mMeetingsLock = new ReentrantReadWriteLock();
    private final ReadWriteLock mExcludedCalendarsLock = new ReentrantReadWriteLock();
    private final Set<String> mExcludedCalendars = new HashSet<>();
    private EventInfo mMeetings[];

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

    private AsyncTask<Void, Void, Set<EventInfo>> mLoadMeetingsTask;
    private class LoadMeetingsTask extends AsyncTask<Void, Void, Set<EventInfo>> {
        private final String[] EVENT_FIELDS = {
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.CALENDAR_ID,
                CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
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
            final Cursor cursor = context.getContentResolver().query(builder.build(), EVENT_FIELDS, null, null, null);

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
                //String cal_id = cursor.getString(2);
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
