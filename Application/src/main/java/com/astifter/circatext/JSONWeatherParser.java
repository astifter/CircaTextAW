package com.astifter.circatext;

import android.location.Location;

import com.astifter.circatextutils.Weather;

import org.json.JSONException;

import java.net.URL;

public interface JSONWeatherParser {
    Weather getWeather(String data) throws JSONException;
    URL getURL(Location location, String cityName);
}
