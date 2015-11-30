package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Rect;

import com.astifter.circatext.R;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.StaticIcon;
import com.astifter.circatext.drawables.StaticText;

import java.util.ArrayList;

class ScreenHelpers {
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

    public static void createStaticTest(ArrayList<Drawable> drawables, String t, int i, Rect pos, int align, int bgColor) {
        StaticText date = new StaticText(i, t, pos, align);
        date.autoSize(true);
        date.setBackgroundColor(bgColor);
        drawables.add(date);
    }
}
