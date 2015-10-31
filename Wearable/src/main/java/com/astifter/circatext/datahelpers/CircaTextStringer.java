package com.astifter.circatext.datahelpers;

import java.util.Calendar;

public interface CircaTextStringer {
    String[] getStringFromTime(int hour, int minute);

    String[] getString();

    String[] getString(Calendar c);
}
