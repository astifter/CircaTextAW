package com.astifter.circatext;

import android.util.Log;

import com.astifter.circatextutils.CircaTextConsts;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class CircaTextWeatherService extends WearableListenerService {
    private static final String TAG = "CircaTextWeatherService";
    private String mPeerId;
    private GoogleApiClient mGoogleApiClient;

    public CircaTextWeatherService() {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onMessageReceived()");
        super.onMessageReceived(messageEvent);

        mPeerId = messageEvent.getSourceNodeId();

        if (messageEvent.getPath().equals(CircaTextConsts.REQUIRE_WEATHER_MESSAGE)) {
            if (mGoogleApiClient == null)
                mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
            if (!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CircaTextConsts.SEND_WEATHER_MESSAGE, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (Log.isLoggable(TAG, Log.DEBUG))
                        Log.d(TAG, "onMessageReceived(): " + sendMessageResult.toString());

                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = null;
                }
            });
        }
    }
}
