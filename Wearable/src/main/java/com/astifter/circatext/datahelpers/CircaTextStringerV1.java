package com.astifter.circatext.datahelpers;

import java.util.ArrayList;
import java.util.Calendar;

public class CircaTextStringerV1 extends CircaTextStringerBase {
    public CircaTextStringerV1() {
        calendar = Calendar.getInstance();
        precise = false;
    }

    public CircaTextStringerV1(boolean p) {
        calendar = Calendar.getInstance();
        precise = p;
    }

    public String[] getStringFromTime(int hour, int minute) {
        if (precise)
            return getPreciseStringFromTime(hour, minute);

        ArrayList<String> returnvalue = new ArrayList<>();

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
            returnvalue.add("fünf vor");
        if (section % 3 == 2)
            returnvalue.add("fünf nach");

        // Now merge the sections into 4 blocks, use "viertel", "halb" and
        // "dreiviertel" accordingly.
        section = section / 3;
        if (section % 4 == 0)
            returnvalue.add("viertel");
        if (section % 4 == 1)
            returnvalue.add("halb");
        if (section % 4 == 2)
            returnvalue.add("dreiviertel");

        // This format is inherently 12 hour based, make sure hour is corrected and
        // printed accordingly.
        if (hour >= 12) hour -= 12;
        returnvalue.add(german_numbers[hour + 1]);

        String[] r = new String[returnvalue.size()];
        returnvalue.toArray(r);
        return r;
    }

    public String[] getPreciseStringFromTime(int hour, int minute) {
        ArrayList<String> returnvalue = new ArrayList<>();

        int qmin = minute % 15;
        if (1 <= qmin && qmin <= 7) {
            returnvalue.add(german_numbers[qmin] + " nach");
        } else if (8 <= qmin && qmin <= 14) {
            returnvalue.add(german_numbers[15 - qmin] + " vor");
        }

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

        int section = minute / 15;
        if (section % 4 == 0)
            returnvalue.add("viertel");
        if (section % 4 == 1)
            returnvalue.add("halb");
        if (section % 4 == 2)
            returnvalue.add("dreiviertel");

        // This format is inherently 12 hour based, make sure hour is corrected and
        // printed accordingly.
        if (hour >= 12) hour -= 12;
        returnvalue.add(german_numbers[hour + 1]);

        String[] r = new String[returnvalue.size()];
        returnvalue.toArray(r);
        return r;
    }
}
