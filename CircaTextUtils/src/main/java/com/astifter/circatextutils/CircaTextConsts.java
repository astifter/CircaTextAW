package com.astifter.circatextutils;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

public class CircaTextConsts {
    private static final String TAG = "CircaTextConsts";

    public static final boolean DEBUG = false;

    public static final String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    public static final String KEY_EXCLUDED_CALENDARS = "EXCLUDED_CALENDARS";
    public static final String PATH_WITH_FEATURE = "/com.astifter.circatext/config";
    public static final String REQUIRE_WEATHER_MESSAGE = "/com.astifter.circatext/require_weather";
    public static final String SEND_WEATHER_MESSAGE = "/com.astifter.circatext/send_weather";

    private static final String COLOR_NAME_DEFAULT_AND_AMBIENT_BACKGROUND = "Black";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_BACKGROUND);

    private static int parseColor(String colorName) {
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "parseColor()");

        return Color.parseColor(colorName.toLowerCase());
    }

    public static void setDefaultValuesForMissingConfigKeys(DataMap config) {
        addConfigKeyIfMissing(config, KEY_BACKGROUND_COLOR, COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
        addConfigKeyIfMissing(config, KEY_EXCLUDED_CALENDARS, "");
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
}
