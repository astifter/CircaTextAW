package com.astifter.circatextutils;

import android.util.Log;

import com.google.android.gms.wearable.DataMap;

public class CTCs {
    public static final boolean DEBUG = false;
    public static final String KEY_EXCLUDED_CALENDARS = "EXCLUDED_CALENDARS";
    public static final String KEY_WATCHFACE_CONFIG = "WATCHFACE_CONFIG";
    public static final String KEY_WATCHFACE_STRINGER = "WATCHFACE_STRINGER";

    public static final String PATH_WITH_FEATURE = "/com.astifter.circatext/config";

    public static final String REQUIRE_WEATHER_MESSAGE = "/com.astifter.circatext/require_weather";
    public static final String SEND_WEATHER_MESSAGE = "/com.astifter.circatext/send_weather";

    public static final String TAGCON = "CircaTextConnections";

    public static void setDefaultValuesForMissingConfigKeys(DataMap config) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "setDefaultValuesForMissingConfigKeys()");

        addConfigKeyIfMissing(config, KEY_EXCLUDED_CALENDARS, "");
        addConfigKeyIfMissing(config, KEY_WATCHFACE_CONFIG, Config.PLAIN.toString());
        addConfigKeyIfMissing(config, KEY_WATCHFACE_STRINGER, Stringer.CIRCA.toString());
    }

    private static void addConfigKeyIfMissing(DataMap config, String key, int value) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "addConfigKeyIfMissing(): key=" + key + ", value=" + value);
        if (!config.containsKey(key)) {
            config.putInt(key, value);
        }
    }

    private static void addConfigKeyIfMissing(DataMap config, String key, String value) {
        if (Log.isLoggable(CTCs.TAGCON, Log.DEBUG))
            Log.d(CTCs.TAGCON, "addConfigKeyIfMissing(): key=" + key + ", value=" + value);
        if (!config.containsKey(key)) {
            config.putString(key, value);
        }
    }

    public enum Config {
        PLAIN, PEEK, TIME
    }

    public enum Stringer {
        CIRCA, RELAXED, PRECISE
    }
}
