package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.astifter.circatext.datahelpers.CalendarHelper;

import java.util.ArrayList;

public class Schedule implements Screen {
    private final CalendarHelper.EventInfo[] meetings;
    private final ArrayList<StaticText> drawables;
    private final ArrayList<ColorRect> rects;

    final static int disection = 25;
    final static int lineHeight = 10;

    public Schedule(CalendarHelper.EventInfo[] mMeetings) {
        this.meetings = mMeetings;
        drawables = new ArrayList<>();
        rects = new ArrayList<>();
        int i = 0;
        StaticText head = new StaticText("Termine", new Rect(5,5,95,20), Drawable.Align.RIGHT);
        head.autoSize(true);
        drawables.add(head);
        for (CalendarHelper.EventInfo ei : mMeetings) {
            int top = 20 + (i*lineHeight);
            int bottom = top + lineHeight;

            StaticText date = new StaticText(ei.formatTime(), new Rect(5,top,disection-2,bottom), Drawable.Align.RIGHT);
            date.autoSize(true); date.ensureMaximumWidth(true);
            drawables.add(date);

            StaticText title = new StaticText(ei.Title, new Rect(disection+2,top,95,bottom), Drawable.Align.LEFT);
            title.autoSize(true); title.ensureMaximumWidth(true);
            drawables.add(title);

            ColorRect r = new ColorRect(new Rect(disection-1,top,disection+1,bottom), ei.Color);
            rects.add(r);
            i++;
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        for (Drawable d : drawables) {
            d.onDraw(canvas, bounds);
        }
        for (ColorRect r : rects) {
            r.onDraw(canvas, bounds);
        }
    }

    private class ColorRect {
        private final Position position;
        private final Paint p;

        public ColorRect(Rect rect, int color) {
            this.position = new Position(rect);
            this.p = new Paint();
            this.p.setColor(color);
            this.p.setAntiAlias(true);
        }

        public void onDraw(Canvas canvas, Rect bounds) {
            Position p = this.position.percentagePosition(bounds);
            canvas.drawRect(p.toRect(), this.p);
        }
    }
}
