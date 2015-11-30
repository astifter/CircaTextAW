package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.datahelpers.Position;

public class StaticIcon extends DrawableIcon {
    private Position position;

    public StaticIcon(int idx, android.graphics.drawable.Drawable drawable, Rect p, int scale) {
        super(idx, drawable, Align.LEFT, scale);
        position = new Position(p);
    }

    public StaticIcon(int idx, android.graphics.drawable.Drawable drawable, Rect p, int scale, int align) {
        super(idx, drawable, align, scale);
        position = new Position(p, align);
        this.setAlignment(align);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        Rect r = position.percentagePosition(bounds).toRect();
        super.onDraw(canvas, r);
    }
}
