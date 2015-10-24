package com.astifter.circatext.graphicshelpers;

import android.graphics.Typeface;

import com.astifter.circatext.drawables.Drawable;

import java.util.ArrayList;

public class DrawingHelpers {
    public static Typeface NORMAL_TYPEFACE = Typeface.create((String) null, Typeface.NORMAL);
    public static Typeface BOLD_TYPEFACE = Typeface.create((String) null, Typeface.BOLD);

    public static int getTouchedText(int x, int y, ArrayList<Drawable> drawables) {
        for (Drawable d : drawables) {
            int i = d.getTouchedText(x, y);
            if (i >= Drawable.Touched.FIRST)
                return i;
        }
        return Drawable.Touched.UNKNOWN;
    }
}
