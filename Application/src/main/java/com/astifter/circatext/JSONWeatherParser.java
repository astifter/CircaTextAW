package com.astifter.circatext;

import android.location.Address;
import android.location.Location;

import com.astifter.circatextutils.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

abstract class JSONWeatherParser {
    abstract Weather getWeather(String data, Address address) throws JSONException;

    abstract URL getURL(Location location, String cityName);

    static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getJSONObject(tagName);
    }

    static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }

    static long getLong(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getLong(tagName);
    }
}
