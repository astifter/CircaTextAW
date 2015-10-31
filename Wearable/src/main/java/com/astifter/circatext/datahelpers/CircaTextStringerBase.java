package com.astifter.circatext.datahelpers;

import java.util.Calendar;

abstract class CircaTextStringerBase implements CircaTextStringer {
    protected static String[] german_numbers = {
            "null",
            "eins",
            "zwei",
            "drei",
            "vier",
            "fünf",
            "sechs",
            "sieben",
            "acht",
            "neun",
            "zehn",
            "elf",
            "zwölf",
            "dreizehn",
            "vierzehn",
            "fünfzehn"
    };
    protected boolean precise;
    protected Calendar calendar;

    public String[] getString() {
        long now = System.currentTimeMillis();
        calendar.setTimeInMillis(now);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return getStringFromTime(hour, minute);
    }

    public String[] getString(Calendar c) {
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return getStringFromTime(hour, minute);
    }
}
