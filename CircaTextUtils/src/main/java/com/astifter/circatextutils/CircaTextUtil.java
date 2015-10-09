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
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public final class CircaTextUtil {
    private static final String TAG = "CircaTextUtil";

    private CircaTextUtil() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "CircaTextUtil()");
    }

    public static GoogleApiClient buildGoogleApiClient(
            Context c,
            GoogleApiClient.ConnectionCallbacks cl,
            GoogleApiClient.OnConnectionFailedListener cfl) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "buildGoogleApiClient()");

        return new GoogleApiClient.Builder(c)
                .addConnectionCallbacks(cl)
                .addOnConnectionFailedListener(cfl)
                .addApi(Wearable.API)
                .build();
    }

    public static void fetchConfigDataMap(final GoogleApiClient client,
                                          final FetchConfigDataMapCallback callback) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "fetchConfigDataMap()");

        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "fetchConfigDataMap().onResult()");

                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(CircaTextConsts.PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build();

                        Log.d(TAG, "fetching data with Wearable.DataApi.getDataItem(" + uri.toString() + ")");
                        Wearable.DataApi.getDataItem(client, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                }
        );
    }

    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
                                                    final DataMap configKeysToOverwrite) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "overwriteKeysInConfigDataMap()");

        CircaTextUtil.fetchConfigDataMap(googleApiClient,
                new FetchConfigDataMapCallback() {
                    @Override
                    public void onConfigDataMapFetched(DataMap currentConfig) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "overwriteKeysInConfigDataMap().onConfigDataMapFetched()");

                        DataMap overwrittenConfig = new DataMap();
                        overwrittenConfig.putAll(currentConfig);
                        overwrittenConfig.putAll(configKeysToOverwrite);
                        CircaTextUtil.putConfigDataItem(googleApiClient, CircaTextConsts.PATH_WITH_FEATURE, overwrittenConfig);
                    }
                }
        );
    }

    public static void putConfigDataItem(GoogleApiClient googleApiClient, String path, DataMap newConfig) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "putConfigDataItem()");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (Log.isLoggable(TAG, Log.DEBUG))
                            Log.d(TAG, "putConfigDataItem().onResult()");
                    }
                });
    }

    /// Interface FetchConfigDataMapCallback
    public interface FetchConfigDataMapCallback {
        void onConfigDataMapFetched(DataMap config);
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallback;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "DataItemResultCallback.DataItemResultCallback()");

            mCallback = callback;
        }

        @Override // ResultCallback<DataApi.DataItemResult>
        public void onResult(DataApi.DataItemResult dataItemResult) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "DataItemResultCallback.onResult()");

            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }
}
