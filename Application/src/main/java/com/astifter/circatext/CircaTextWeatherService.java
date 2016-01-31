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
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
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

public class CircaTextWeatherService extends WearableListenerService implements DataApi.DataListener {
    private static final String TAG = "CircaTextWeatherService";

    private GoogleApiClient gAPIClient;

    static private JSONWeatherParser weatherParser;
    static private Object weatherParserLock = new Object();

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

    @Override
    public void onCreate() {
        super.onCreate();

        gAPIClient = CTU.buildBasicAPIClient(this);
        CTU.connectAPI(gAPIClient, new CTU.ConnectAPICallback() {
            @Override
            public void onConnected() {
                CTU.getAPIData(gAPIClient,
                        new CTU.GetAPIDataCallback() {
                            @Override
                            public void onConfigDataMapFetched(DataMap startupConfig) {
                                if (Log.isLoggable(TAG, Log.DEBUG))
                                    Log.d(TAG, "onConnected().onConfigDataMapFetched()");

                                CTU.putAPIData(gAPIClient, CTCs.URI_CONFIG, startupConfig);

                                updateUiForConfigDataMap(startupConfig);
                            }
                        }
                );
            }
        });
    }

    private void updateUiForConfigDataMap(DataMap config) {
        if (!config.containsKey(CTCs.KEY_USED_WEATHER_PROVIDER))
            return;
        CTCs.WeatherProvider provider =
                CTCs.WeatherProvider.valueOf(config.getString(CTCs.KEY_USED_WEATHER_PROVIDER, CTCs.WeatherProvider.Yahoo.toString()));
        setUpProvider(provider);
    }

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

    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                continue;
            }

            DataItem dataItem = dataEvent.getDataItem();
            if (!dataItem.getUri().getPath().equals(CTCs.URI_CONFIG))
                continue;

            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap config = dataMapItem.getDataMap();

            updateUiForConfigDataMap(config);
        }
    }

    private void setUpProvider(CTCs.WeatherProvider provider) {
        synchronized (weatherParserLock) {
            weatherParser = null;
            switch (provider) {
                case OWM:
                    weatherParser = new OpenWeatherMapJSONParser();
                    break;
                case Yahoo:
                    weatherParser = new YahooJSONParser();
                    break;
                case WUnderground:
                    weatherParser = new WundergroundJSONParser();
                    break;
            }
        }
    }

    private class JSONWeatherTask extends AsyncTask<Void, Void, Weather> {
        private List<Node> nodes;

        @Override
        protected Weather doInBackground(Void... params) {
            synchronized (weatherParserLock) {
                if (weatherParser == null)
                    return new Weather();

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

                Weather weather = new Weather();
                try {
                    weather = weatherParser.getWeather(data, address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return weather;
            }
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
