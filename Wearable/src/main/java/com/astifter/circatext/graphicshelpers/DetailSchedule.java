package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.datahelpers.CalendarHelper;

import java.util.ArrayList;

/**
 * Created by astifter on 23.10.15.
 */
public class DetailSchedule implements Screen {
    private final CalendarHelper.EventInfo meeting;
    private final ArrayList<StaticText> drawables;

    DetailSchedule(CalendarHelper.EventInfo ei, int bgColor) {
        drawables = new ArrayList<>();
        meeting = ei;

        StaticText start = new StaticText(0, meeting.formatStart(), new Rect(5,5,95,20), Drawable.Align.LEFT);
        drawables.add(start);
        StaticText end = new StaticText(0, meeting.formatEnd(), new Rect(5,5,95,20), Drawable.Align.RIGHT);
        drawables.add(end);
        StaticText title = new StaticText(0, meeting.Title, new Rect(5,22,95,37), Drawable.Align.LEFT);
        drawables.add(title);
        StaticText descr = new StaticText(0, meeting.Description, new Rect(5,39,95,54), Drawable.Align.LEFT);
        drawables.add(descr);

        for (DrawableText dt : drawables) {
            dt.autoSize(true); dt.ensureMaximumWidth(true);
            dt.setBackgroundColor(bgColor);
        }
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
