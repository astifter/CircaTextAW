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
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.CTU;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

public class CircaTextConfigActivity extends Activity {
    private static final String TAG = "CircaTextConfigActivity";

    private GoogleApiClient gAPIClient;
    private String mLocalId;

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circa_text_config);

        gAPIClient = CTU.buildBasicAPIClient(this);
        Wearable.NodeApi.getLocalNode(gAPIClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult r) {
                mLocalId = r.getNode().getId();
            }
        });
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "onCreate(): mLocalId=" + mLocalId);

        TextView htmlText = (TextView) findViewById(R.id.attributions);
        htmlText.setText(Html.fromHtml(getResources().getString(R.string.digital_config_attribution_text)));
        htmlText = (TextView) findViewById(R.id.exclude_calendars_expanation);
        htmlText.setText(Html.fromHtml(getResources().getString(R.string.excluded_calendar_explanation)));

        Button weather_update_button = (Button) findViewById(R.id.weather_update_button);
        weather_update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CTU.sendAPIMessage(gAPIClient, mLocalId, CTCs.URI_GET_WEATHER);
            }
        });

        RadioGroup weatherselection = (RadioGroup) findViewById(R.id.weatherselection);
        weatherselection.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onStart() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onStart()");

        super.onStart();

        CTU.connectAPI(gAPIClient, new CTU.ConnectAPICallback() {
            @Override
            public void onConnected() {
                CTU.getAPIData(gAPIClient, new CTU.GetAPIDataCallback() {
                    @Override
                    public void onConfigDataMapFetched(DataMap config) {
                        setUpAllPickers(config);
                    }
                });
            }
        });
    }

    @Override // Activity
    protected void onStop() {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onStop()");

        CTU.disconnectAPI(gAPIClient);

        super.onStop();
    }

    private void setUpAllPickers(DataMap config) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "setUpAllPickers(): config=" + config.toString());

        setUpEditTextContent(R.id.exclude_calendars, CTCs.KEY_EXCLUDED_CALENDARS, config, "");

        RadioGroup weatherselection = (RadioGroup) findViewById(R.id.weatherselection);
        CTCs.WeatherProvider provider =
                CTCs.WeatherProvider.valueOf(config.getString(CTCs.KEY_USED_WEATHER_PROVIDER, CTCs.WeatherProvider.Yahoo.toString()));
        switch (provider) {
            case OWM: weatherselection.check(R.id.radioButtonOWM); break;
            case Yahoo: weatherselection.check(R.id.radioButtonYahoo); break;
            case WUnderground: weatherselection.check(R.id.radioButtonWUnderground); break;
        }
        weatherselection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                CTCs.WeatherProvider provider = CTCs.WeatherProvider.Yahoo;
                switch (checkedId) {
                    case R.id.radioButtonOWM: provider = CTCs.WeatherProvider.OWM; break;
                    case R.id.radioButtonYahoo: provider = CTCs.WeatherProvider.Yahoo; break;
                    case R.id.radioButtonWUnderground: provider = CTCs.WeatherProvider.WUnderground; break;
                }
                CTU.overwriteAPIData(gAPIClient, CTCs.KEY_USED_WEATHER_PROVIDER, provider.toString());
            }
        });
        weatherselection.setVisibility(View.VISIBLE);
    }

    private void setUpEditTextContent(int editTextId, final String configKey, DataMap config,
                                      String defaultContent) {
        String content = defaultContent;
        if (config != null) {
            content = config.getString(configKey, defaultContent);
            if (content == null)
                content = defaultContent;
        }
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "setUpAllPickers(): content=" + content);

        final EditText editText = (EditText) findViewById(editTextId);
        editText.setText(content);
        editText.addTextChangedListener(new DelayedTextWatcher() {
            @Override
            void onResult() {
                String text = editText.getText().toString();
                if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                    Log.d(CTCs.TAGCON, "setUpEditTextListener(): onResult(): configKey=" + configKey + ", editText=" + text);
                CTU.overwriteAPIData(gAPIClient, configKey, text);
            }
        });
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
