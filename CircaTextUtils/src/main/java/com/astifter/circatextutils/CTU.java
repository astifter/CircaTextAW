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

package com.astifter.circatextutils;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public final class CTU {
    private static final String TAG = "CTU";

    private CTU() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "CTU()");
    }

    public static String getAge(long now, Date lastupdate) {
        if (lastupdate == null) return "?";
        double age = now - lastupdate.getTime();
        double ageFloat = age / (60.0 * 1000);
        return String.format("%.1f", ageFloat);
    }

    public static GoogleApiClient buildBasicAPIClient(Context c) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "buildBasicAPIClient()");

        return buildAPIClient(c,
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult r) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "buildBasicAPIClient().onConnectionFailed(): " + r.toString());
                    }
                });
    }

    private static GoogleApiClient buildAPIClient(Context c,
                                                  GoogleApiClient.OnConnectionFailedListener cfl) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "buildAPIClient()");

        return new GoogleApiClient.Builder(c).addApi(Wearable.API)
                .addOnConnectionFailedListener(cfl)
                .build();
    }

    public static void getAPIData(final GoogleApiClient gAPIClient,
                                  final GetAPIDataCallback callback) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAPIData()");

        Wearable.NodeApi.getLocalNode(gAPIClient).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult r) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "getAPIData().onResult()");

                        String localId = r.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .authority(localId)
                                .path(CTCs.URI_CONFIG)
                                .build();

                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "fetching data with Wearable.DataApi.getDataItem(" + uri.toString() + ")");
                        Wearable.DataApi.getDataItem(gAPIClient, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                });
    }

    public static void overwriteAPIData(GoogleApiClient gAPIClient, String key, String data) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "overwriteAPIData(String)");

        DataMap config = new DataMap();
        config.putString(key, data);
        overwriteAPIData(gAPIClient, config);
    }

    private static void overwriteAPIData(final GoogleApiClient gAPIClient,
                                         final DataMap config) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "overwriteAPIData()");

        CTU.getAPIData(gAPIClient,
                new GetAPIDataCallback() {
                    @Override
                    public void onConfigDataMapFetched(DataMap current) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "overwriteAPIData().onConfigDataMapFetched()");

                        DataMap overwrittenConfig = new DataMap();
                        overwrittenConfig.putAll(current);
                        overwrittenConfig.putAll(config);
                        CTU.putAPIData(gAPIClient, CTCs.URI_CONFIG, overwrittenConfig);
                    }
                });
    }

    public static void putAPIData(GoogleApiClient gAPIClient, String path, DataMap config) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "putAPIData()");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(config);

        Wearable.DataApi.putDataItem(gAPIClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult r) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "putAPIData().onResult(): " + r.toString());
                    }
                });
    }

    public static void sendAPIMessage(GoogleApiClient client, String target, String msg) {
        if (Log.isLoggable(TAG, Log.DEBUG))
            Log.d(TAG, "sendAPIMessage(" + target + ", " + msg + ")");

        Wearable.MessageApi.sendMessage(client, target, msg, null)
                .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult r) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "sendAPIMessage().onResult(): " + r.toString());
                    }
                });
    }

    public static void connectAPI(GoogleApiClient client, final ConnectAPICallback cb) {
        if (!client.isConnected()) {
            client.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle b) {
                    if (Log.isLoggable(TAG, Log.DEBUG))
                        Log.d(TAG, "buildBasicAPIClient().onConnected()");
                    if (cb != null)
                        cb.onConnected();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    if (Log.isLoggable(TAG, Log.DEBUG))
                        Log.d(TAG, "buildBasicAPIClient().onConnectionSuspended(): " + i);
                }
            });
            client.connect();
            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "connectAPI()");
        }
    }

    public static void disconnectAPI(GoogleApiClient client) {
        if (client != null && client.isConnected()) {
            client.disconnect();
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "disconnectAPI(): client.disconnect()");
        }
    }

    public interface GetAPIDataCallback {
        void onConfigDataMapFetched(DataMap config);
    }

    public interface ConnectAPICallback {
        void onConnected();
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {
        private final GetAPIDataCallback mCallback;

        public DataItemResultCallback(GetAPIDataCallback callback) {
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "DataItemResultCallback.DataItemResultCallback()");

            mCallback = callback;
        }

        @Override
        public void onResult(DataApi.DataItemResult r) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "DataItemResultCallback.onResult()");

            if (r.getStatus().isSuccess() && r.getDataItem() != null) {
                DataItem configDataItem = r.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                DataMap config = dataMapItem.getDataMap();
                mCallback.onConfigDataMapFetched(config);
            } else {
                mCallback.onConfigDataMapFetched(new DataMap());
            }
        }
    }
}
