package com.astifter.circatext.datahelpers;

/**
 * Created by astifter on 10.09.15.
 */
public class CircaTextStringerTest {
    public static void main (String[] args) {
        CircaTextStringer cs = new CircaTextStringer();

        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute++) {
                String[] strings = cs.getStringFromTime(hour, minute);
                System.out.printf("--- %2d:%02d ---\n", hour, minute);
                for (int i = 0; i < 3; i++) {
                    if (strings[i] != "") {
                        System.out.println(strings[i]);
                    }
                }
            }
        }
    }
}
