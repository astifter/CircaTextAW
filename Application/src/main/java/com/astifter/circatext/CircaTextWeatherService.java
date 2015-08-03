package com.astifter.circatext;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.astifter.circatextutils.CircaTextConsts;
import com.astifter.circatextutils.Serializer;
import com.astifter.circatextutils.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;

public class CircaTextWeatherService extends WearableListenerService {
    private static final String TAG = "CircaTextWeatherService";

    private String mPeerId;
    private GoogleApiClient mGoogleApiClient;
    private Location city;

    private LocationListener locListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(android.location.Location location) {
            city = location;

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locManager.removeUpdates(locListener);
        }
    };

    void getCity() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria searchProviderCriteria = new Criteria();
        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW);
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        searchProviderCriteria.setCostAllowed(false);

        String provider = locManager.getBestProvider(searchProviderCriteria, true);

        city = locManager.getLastKnownLocation(provider);

        if (city == null || (SystemClock.elapsedRealtime() - city.getTime()) > 10000) {
            locManager.requestSingleUpdate(provider, locListener, null);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onMessageReceived()");
        super.onMessageReceived(messageEvent);

        mPeerId = messageEvent.getSourceNodeId();

        if (messageEvent.getPath().equals(CircaTextConsts.REQUIRE_WEATHER_MESSAGE)) {
            getCity();
            JSONWeatherTask t = new JSONWeatherTask(this);
            t.execute(city);
        }
    }

    private class JSONWeatherTask extends AsyncTask<Location, Void, Weather> {
        private final Context context;

        public JSONWeatherTask(Context c) {
            this.context = c;
        }

        @Override
        protected Weather doInBackground(android.location.Location... params) {
            Weather weather = new Weather();
            String data = (new WeatherHttpClient()).getWeatherData(params[0]);
            if (data == null) return null;
            try {
                weather = JSONWeatherParser.getWeather(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            if (weather == null) return;

            DataMap weatherData = new DataMap();
            if (weather.location != null) {
                weatherData.putString("city", weather.location.getCity() + "," + weather.location.getCountry());
            }
            weatherData.putFloat("temperature", weather.temperature.getTemp() - 273.15f);
            weatherData.putString("condition", weather.currentCondition.getCondition());
            weatherData.putString("detailedCondition", weather.currentCondition.getCondition());
            try {
                byte[] data = Serializer.serialize(weather);
                weatherData.putByteArray("weather", data);
            } catch (Exception e) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onPostExecute(): " + e.toString());
            }

            if (mGoogleApiClient == null)
                mGoogleApiClient = new GoogleApiClient.Builder(this.context).addApi(Wearable.API).build();
            if (!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CircaTextConsts.SEND_WEATHER_MESSAGE, weatherData.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (Log.isLoggable(TAG, Log.DEBUG))
                                Log.d(TAG, "onMessageReceived(): " + sendMessageResult.toString());
                        }
                    });
        }
    }
}
