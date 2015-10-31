package com.astifter.circatext.datahelpers;

import java.util.ArrayList;
import java.util.Calendar;

public class CircaTextStringerV2 extends CircaTextStringerBase {
    public CircaTextStringerV2() {
        calendar = Calendar.getInstance();
    }

    public String[] getStringFromTime(int hour, int minute) {
        ArrayList<String> returnvalue = new ArrayList<>();

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
            returnvalue.add("zehn vor");
        if (section % 6 == 2)
            returnvalue.add("fünf vor");
        if (section % 6 == 4)
            returnvalue.add("fünf nach");
        if (section % 6 == 5)
            returnvalue.add("zehn nach");

        // Use the sections again to fetch the next part of the string.
        if (section == 0)
            returnvalue.add("viertel");
        if (section > 0 && section < 6)
            returnvalue.add("halb");
        if (section == 6)
            returnvalue.add("dreiviertel");
        // (section % 4 == 3) is the full hour.

        // This format is inherently 12 hour based, make sure hour is corrected and
        // printed accordingly.
        if (hour >= 12) hour -= 12;
        returnvalue.add(german_numbers[hour + 1]);

        String[] r = new String[returnvalue.size()];
        returnvalue.toArray(r);
        return r;
    }
}
