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
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.CTU;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

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
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "onCreate(): mPeerId=" + mPeerId);

        mGoogleApiClient = CTU.buildGoogleApiClient(this, this, this);

        ComponentName name =
                getIntent().getParcelableExtra(WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate(): name=" + name);

        TextView htmlText = (TextView) findViewById(R.id.attributions);
        htmlText.setText(Html.fromHtml(getResources().getString(R.string.digital_config_attribution_text)));
        htmlText = (TextView) findViewById(R.id.exclude_calendars_expanation);
        htmlText.setText(Html.fromHtml(getResources().getString(R.string.excluded_calendar_explanation)));

    }

    @Override // Activity
    protected void onStart() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onStart()");

        super.onStart();

        mGoogleApiClient.connect();
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "onStart(): mGoogleApiClient.connect()");
    }

    @Override // Activity
    protected void onStop() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onStop()");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "onStart(): mGoogleApiClient.disconnect()");
            mGoogleApiClient.disconnect();
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "onStop(): mGoogleApiClient.disconnect()");
        }

        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onConnected()");

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(CTCs.PATH_WITH_FEATURE).authority(mPeerId).build();
            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "onConnected(): uri=" + uri.toString());

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
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onConnectionSuspended()");
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onConnectionFailed()");
        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE)
            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "onConnectionFailed(): API_UNAVAILABLE");
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onResult()");

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
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "setUpAllPickers(): config=" + config.toString());

        setUpEditTextContent(R.id.exclude_calendars, CTCs.KEY_EXCLUDED_CALENDARS, config, "");
        setUpEditTextListener(R.id.exclude_calendars, CTCs.KEY_EXCLUDED_CALENDARS);
    }

    private void setUpEditTextContent(int editTextId, final String configKey, DataMap config,
                                      String defaultContent) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "setUpAllPickers(): configKey=" + configKey + ", defaultContent=" + defaultContent);

        String content;
        if (config != null) {
            content = config.getString(configKey, defaultContent);
        } else {
            content = defaultContent;
        }
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "setUpAllPickers(): content=" + content);

        EditText editText = (EditText) findViewById(editTextId);
        editText.setText(content);
    }

    private void setUpEditTextListener(final int editTextId, final String configKey) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "setUpEditTextListener()");

        final EditText editText = (EditText) findViewById(editTextId);
        editText.addTextChangedListener(new DelayedTextWatcher() {
            @Override
            void onResult() {
                String text = editText.getText().toString();
                if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                    Log.d(CTCs.TAGCON, "setUpEditTextListener(): onResult(): configKey=" + configKey + ", editText=" + text);
                sendConfigUpdateMessage(configKey, text);
            }
        });
    }

    private void sendConfigUpdateMessage(String configKey, String content) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "sendConfigUpdateMessage()");

        DataMap config = new DataMap();
        config.putString(configKey, content);
        sendGenericConfigUpdateMessage(config);
    }

    private void sendGenericConfigUpdateMessage(DataMap config) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "sendGenericConfigUpdateMessage()");
        byte[] rawData = config.toByteArray();

        if (mPeerId != null) {
            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "sendGenericConfigUpdateMessage(): sendMessage()");
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CTCs.PATH_WITH_FEATURE, rawData);
        }
    }

    abstract class DelayedTextWatcher implements TextWatcher {
        private final long DELAY_MS = 1000;
        private Timer timer = new Timer();

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            if (timer != null)
                timer.cancel();
        }

        @Override
        public void afterTextChanged(final Editable s) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onResult();
                }
            }, DELAY_MS);
        }

        abstract void onResult();
    }
}
