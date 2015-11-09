package com.astifter.circatext;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.astifter.circatextutils.CTCs;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class CircaTextWeatherService extends WearableListenerService {
    private static final String TAG = "CircaTextWeatherService";

    private String mPeerId;
    private GoogleApiClient mGoogleApiClient;

    private JSONWeatherParser weatherParser = new OpenWeatherMapJSONParser();
    //private JSONWeatherParser weatherParser = new YahooJSONParser();

    private Location city;
    private String cityName;
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
            cityName = getCityName(city);

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locManager.removeUpdates(locListener);
        }
    };

    private String getCityName(Location loc) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                Address address = listAddresses.get(0);
                return address.getLocality() + "," + address.getCountryCode();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void getCity() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria searchProviderCriteria = new Criteria();
        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW);
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        searchProviderCriteria.setCostAllowed(false);

        String provider = locManager.getBestProvider(searchProviderCriteria, true);

        city = locManager.getLastKnownLocation(provider);
        cityName = getCityName(city);

        if (city == null || (SystemClock.elapsedRealtime() - city.getTime()) > 10000) {
            locManager.requestSingleUpdate(provider, locListener, null);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onMessageReceived()");
        super.onMessageReceived(messageEvent);

        mPeerId = messageEvent.getSourceNodeId();
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "onMessageReceived(): mPeerId=" + mPeerId);

        if (messageEvent.getPath().equals(CTCs.REQUIRE_WEATHER_MESSAGE)) {
            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "onMessageReceived(): REQUIRE_WEATHER_MESSAGE");
            getCity();
            JSONWeatherTask t = new JSONWeatherTask(this);
            t.execute();
        }
    }

    private class JSONWeatherTask extends AsyncTask<Void, Void, Weather> {
        private final Context context;

        public JSONWeatherTask(Context c) {
            this.context = c;
        }

        @Override
        protected Weather doInBackground(Void... params) {
            URL url = weatherParser.getURL(city, cityName);
            if (url == null) return null;

            String data;
            try {
                data = (new WeatherHttpClient()).getWeatherData(url);
                if (data == null) return null;
            } catch (Throwable t) {
                return null;
            }

            Weather weather = null;
            try {
                weather = weatherParser.getWeather(data);
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
            weatherData.putFloat("temperature", weather.temperature.getTemp());
            weatherData.putString("condition", weather.currentCondition.getCondition());
            weatherData.putString("detailedCondition", weather.currentCondition.getDescr());
            try {
                byte[] data = Serializer.serialize(weather);
                weatherData.putByteArray("weather", data);
            } catch (Exception e) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onPostExecute(): " + e.toString());
            }

            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onPostExecute()");
            if (mGoogleApiClient == null) {
                if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                    Log.d(CTCs.TAGCON, "onPostExecute(): mGoogleApiClient == null");
                mGoogleApiClient = new GoogleApiClient.Builder(this.context).addApi(Wearable.API).build();
            }
            if (!mGoogleApiClient.isConnected()) {
                if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                    Log.d(CTCs.TAGCON, "onPostExecute(): mGoogleApiClient not connected");
                mGoogleApiClient.connect();
            }

            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                Log.d(CTCs.TAGCON, "onPostExecute(): sendMessage()");
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CTCs.SEND_WEATHER_MESSAGE, weatherData.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
                                Log.d(CTCs.TAGCON, "onPostExecute(): onResult=" + sendMessageResult.toString());
                        }
                    });
        }
    }
}
