package com.astifter.circatext;

import android.location.Address;
import android.location.Location;

import com.astifter.circatextutils.Weather;

import org.json.JSONException;

import java.net.URL;

interface JSONWeatherParser {
    Weather getWeather(String data, Address address) throws JSONException;

    URL getURL(Location location, String cityName);
}
