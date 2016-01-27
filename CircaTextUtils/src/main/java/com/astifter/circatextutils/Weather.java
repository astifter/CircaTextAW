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
package com.astifter.circatextutils;

import java.io.Serializable;
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
public class Weather implements Serializable {
    public Location location = new Location();
    public final CurrentCondition currentCondition = new CurrentCondition();
    public final Temperature temperature = new Temperature();
    public final Wind wind = new Wind();
    public Rain rain = new Rain();
    public Snow snow = new Snow();
    public final Clouds clouds = new Clouds();
    public Date time = new Date();
    public Date lastupdate = new Date();

    public static String translateOpenWeather(int weatherId) {
        switch (weatherId) {
            case 200:
                return "Gewitter mit Leichtem Regen";
            case 201:
                return "Gewitter mit Regen";
            case 202:
                return "Gewitter mit Starkem Regen";
            case 210:
                return "Leichtes Gewitter";
            case 211:
                return "Gewitter";
            case 212:
                return "Starkes Gewitter";
            case 221:
                return "Gewitter";
            case 230:
                return "Gewitter mit Leichtem Nieseln";
            case 231:
                return "Gewitter mit Nieseln";
            case 232:
                return "Gewitter mit Starkem Nieseln";

            case 300:
                return "Leichtes Nieseln";
            case 301:
                return "Nieseln";
            case 302:
                return "Starkes Nieseln";
            case 310:
                return "Leichter Nieselregen";
            case 311:
                return "Nieselregen";
            case 312:
                return "Starker Nieselregen";
            case 313:
                return "Leichte Schauer";
            case 314:
                return "Schauer";
            case 321:
                return "Starke Schauer";

            case 500:
                return "Leichter Regen";
            case 501:
                return "Regen";
            case 502:
                return "Starker Regen";
            case 503:
                return "Sehr Starker Regen";
            case 504:
                return "Extremer Regen";
            case 511:
                return "Gefrierender Regen";
            case 520:
                return "Leichte Schauer";
            case 521:
                return "Schauer";
            case 522:
                return "Starke Schauer";
            case 531:
                return "Schauer";

            case 600:
                return "Leichter Schneefall";
            case 601:
                return "Schnee";
            case 602:
                return "Starker Schneefall";
            case 611:
                return "Schneeregen";
            case 612:
                return "Starker Schneeregen";
            case 615:
                return "Leicher Regen und Schneefall";
            case 616:
                return "Regen und Schneefall";
            case 620:
                return "Leichte Schneeschauer";
            case 621:
                return "Schneeschauer";
            case 622:
                return "Starke Schneeschauer";

            case 701:
                return "Leicher Nebel";
            case 711:
                return "Rauch";
            case 721:
                return "Hochnebel";
            case 731:
                return "Sand oder Staub";
            case 741:
                return "Nebel";
            case 751:
                return "Sand";
            case 761:
                return "Staub";
            case 762:
                return "Vulkanasche";
            case 771:
                return "Böen";
            case 781:
                return "Tornado";

            case 800:
                return "Klar";

            case 801:
                return "Leichte Bewölkung";
            case 802:
                return "Wolken";
            case 803:
                return "Wolken";
            case 804:
                return "Bewölkt";

            case 900:
                return "Tornado";
            case 901:
                return "Tropensturm";
            case 902:
                return "Hurrikan";
            case 903:
                return "Kalt";
            case 904:
                return "Heiß";
            case 905:
                return "Windig";
            case 906:
                return "Hagel";

            case 951:
                return "Ruhig";
            case 952:
                return "Schwache Brise";
            case 953:
                return "Leichte Brise";
            case 954:
                return "Brise";
            case 955:
                return "Wind";
            case 956:
                return "Starker Wind";
            case 957:
                return "Stürmisch";
            case 958:
                return "Sturm";
            case 959:
                return "Starker Sturm";
            case 960:
                return "Orkan";
            case 961:
                return "Starker Orkan";
            case 962:
                return "Hurrikan";
        }
        return "?";
    }

    public static String translateYahoo(int code) {
        switch (code) {
            case 0: return "tornado";
            case 1: return "tropical storm";
            case 2: return "hurricane";
            case 3: return "severe thunderstorms";
            case 4: return "thunderstorms";
            case 5: return "mixed rain and snow";
            case 6: return "mixed rain and sleet";
            case 7: return "mixed snow and sleet";
            case 8: return "freezing drizzle";
            case 9: return "drizzle";
            case 10: return "freezing rain";
            case 11: return "showers";
            case 12: return "showers";
            case 13: return "snow flurries";
            case 14: return "light snow showers";
            case 15: return "blowing snow";
            case 16: return "snow";
            case 17: return "hail";
            case 18: return "sleet";
            case 19: return "dust";
            case 20: return "foggy";
            case 21: return "haze";
            case 22: return "smoky";
            case 23: return "blustery";
            case 24: return "windy";
            case 25: return "cold";
            case 26: return "cloudy";
            case 27: return "mostly cloudy (night) ";
            case 28: return "mostly cloudy (day)";
            case 29: return "partly cloudy (night)";
            case 30: return "partly cloudy (day)";
            case 31: return "clear (night)";
            case 32: return "sunny";
            case 33: return "fair (night)";
            case 34: return "fair (day)";
            case 35: return "mixed rain and hail";
            case 36: return "hot";
            case 37: return "isolated thunderstorms";
            case 38: return "scattered thunderstorms";
            case 39: return "scattered thunderstorms";
            case 40: return "scattered showers";
            case 41: return "heavy snow";
            case 42: return "scattered snow showers";
            case 43: return "heavy snow";
            case 44: return "partly cloudy";
            case 45: return "thundershowers";
            case 46: return "snow showers";
            case 47: return "isolated thundershowers";
            case 3200: return "not available";
        }
        return "?";
    }

    public class CurrentCondition implements Serializable {
        private int weatherId = -1;
        private String condition = "-";
        private String descr = "-";
        private String icon = "-";
        private float pressure = 0.0f;
        private float humidity = 0.0f;

        public int getWeatherId() {
            return weatherId;
        }

        public void setWeatherId(int weatherId) {
            this.weatherId = weatherId;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getDescr() {
            return descr;
        }

        public void setDescr(String descr) {
            this.descr = descr;
        }

        public float getPressure() {
            return pressure;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        public float getHumidity() {
            return humidity;
        }

        public void setHumidity(float humidity) {
            this.humidity = humidity;
        }
    }

    public class Temperature implements Serializable {
        private float temp = 0.0f;
        private float minTemp = 0.0f;
        private float maxTemp = 0.0f;

        public float getTemp() {
            return temp;
        }

        public void setTemp(float temp) {
            this.temp = temp;
        }

        public float getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(float minTemp) {
            this.minTemp = minTemp;
        }

        public float getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(float maxTemp) {
            this.maxTemp = maxTemp;
        }
    }

    public class Wind implements Serializable {
        private float speed = 0.0f;
        private float deg = 0.0f;

        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        public float getDeg() {
            return deg;
        }

        public void setDeg(float deg) {
            this.deg = deg;
        }
    }

    public class Rain implements Serializable {
        private String time = "-";
        private float ammount = 0.0f;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public float getAmmount() {
            return ammount;
        }

        public void setAmmount(float ammount) {
            this.ammount = ammount;
        }
    }

    public class Snow implements Serializable {
        private String time = "-";
        private float ammount = 0.0f;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public float getAmmount() {
            return ammount;
        }

        public void setAmmount(float ammount) {
            this.ammount = ammount;
        }
    }

    public class Clouds implements Serializable {
        private int perc = 0;

        public int getPerc() {
            return perc;
        }

        public void setPerc(int perc) {
            this.perc = perc;
        }
    }
}
