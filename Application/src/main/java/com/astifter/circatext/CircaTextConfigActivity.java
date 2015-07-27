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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.CircaTextUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class CircaTextConfigActivity extends Activity
                                  implements GoogleApiClient.ConnectionCallbacks,
                                             GoogleApiClient.OnConnectionFailedListener,
                                             ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = "CircaTextConfigActivity";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circa_text_config);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate(): mPeerId=" + mPeerId);

        mGoogleApiClient = CircaTextUtil.buildGoogleApiClient(this,this,this);

        ComponentName name =
                getIntent().getParcelableExtra(WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate(): name=" + name);

        TextView label = (TextView) findViewById(R.id.label);
        label.setText(label.getText() + " (" + name.getClassName() + ")");
    }

    @Override // Activity
    protected void onStart() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onStart()");

        super.onStart();

        mGoogleApiClient.connect();
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate(): mGoogleApiClient.connect()");
    }

    @Override // Activity
    protected void onStop() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onStop()");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "onStop(): mGoogleApiClient.disconnect()");
        }

        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnected()");

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(CircaTextConsts.PATH_WITH_FEATURE).authority(mPeerId).build();
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnected(): uri=" + uri.toString());

            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }
    }

    private void displayNoConnectedDeviceDialog() {
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionSuspended()");
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onConnectionFailed()");
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onResult()");

        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);
        } else {
            setUpAllPickers(null);
        }
    }

    private void setUpAllPickers(DataMap config) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "setUpAllPickers()");

        setUpColorPickerSelection(R.id.background, CircaTextConsts.KEY_BACKGROUND_COLOR, config, R.string.color_black);
        setUpColorPickerSelection(R.id.hours, CircaTextConsts.KEY_HOURS_COLOR, config, R.string.color_white);
        setUpColorPickerSelection(R.id.minutes, CircaTextConsts.KEY_MINUTES_COLOR, config, R.string.color_white);
        setUpColorPickerSelection(R.id.seconds, CircaTextConsts.KEY_SECONDS_COLOR, config, R.string.color_gray);

        setUpColorPickerListener(R.id.background, CircaTextConsts.KEY_BACKGROUND_COLOR);
        setUpColorPickerListener(R.id.hours, CircaTextConsts.KEY_HOURS_COLOR);
        setUpColorPickerListener(R.id.minutes, CircaTextConsts.KEY_MINUTES_COLOR);
        setUpColorPickerListener(R.id.seconds, CircaTextConsts.KEY_SECONDS_COLOR);
    }

    private void setUpColorPickerSelection(int spinnerId, final String configKey, DataMap config,
                                           int defaultColorNameResId) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "setUpColorPickerSelection()");

        String defaultColorName = getString(defaultColorNameResId);
        int defaultColor = Color.parseColor(defaultColorName);
        int color;

        if (config != null) {
            color = config.getInt(configKey, defaultColor);
        } else {
            color = defaultColor;
        }

        Spinner spinner = (Spinner) findViewById(spinnerId);
        String[] colorNames = getResources().getStringArray(R.array.color_array);
        for (int i = 0; i < colorNames.length; i++) {
            if (Color.parseColor(colorNames[i]) == color) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setUpColorPickerListener(int spinnerId, final String configKey) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "setUpColorPickerListener()");

        Spinner spinner = (Spinner) findViewById(spinnerId);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "setUpColorPickerListener().onItemSelected()");
                final String colorName = (String) adapterView.getItemAtPosition(pos);
                sendConfigUpdateMessage(configKey, Color.parseColor(colorName));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "setUpColorPickerListener().onNothingSelected()");
            }
        });
    }

    private void sendConfigUpdateMessage(String configKey, int color) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "sendConfigUpdateMessage()");

        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(configKey, color);
            byte[] rawData = config.toByteArray();

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CircaTextConsts.PATH_WITH_FEATURE, rawData);
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "sendConfigUpdateMessage(): Wearable.MessageApi.sendMessage()");
        }
    }
}
