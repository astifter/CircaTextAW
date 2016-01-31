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
public class WundergroundJSONParser extends JSONWeatherParser {
    public Weather getWeather(String data, Address address) throws JSONException {
//      {
//          "response": {
//          "version":"0.1",
//                  "termsofService":"http://www.wunderground.com/weather/api/d/terms.html",
//                  "features": {
//              "conditions": 1
//          }
//      }
//          ,	"current_observation": {
//          "image": {
//              "url":"http://icons.wxug.com/graphics/wu2/logo_130x80.png",
//                      "title":"Weather Underground",
//                      "link":"http://www.wunderground.com"
//          },
//          "display_location": {
//              "full":"Stetten, Austria",
//                      "city":"Stetten",
//                      "state":"",
//                      "state_name":"Austria",
//                      "country":"OS",
//                      "country_iso3166":"AT",
//                      "zip":"00000",
//                      "magic":"3",
//                      "wmo":"11035",
//                      "latitude":"48.360000",
//                      "longitude":"16.380000",
//                      "elevation":"165.00000000"
//          },
//          "observation_location": {
//              "full":"private Wetterstation Korneuburg, Korneuburg, NIEDERÃ¶STERREICH",
//                      "city":"private Wetterstation Korneuburg, Korneuburg",
//                      "state":"NIEDERÃ¶STERREICH",
//                      "country":"ÖSTERREICH",
//                      "country_iso3166":"AT",
//                      "latitude":"48.337051",
//                      "longitude":"16.330730",
//                      "elevation":"551 ft"
//          },
//          "estimated": {
//          },
//          "station_id":"INIEDERS722",
//                  "observation_time":"Last Updated on January 27, 11:03 PM CET",
//                  "observation_time_rfc822":"Wed, 27 Jan 2016 23:03:17 +0100",
//                  "observation_epoch":"1453932197",
//                  "local_time_rfc822":"Wed, 27 Jan 2016 23:03:49 +0100",
//                  "local_epoch":"1453932229",
//                  "local_tz_short":"CET",
//                  "local_tz_long":"Europe/Vienna",
//                  "local_tz_offset":"+0100",
//                  "weather":"Mostly Cloudy",
//                  "temperature_string":"46.3 F (7.9 C)",
//                  "temp_f":46.3,
//                  "temp_c":7.9,
//                  "relative_humidity":"73%",
//                  "wind_string":"Calm",
//                  "wind_dir":"South",
//                  "wind_degrees":171,
//                  "wind_mph":0.0,
//                  "wind_gust_mph":0,
//                  "wind_kph":0,
//                  "wind_gust_kph":0,
//                  "pressure_mb":"1022",
//                  "pressure_in":"30.18",
//                  "pressure_trend":"0",
//                  "dewpoint_string":"38 F (3 C)",
//                  "dewpoint_f":38,
//                  "dewpoint_c":3,
//                  "heat_index_string":"NA",
//                  "heat_index_f":"NA",
//                  "heat_index_c":"NA",
//                  "windchill_string":"46 F (8 C)",
//                  "windchill_f":"46",
//                  "windchill_c":"8",
//                  "feelslike_string":"46 F (8 C)",
//                  "feelslike_f":"46",
//                  "feelslike_c":"8",
//                  "visibility_mi":"18.6",
//                  "visibility_km":"30.0",
//                  "solarradiation":"0",
//                  "UV":"0.0","precip_1hr_string":"-999.00 in ( 0 mm)",
//                  "precip_1hr_in":"-999.00",
//                  "precip_1hr_metric":" 0",
//                  "precip_today_string":"0.00 in (0 mm)",
//                  "precip_today_in":"0.00",
//                  "precip_today_metric":"0",
//                  "icon":"mostlycloudy",
//                  "icon_url":"http://icons.wxug.com/i/c/k/nt_mostlycloudy.gif",
//                  "forecast_url":"http://www.wunderground.com/global/stations/11035.html",
//                  "history_url":"http://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=INIEDERS722",
//                  "ob_url":"http://www.wunderground.com/cgi-bin/findweather/getForecast?query=48.337051,16.330730",
//                  "nowcast":""
//      }
//      }
        Weather weather = new Weather("no error");

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

        weather.clearErrorMessage();
        return weather;
    }

    @Override
    public URL getURL(android.location.Location location, String cityName) {
        // http://api.wunderground.com/api/73fe799c5aba8d79/conditions/pws:1/q/48.364748,16.387343.json
        String BASE_URL = "http://api.wunderground.com/api/73fe799c5aba8d79/conditions/pws:1/q/" + location.getLatitude() + "," + location.getLongitude() + ".json";
        try {
            return new URL(BASE_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
