package com.astifter.circatext.datahelpers;

import java.util.Calendar;

/**
 * Created by astifter on 10.09.15.
 */
public class CircaTextStringerV2 implements CircaTextStringer {
    Calendar calendar;

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

    public CircaTextStringerV2() {
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
        String returnvalue[] = {"","",""};

        // To make things easier the first 13 minutes of the new hour are
        // handled together with the previous hour. For this the minutes are
        // shifted back by 13 correcting the hour accordingly.
        minute -= 13;
        if (minute < 0) {
            minute += 60;
            hour -= 1;
        }
        if (hour < 0) {
            hour += 24;
        }

        // First divide minutes into 12 sections, then for the sections use
        // different terms.  "fünf vor", for the sections 2, 5, 8, 11 use "fünf
        int section = minute / 5;

        // Prepare string for returning, reset used and free counters.

        if (section % 6 == 1)
            returnvalue[0] = "zehn vor";
        if (section % 6 == 2)
            returnvalue[0] = "fünf vor";
        if (section % 6 == 4)
            returnvalue[0] = "fünf nach";
        if (section % 6 == 5)
            returnvalue[0] = "zehn nach";

        // Use the sections again to fetch the next part of the string.
        if (section == 0)
            returnvalue[1] = "viertel";
        if (section > 0 && section < 6)
            returnvalue[1] = "halb";
        if (section == 6)
            returnvalue[1] = "dreiviertel";
        // (section % 4 == 3) is the full hour.

        // This format is inherently 12 hour based, make sure hour is corrected and
        // printed accordingly.
        if (hour >= 12) hour -= 12;
        returnvalue[2] = german_numbers[hour+1];

        return returnvalue;
    }
}
