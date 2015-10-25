package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.graphicshelpers.Position;

import java.util.HashMap;

public class StaticText extends DrawableText {
    private Position position;

    public StaticText(int idx, String s) {
        create(idx, s, null, Align.LEFT);
    }

    public StaticText(int idx, String s, Rect p, int a) {
        create(idx, s, p, a);
    }

    private void create(int idx, String s, Rect p, int a) {
        HashMap<Integer, String> text;
        text = new HashMap<>();
        text.put(idx, s);
        setText(idx, text);
        if (p != null)
            position = new Position(p, a);
        setAlignment(a);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        Rect r;
        if (position != null)
            r = position.percentagePosition(bounds).toRect();
        else
            r = bounds;
        super.onDraw(canvas, r);
    }
}
