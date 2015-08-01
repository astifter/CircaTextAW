package com.astifter.circatext.datahelpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by astifter on 01.08.15.
 */
public class BatteryHelper {
    private static final String TAG = "CircaTextService";
    private final CanvasWatchFaceService.Engine engine;

    public BatteryHelper(CanvasWatchFaceService.Engine engine) {
        this.engine = engine;
    }

    public BatteryInfo getBatteryInfo() {
        BatteryInfo returnValue = null;
        mBatteryInfoLock.readLock().lock();
        try {
            if (mBatteryInfo != null) {
                returnValue = mBatteryInfo;
            }
        } finally {
            mBatteryInfoLock.readLock().unlock();
        }
        return returnValue;
    }

    public class BatteryInfo {
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

        public float getPercent() {
            return mPercent;
        }
    }

    public final BroadcastReceiver mPowerReceiver = new BroadcastReceiver() {
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

            engine.invalidate();
        }
    };

    private final ReadWriteLock mBatteryInfoLock = new ReentrantReadWriteLock();
    private BatteryInfo mBatteryInfo;
}
