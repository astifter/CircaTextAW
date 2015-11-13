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
    public Location location;
    public CurrentCondition currentCondition = new CurrentCondition();
    public Temperature temperature = new Temperature();
    public Wind wind = new Wind();
    public Rain rain = new Rain();
    public Snow snow = new Snow();
    public Clouds clouds = new Clouds();
    public Date time;
    public Date lastupdate;

    public class CurrentCondition implements Serializable {
        private int weatherId;
        private String condition;
        private String descr;
        private String icon;
        private float pressure;
        private float humidity;

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
        private float temp;
        private float minTemp;
        private float maxTemp;

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
        private float speed;
        private float deg;

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
        private String time;
        private float ammount;

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
        private String time;
        private float ammount;

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
        private int perc;

        public int getPerc() {
            return perc;
        }

        public void setPerc(int perc) {
            this.perc = perc;
        }
    }

    public static String translate(int weatherId) {
        switch (weatherId) {
            case 200: return "Gewitter mit Leichtem Regen";
            case 201: return "Gewitter mit Regen";
            case 202: return "Gewitter mit Starkem Regen";
            case 210: return "Leichtes Gewitter";
            case 211: return "Gewitter";
            case 212: return "Starkes Gewitter";
            case 221: return "Gewitter";
            case 230: return "Gewitter mit Leichtem Nieseln";
            case 231: return "Gewitter mit Nieseln";
            case 232: return "Gewitter mit Starkem Nieseln";

            case 300: return "Leichtes Nieseln";
            case 301: return "Nieseln";
            case 302: return "Starkes Nieseln";
            case 310: return "Leichter Nieselregen";
            case 311: return "Nieselregen";
            case 312: return "Starker Nieselregen";
            case 313: return "Leichte Schauer";
            case 314: return "Schauer";
            case 321: return "Starke Schauer";

            case 500: return "Leichter Regen";
            case 501: return "Regen";
            case 502: return "Starker Regen";
            case 503: return "Sehr Starker Regen";
            case 504: return "Extremer Regen";
            case 511: return "Gefrierender Regen";
            case 520: return "Leichte Schauer";
            case 521: return "Schauer";
            case 522: return "Starke Schauer";
            case 531: return "Schauer";

            case 600: return "Leichter Schneefall";
            case 601: return "Schnee";
            case 602: return "Starker Schneefall";
            case 611: return "Schneeregen";
            case 612: return "Starker Schneeregen";
            case 615: return "Leicher Regen und Schneefall";
            case 616: return "Regen und Schneefall";
            case 620: return "Leichte Schneeschauer";
            case 621: return "Schneeschauer";
            case 622: return "Starke Schneeschauer";

            case 701: return "Leicher Nebel";
            case 711: return "Rauch";
            case 721: return "Hochnebel";
            case 731: return "Sand oder Staub";
            case 741: return "Nebel";
            case 751: return "Sand";
            case 761: return "Staub";
            case 762: return "Vulkanasche";
            case 771: return "Böen";
            case 781: return "Tornado";

            case 800: return "Klar";

            case 801: return "Leichte Bewölkung";
            case 802: return "Wolken";
            case 803: return "Wolken";
            case 804: return "Bewölkt";

            case 900: return "Tornado";
            case 901: return "Tropensturm";
            case 902: return "Hurrikan";
            case 903: return "Kalt";
            case 904: return "Heiß";
            case 905: return "Windig";
            case 906: return "Hagel";

            case 951: return "Ruhig";
            case 952: return "Schwache Brise";
            case 953: return "Leichte Brise";
            case 954: return "Brise";
            case 955: return "Wind";
            case 956: return "Starker Wind";
            case 957: return "Stürmisch";
            case 958: return "Sturm";
            case 959: return "Starker Sturm";
            case 960: return "Orkan";
            case 961: return "Starker Orkan";
            case 962: return "Hurrikan";
        }
        return "?";
    }
}
