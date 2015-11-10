package com.astifter.circatext.graphicshelpers;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.astifter.circatext.R;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.StaticIcon;
import com.astifter.circatext.drawables.StaticText;

import java.util.ArrayList;

public class DrawingHelpers {
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

    public static void createHeadline(ArrayList<Drawable> drawables, Resources r, boolean isRound, String text) {
        if (isRound) {
            drawables.add(new StaticIcon(Drawable.Touched.CLOSEME, r.getDrawable(R.drawable.ic_arrow_back_24dp, r.newTheme()), new Rect(5, 5, 95, 20), 20, Drawable.Align.CENTER));
        } else {
            if (text != null) {
                StaticText head = new StaticText(Drawable.Touched.UNKNOWN, text, new Rect(5, 5, 95, 20), Drawable.Align.RIGHT);
                head.autoSize(true);
                drawables.add(head);
            }
            drawables.add(new StaticIcon(Drawable.Touched.CLOSEME, r.getDrawable(R.drawable.ic_arrow_back_24dp, r.newTheme()), new Rect(5, 5, 20, 20), 20));
        }
    }

    public static ArrayList<Rect> getDrawnRects(ArrayList<Drawable> drawables) {
        ArrayList<Rect> retval = new ArrayList<>();
        for (Drawable d : drawables) {
            retval.addAll(retval);
        }
        return retval;
    }
}
