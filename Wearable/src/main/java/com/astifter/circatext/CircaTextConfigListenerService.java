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

import android.os.Bundle;
import android.util.Log;

import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.CTU;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/**
 * A {@link WearableListenerService} listening for {@link CircaTextService} config messages
 * and updating the config {@link com.google.android.gms.wearable.DataItem} accordingly.
 */
public class CircaTextConfigListenerService extends WearableListenerService
                                         implements GoogleApiClient.ConnectionCallbacks,
                                                    GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "CircaTextConfigLS";

    private GoogleApiClient mGoogleApiClient;

    @Override // WearableListenerService
    public void onMessageReceived(MessageEvent messageEvent) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onMessageReceived()");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = CTU.buildGoogleApiClient(this, this, this);
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "onMessageReceived(): built mGoogleApiClient");
        }
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onMessageReceived(): connection failed");
                return;
            } else {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onMessageReceived(): connection sucessful");
            }
        }

        byte[] rawData = messageEvent.getData();
        // It's allowed that the message carries only some of the keys used in the config DataItem
        // and skips the ones that we don't want to change.
        DataMap configKeys = DataMap.fromByteArray(rawData);

        if (messageEvent.getPath().equals(CTCs.SEND_WEATHER_MESSAGE)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onMessageReceived(): weather");

            CTU.putConfigDataItem(mGoogleApiClient, CTCs.SEND_WEATHER_MESSAGE, configKeys);
        }

        if (messageEvent.getPath().equals(CTCs.PATH_WITH_FEATURE)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onMessageReceived(): config");

            CTU.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeys);
        }
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnected()");
    }

    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionSuspended()");
    }

    @Override  // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionFailed()");
        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE)
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionFailed(): API_UNAVAILABLE");
    }
}
