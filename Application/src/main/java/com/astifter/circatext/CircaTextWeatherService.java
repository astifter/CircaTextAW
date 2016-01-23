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
import com.astifter.circatextutils.CTU;
import com.astifter.circatextutils.Serializer;
import com.astifter.circatextutils.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class CircaTextWeatherService extends WearableListenerService {
    private static final String TAG = "CircaTextWeatherService";

    private GoogleApiClient gAPIClient;

    //private final JSONWeatherParser weatherParser = new OpenWeatherMapJSONParser();
    private JSONWeatherParser weatherParser = new YahooJSONParser();

    private Location city;
    private Address address;
    private final LocationListener locListener = new LocationListener() {
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
            address = getCityName(city);

            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locManager.removeUpdates(locListener);
        }
    };

    private Address getCityName(Location loc) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                return listAddresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getCity() {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria searchProviderCriteria = new Criteria();
        searchProviderCriteria.setPowerRequirement(Criteria.POWER_LOW);
        searchProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        searchProviderCriteria.setCostAllowed(false);

        String provider = locManager.getBestProvider(searchProviderCriteria, true);

        city = locManager.getLastKnownLocation(provider);
        address = getCityName(city);

        if (city == null || (SystemClock.elapsedRealtime() - city.getTime()) > 10000) {
            locManager.requestSingleUpdate(provider, locListener, null);
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onMessageReceived()");
        super.onMessageReceived(messageEvent);

        if (messageEvent.getPath().equals(CTCs.URI_GET_WEATHER)) {
            gAPIClient = CTU.buildBasicAPIClient(this);
            CTU.connectAPI(gAPIClient, null);

            Wearable.NodeApi.getConnectedNodes(gAPIClient)
                    .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                        @Override
                        public void onResult(NodeApi.GetConnectedNodesResult r) {
                            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) {
                                String mPeerId = messageEvent.getSourceNodeId();
                                Log.d(CTCs.TAGCON, "onMessageReceived(): URI_GET_WEATHER from " + mPeerId);
                            }

                            getCity();
                            JSONWeatherTask t = new JSONWeatherTask();
                            t.setNodes(r.getNodes());
                            t.execute();
                        }
                    });
        }
    }

    private class JSONWeatherTask extends AsyncTask<Void, Void, Weather> {
        private List<Node> nodes;

        @Override
        protected Weather doInBackground(Void... params) {
            String cityString = address.getLocality() + "," + address.getCountryCode();
            URL url = weatherParser.getURL(city, cityString);
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
                weather = weatherParser.getWeather(data, address);
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
            // update data every time with current time which forces retransmission even when data
            // has not changed.
            weatherData.putLong("updated", System.currentTimeMillis());

            if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG)) Log.d(CTCs.TAGCON, "onPostExecute()");
            for (Node n : this.nodes) {
                CTU.putAPIData(gAPIClient, CTCs.URI_PUT_WEATHER, weatherData);
            }
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
        }
    }
}
