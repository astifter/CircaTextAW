package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.HashMap;

/**
 * Created by astifter on 22.10.15.
 */
public class StaticText extends DrawableText {
    private final HashMap<Integer, String> text;
    private Position position;

    public StaticText(int idx, String s, Rect p, int a) {
        text = new HashMap<>();
        text.put(idx, s);
        setText(idx, text);
        position = new Position(p, a);
        setAlignment(a);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        Rect r = position.percentagePosition(bounds).toRect();
        super.onDraw(canvas, r);
    }
}
