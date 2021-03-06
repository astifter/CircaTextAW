/**
 * This is a tutorial source code
 * provided "as is" and without warranties.
 * <p/>
 * For any question please visit the web site
 * http://www.survivingwithandroid.com
 * <p/>
 * or write an email to
 * survivingwithandroid@gmail.com
 */
package com.astifter.circatext;

import android.annotation.SuppressLint;
import android.location.Address;

import com.astifter.circatextutils.Location;
import com.astifter.circatextutils.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
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
public class YahooJSONParser implements JSONWeatherParser {

    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getJSONObject(tagName);
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }

    private static long getLong(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getLong(tagName);
    }

    public Weather getWeather(String data, Address address) throws JSONException {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        JSONObject queryObj = getObject("query", jObj);
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            weather.time = sdf.parse(getString("created", queryObj));
        } catch (Throwable t) {
            weather.time = null;
        }
        JSONObject resultsObj = getObject("results", queryObj);
        JSONObject channelObj = getObject("channel", resultsObj);
        JSONObject itemObj = getObject("item", channelObj);
        JSONObject condition = getObject("condition", itemObj);

        weather.currentCondition.setCondition(getString("text", condition));
        int code = getInt("code", condition);
        weather.currentCondition.setWeatherId(code);
        weather.currentCondition.setDescr(Weather.translateYahoo(code));
        float temperatureF = getFloat("temp", condition);
        float temperatureC = (temperatureF - 32f) / 1.8f;
        weather.temperature.setTemp(temperatureC);
        try {
            // Tue, 04 Aug 2015 10:59 pm CEST
            Locale l = Locale.ENGLISH;
            SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy hh:mm a", l);
            String date = getString("date", condition).replace("pm", "PM").replace("am", "AM");
            weather.lastupdate = sdf.parse(date);
        } catch (Throwable t) {
            weather.lastupdate = null;
        }

        Location loc = new Location();
        loc.setCountry(address.getCountryCode());
        loc.setCity(address.getLocality());
        weather.location = loc;

        return weather;
    }

    @Override
    public URL getURL(android.location.Location location, String cityName) {
        String urlCityName = cityName.replace(",", "%2C");
        String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=select%20item.condition%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" + urlCityName + "%22)&format=json";
        try {
            return new URL(BASE_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
