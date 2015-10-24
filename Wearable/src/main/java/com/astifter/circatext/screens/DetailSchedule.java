package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.DrawableText;
import com.astifter.circatext.drawables.StaticIcon;
import com.astifter.circatext.drawables.StaticText;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;

import java.util.ArrayList;

public class DetailSchedule implements Screen {
    private final CalendarHelper.EventInfo meeting;
    private final ArrayList<Drawable> drawables;
    private final int bgColor;

    DetailSchedule(Resources r, CalendarHelper.EventInfo ei, int bgColor) {
        this.bgColor = bgColor;
        drawables = new ArrayList<>();
        meeting = ei;
        drawables.add(new StaticIcon(Drawable.Touched.CLOSEME, r.getDrawable(R.drawable.ic_arrow_back_24dp, r.newTheme()), new Rect(5,5,20,20), 20));

        createStaticText(meeting.formatStart(), new Rect(5, 5, 95, 15), Drawable.Align.LEFT);
        createStaticText(meeting.formatEnd(), new Rect(5, 5, 95, 15), Drawable.Align.RIGHT);
        createStaticText(meeting.Title, new Rect(5, 17, 95, 27), Drawable.Align.LEFT);
        createStaticText(meeting.Description, new Rect(5, 29, 95, 39), Drawable.Align.LEFT);
    }

    private void createStaticText(String s, Rect r, int a) {
        StaticText start = new StaticText(0, s, r, a);
        start.autoSize(true);
        start.ensureMaximumWidth(true);
        start.setBackgroundColor(this.bgColor);
        drawables.add(start);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        for (Drawable d : drawables) {
            d.onDraw(canvas, bounds);
        }
    }

    @Override
    public int getTouchedText(int x, int y) {
        return Drawable.Touched.CLOSEME;
    }
}
