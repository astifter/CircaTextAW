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

    DetailSchedule(CalendarHelper.EventInfo ei) {
        drawables = new ArrayList<>();
        meeting = ei;
        StaticText hour = new StaticText(0, meeting.formatTime(), new Rect(5,5,95,20), Drawable.Align.LEFT);
        hour.autoSize(true); hour.ensureMaximumWidth(true);
        drawables.add(hour);

        StaticText title = new StaticText(0, meeting.Title, new Rect(5,22,95,95), Drawable.Align.LEFT);
        title.setTextSize(18);
        drawables.add(title);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        for (Drawable d : drawables) {
            d.onDraw(canvas, bounds);
        }
    }

    @Override
    public int getTouchedText(int x, int y) {
        return -1;
    }
}
