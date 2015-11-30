package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.datahelpers.Position;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.DrawableHelpers;

import java.util.ArrayList;

public class Schedule implements Screen {
    final static int disection = 27;
    final static int lineHeight = 10;
    private final CalendarHelper.EventInfo[] meetings;
    private final ArrayList<Drawable> drawables;
    private final ArrayList<ColorRect> rects;
    private final int bgColor;
    private final Resources resources;
    private final boolean isRound;
    private DetailSchedule detailedScreen;

    public Schedule(Resources r, boolean isRound, CalendarHelper.EventInfo[] mMeetings, int bgColor) {
        this.resources = r;
        this.bgColor = bgColor;
        this.meetings = mMeetings;
        drawables = new ArrayList<>();
        rects = new ArrayList<>();

        this.isRound = isRound;
        ScreenHelpers.createHeadline(drawables, r, isRound, "Termine");

        int i = 0;
        for (CalendarHelper.EventInfo ei : mMeetings) {
            int top = 20 + (i * (lineHeight * 12 / 10));
            int bottom = top + lineHeight;
            if (bottom > 95) {
                break;
            }

            ScreenHelpers.createStaticTest(this.drawables, ei.formatStart(), i, new Rect(5, top, disection - 2, bottom), Drawable.Align.RIGHT, bgColor);
            ScreenHelpers.createStaticTest(this.drawables, ei.Title, i, new Rect(disection + 2, top, 95, bottom), Drawable.Align.LEFT, bgColor);

            ColorRect cr = new ColorRect(new Rect(disection - 1, top, disection + 1, bottom), ei.Color);
            rects.add(cr);
            i++;
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

    @Override
    public int getTouchedText(int x, int y) {
        if (detailedScreen != null) {
            int idx = detailedScreen.getTouchedText(x, y);
            if (idx == Drawable.Touched.CLOSEME)
                detailedScreen = null;
            return Drawable.Touched.FINISHED;
        } else {
            int idx = DrawableHelpers.getTouchedText(x, y, drawables);
            if (idx >= Drawable.Touched.FIRST) {
                detailedScreen = new DetailSchedule(this.resources, this.isRound, meetings[idx], bgColor);
                return Drawable.Touched.FINISHED;
            } else
                return idx;
        }
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return DrawableHelpers.getDrawnRects(drawables);
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
