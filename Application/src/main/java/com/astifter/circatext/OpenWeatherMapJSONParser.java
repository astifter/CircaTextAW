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

import com.astifter.circatextutils.Location;
import com.astifter.circatextutils.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

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
public class OpenWeatherMapJSONParser implements JSONWeatherParser {

    public Weather getWeather(String data) throws JSONException {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        // We start extracting the info
        Location loc = new Location();

        JSONObject coordObj = getObject("coord", jObj);
        loc.setLatitude(getFloat("lat", coordObj));
        loc.setLongitude(getFloat("lon", coordObj));

        JSONObject sysObj = getObject("sys", jObj);
        loc.setCountry(getString("country", sysObj));
        loc.setSunrise(getInt("sunrise", sysObj));
        loc.setSunset(getInt("sunset", sysObj));
        loc.setCity(getString("name", jObj));
        weather.location = loc;

        // We get weather info (This is an array)
        JSONArray jArr = jObj.getJSONArray("weather");

        // We use only the first value
        JSONObject JSONWeather = jArr.getJSONObject(0);
        weather.currentCondition.setWeatherId(getInt("id", JSONWeather));
        weather.currentCondition.setDescr(getString("description", JSONWeather));
        weather.currentCondition.setCondition(getString("main", JSONWeather));

        JSONObject mainObj = getObject("main", jObj);
        weather.currentCondition.setHumidity(getInt("humidity", mainObj));
        weather.currentCondition.setPressure(getInt("pressure", mainObj));
        weather.temperature.setMaxTemp(getFloat("temp_max", mainObj));
        weather.temperature.setMinTemp(getFloat("temp_min", mainObj));
        weather.temperature.setTemp(getFloat("temp", mainObj));

        // Wind
        try {
            JSONObject wObj = getObject("wind", jObj);
            weather.wind.setSpeed(getFloat("speed", wObj));
            weather.wind.setDeg(getFloat("deg", wObj));
        } catch (Exception e) {
            weather.wind.setSpeed(0f);
            weather.wind.setDeg(0f);
        }

        // Clouds
        JSONObject cObj = getObject("clouds", jObj);
        weather.clouds.setPerc(getInt("all", cObj));

        long dt = getLong("dt", jObj);
        weather.time = new Date(dt*1000);
        weather.lastupdate = weather.time;

        return weather;
    }

    @Override
    public URL getURL(android.location.Location location, String cityName) {
        // api.openweathermap.org/data/2.5/weather?lat=35&lon=139
        String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric&APPID=d9b53ed2098c82758e054eec0d9112d5&";
        String lat = "lat=" + Double.toString(location.getLatitude());
        String lon = "lon=" + Double.toString(location.getLongitude());
        try {
            return new URL(BASE_URL + lat + "&" + lon);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
}
