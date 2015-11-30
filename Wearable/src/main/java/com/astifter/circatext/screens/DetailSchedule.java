package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.datahelpers.Position;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.DrawableHelpers;
import com.astifter.circatext.drawables.DrawableText;
import com.astifter.circatext.drawables.StackHorizontal;
import com.astifter.circatext.drawables.StaticDrawable;
import com.astifter.circatext.drawables.StaticText;

import java.util.ArrayList;

public class DetailSchedule implements Screen {
    private final ArrayList<Drawable> drawables;
    private final int bgColor;

    DetailSchedule(Resources r, boolean isRound, CalendarHelper.EventInfo ei, int bgColor) {
        this.bgColor = bgColor;
        drawables = new ArrayList<>();

        ScreenHelpers.createHeadline(drawables, r, isRound, "Termindetails");
        createStaticText(ei.Title, new Rect(5, 22, 95, 37), Drawable.Align.LEFT);

        StackHorizontal sh = new StackHorizontal();
        sh.add(getDrawableText(ei.formatDate() + " "));
        sh.add(getDrawableText(ei.formatStart() + "-"));
        sh.add(getDrawableText(ei.formatEnd()));
        drawables.add(new StaticDrawable(sh, new Position(5, 39, 95, 49)));
        createStaticText(ei.Location, new Rect(5, 49, 95, 59), Drawable.Align.LEFT);

        StaticText desc = createStaticText(ei.Description, new Rect(5, 60, 95, 69), Drawable.Align.LEFT);
        desc.setMultiLine(4);
    }

    private DrawableText getDrawableText(String text) {
        DrawableText dt = new StaticText(Drawable.Touched.UNKNOWN, text);
        dt.autoSize(true);
        dt.setBackgroundColor(this.bgColor);
        return dt;
    }

    private StaticText createStaticText(String s, Rect r, int a) {
        StaticText text = new StaticText(0, s, r, a);
        text.autoSize(true);
        text.setBackgroundColor(this.bgColor);
        drawables.add(text);
        return text;
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        for (Drawable d : drawables) {
            d.onDraw(canvas, bounds);
        }
    }

    @Override
    public int getTouchedText(int x, int y) {
        return DrawableHelpers.getTouchedText(x, y, drawables);
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return DrawableHelpers.getDrawnRects(drawables);
    }
}
