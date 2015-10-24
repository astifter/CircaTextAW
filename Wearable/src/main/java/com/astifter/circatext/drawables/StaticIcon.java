package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.*;

import com.astifter.circatext.graphicshelpers.Position;

import java.util.HashMap;

public class StaticIcon extends DrawableIcon {
    private Position position;

    public StaticIcon(int idx, android.graphics.drawable.Drawable drawable, Rect p, int scale) {
        super(idx, drawable, Align.LEFT, scale);
        position = new Position(p);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        Rect r = position.percentagePosition(bounds).toRect();
        super.onDraw(canvas, r);
    }
}
