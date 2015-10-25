package com.astifter.circatextutils;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

public class CTCs {
    public static final boolean DEBUG = false;
    public static final String KEY_EXCLUDED_CALENDARS = "EXCLUDED_CALENDARS";
    public static final String KEY_WATCHFACE = "WATCHFACE";
    public static final String KEY_WATCHFACE_CONFIG = "WATCHFACE_CONFIG";

    public static final String PATH_WITH_FEATURE = "/com.astifter.circatext/config";

    public static final String REQUIRE_WEATHER_MESSAGE = "/com.astifter.circatext/require_weather";
    public static final String SEND_WEATHER_MESSAGE = "/com.astifter.circatext/send_weather";

    private static final String TAG = "CTCs";

    private static int parseColor(String colorName) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "parseColor()");

        return Color.parseColor(colorName.toLowerCase());
    }

    public static void setDefaultValuesForMissingConfigKeys(DataMap config) {
        addConfigKeyIfMissing(config, KEY_EXCLUDED_CALENDARS, "");
        addConfigKeyIfMissing(config, KEY_WATCHFACE, WatchFaces.CIRCATEXTv1.toString());
        addConfigKeyIfMissing(config, KEY_WATCHFACE_CONFIG, Config.PLAIN.toString());
    }

    private static void addConfigKeyIfMissing(DataMap config, String key, int value) {
        if (!config.containsKey(key)) {
            config.putInt(key, value);
        }
    }

    private static void addConfigKeyIfMissing(DataMap config, String key, String value) {
        if (!config.containsKey(key)) {
            config.putString(key, value);
        }
    }

    public enum WatchFaces {
        REGULAR, CIRCATEXTv1, CIRCATEXTv1ROUND
    }

    public enum Config {
        PLAIN, PEEK, TIME
    }
}
