package com.astifter.circatext.drawables;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.astifter.circatext.R;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.StaticIcon;
import com.astifter.circatext.drawables.StaticText;

import java.util.ArrayList;

public class DrawableHelpers {
    public static Typeface NORMAL_TYPEFACE = Typeface.create((String) null, Typeface.NORMAL);
    public static Typeface BOLD_TYPEFACE = Typeface.create((String) null, Typeface.BOLD);

    public static int getTouchedText(int x, int y, ArrayList<Drawable> drawables) {
        for (Drawable d : drawables) {
            int i = d.getTouchedText(x, y);
            if (i != Drawable.Touched.UNKNOWN)
                return i;
        }
        return Drawable.Touched.UNKNOWN;
    }

    public static ArrayList<Rect> getDrawnRects(ArrayList<Drawable> drawables) {
        ArrayList<Rect> retval = new ArrayList<>();
        for (Drawable d : drawables) {
            retval.addAll(retval);
        }
        return retval;
    }

}
