package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.DrawableText;
import com.astifter.circatext.graphicshelpers.Position;

import java.util.HashMap;

public class StaticText extends DrawableText {
    private Position position;

    public StaticText(int idx, String s, Rect p, int a) {
        create(idx, s, p, a);
    }

    private void create(int idx, String s, Rect p, int a) {
        HashMap<Integer, String> text;
        text = new HashMap<>();
        text.put(idx, s);
        setText(idx, text);
        position = new Position(p, a);
        setAlignment(a);
    }

    public StaticText(int idx, String s, Rect p) {
        create(idx, s, p, Align.LEFT);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        Rect r = position.percentagePosition(bounds).toRect();
        super.onDraw(canvas, r);
    }
}
