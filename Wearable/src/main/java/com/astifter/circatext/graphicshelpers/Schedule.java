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
    private final int bgColor;
    private DetailSchedule detailedScreen;

    public Schedule(CalendarHelper.EventInfo[] mMeetings, int bgColor) {
        this.bgColor = bgColor;
        this.meetings = mMeetings;
        drawables = new ArrayList<>();
        rects = new ArrayList<>();
        int i = 0;
        StaticText head = new StaticText(-1, "Termine", new Rect(5,5,95,20), Drawable.Align.RIGHT);
        head.autoSize(true);
        drawables.add(head);
        for (CalendarHelper.EventInfo ei : mMeetings) {
            int top = 20 + (i*(lineHeight*12/10));
            int bottom = top + lineHeight;
            if (bottom > 95) {
                break;
            }

            StaticText date = new StaticText(i, ei.formatStart(), new Rect(5,top,disection-2,bottom), Drawable.Align.RIGHT);
            drawables.add(date);

            StaticText title = new StaticText(i, ei.Title, new Rect(disection+2,top,95,bottom), Drawable.Align.LEFT);
            drawables.add(title);

            ColorRect r = new ColorRect(new Rect(disection-1,top,disection+1,bottom), ei.Color);
            rects.add(r);
            i++;
        }
        for (DrawableText dt : drawables) {
            dt.autoSize(true); dt.ensureMaximumWidth(true);
            dt.setBackgroundColor(bgColor);
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        if (detailedScreen != null) {
            detailedScreen.onDraw(canvas, bounds);
            return;
        }
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

    @Override
    public int getTouchedText(int x, int y) {
        if (detailedScreen != null) {
            detailedScreen = null;
            return 0;
        }
        for (DrawableText dt : drawables) {
            int idx = dt.getTouchedText(x, y);
            if (idx >= 0) {
                detailedScreen = new DetailSchedule(meetings[idx], bgColor);
                return idx;
            }
        }
        return -1;
    }
}
