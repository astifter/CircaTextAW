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
public class YahooJSONParser extends JSONWeatherParser {
    public Weather getWeather(String data, Address address) throws JSONException {
//      {"query":
//          {"count":1,
//           "created":"2016-01-27T21:36:11Z",
//           "lang":"en-GB",
//           "results":
//              {"channel":
//                  {"item":
//                      {"condition":
//                          {"code":"26",
//                           "date":"Wed, 27 Jan 2016 9:59 pm CET",
//                           "temp":"53",
//                           "text":"Cloudy"
//      }   }   }   }   }   }
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
        weather.currentCondition.setWeatherId(getInt("code", condition));
        weather.currentCondition.setDescr(Weather.translateYahoo(weather.currentCondition.getWeatherId()));

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
        // https://query.yahooapis.com/v1/public/yql?q=select%20item.condition%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22AT%2CStetten%22)&format=json
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
