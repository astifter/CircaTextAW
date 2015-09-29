package com.astifter.circatext.datahelpers;

import java.util.Calendar;

public class CircaTextStringerV1 implements CircaTextStringer {
    private static String german_numbers[] = {
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
            "zwölf"
    };
    Calendar calendar;

    public CircaTextStringerV1() {
        calendar = Calendar.getInstance();
    }

    public String[] getString() {
        long now = System.currentTimeMillis();
        calendar.setTimeInMillis(now);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return getStringFromTime(hour, minute);
    }

    public String[] getStringFromTime(int hour, int minute) {
        String returnvalue[] = {"", "", ""};

        // To make things easier the first eight minutes of the new hour are
        // handled together with the previous hour. For this the minutes are
        // shifted back by 8 correcting the hour accordingly.
        minute -= 8;
        if (minute < 0) {
            minute += 60;
            hour -= 1;
        }
        if (hour < 0) {
            hour += 24;
        }

        // First divide minutes into 12 sections, for the sections 0, 3, 6, 9 use
        // "fünf vor", for the sections 2, 5, 8, 11 use "fünf nach".
        int section = minute / 5;
        if (section % 3 == 0)
            returnvalue[0] = "fünf vor";
        if (section % 3 == 2)
            returnvalue[0] = "fünf nach";

        // Now merge the sections into 4 blocks, use "viertel", "halb" and
        // "dreiviertel" accordingly.
        section = section / 3;
        if (section % 4 == 0)
            returnvalue[1] = "viertel";
        if (section % 4 == 1)
            returnvalue[1] = "halb";
        if (section % 4 == 2)
            returnvalue[1] = "dreiviertel";

        // This format is inherently 12 hour based, make sure hour is corrected and
        // printed accordingly.
        if (hour >= 12) hour -= 12;
        returnvalue[2] = german_numbers[hour + 1];

        return returnvalue;

    }
}
