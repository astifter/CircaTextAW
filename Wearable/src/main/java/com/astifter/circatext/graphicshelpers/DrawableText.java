package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

import com.astifter.circatextutils.CircaTextConsts;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

public class DrawableText implements CircaTextDrawable {
    private static final String TAG = "CircaTextDrawable";

    public static Typeface BOLD_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    public static Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private Rect bounds;
    private final Paint paint;
    private int alignment;
    private int color;
    private float drawnSize;
    private float defaultTextSize;

    private String text = "";
    private boolean hidden = false;
    private boolean isInAmbientMode = false;

    public DrawableText() {
        this.paint = new Paint();
    }
    public DrawableText(int c) {
        this.color = c;
        this.paint = createTextPaint(NORMAL_TYPEFACE, DrawableText.Align.LEFT);
    }
    public DrawableText(int c, int a) {
        this.color = c;
        this.paint = createTextPaint(NORMAL_TYPEFACE, a);
    }
    public DrawableText(int c, Typeface t) {
        this.color = c;
        this.paint = createTextPaint(t, DrawableText.Align.LEFT);
    }

    private TextPaint createTextPaint(Typeface t, int a) {
        TextPaint paint = new TextPaint();
        paint.setColor(this.color);
        paint.setTypeface(t);
        paint.setAntiAlias(true);
        this.alignment = a;
        switch(a & 0xF) {
            case Align.LEFT: paint.setTextAlign(Paint.Align.LEFT); break;
            case Align.RIGHT: paint.setTextAlign(Paint.Align.RIGHT); break;
            case Align.CENTER: paint.setTextAlign(Paint.Align.CENTER); break;
        }
        return paint;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void onDraw(Canvas canvas, Rect b) {
        if (this.text == "" || hidden) return;
        this.bounds = b;

        this.drawnSize = this.getWidth();
        Paint.FontMetrics fm = this.paint.getFontMetrics();

        float x = 0;
        if (this.alignment == DrawableText.Align.LEFT) {
            x = bounds.left;
            this.bounds.right = this.bounds.left + (int)this.drawnSize;
        } else if (this.alignment == DrawableText.Align.RIGHT) {
            x = bounds.right;
        } else if (this.alignment == DrawableText.Align.CENTER) {
            x = (bounds.left + bounds.right) / 2;
        }

        float y = bounds.top + -fm.ascent;
        this.bounds.bottom = this.bounds.top + (int)getHeight();

        /**
         * Some comments are in order:
         * We first measure the text to be drawn. In case the maximum width is set and the
         * text will exceed it do:
         * - Get the font metrics and measure the overflow text "..." (ellipsis).
         * - Draw the ellpsis right at the end of the allowed area (defined by x, y and
         *   maxWidth).
         * - Save the canvas and set a clipping rectangle for the text minus the width of
         *   the ellipsis.
         * - Adjust the actually used size (drawnSize) to the maxWidth.
         */
        boolean hasSavedState = false;
        int maxWidth = -1; //bounds.width();
        if (maxWidth != -1 && this.drawnSize > maxWidth) {
            float ellipsisSize = paint.measureText("...");

            canvas.drawText("...", x + maxWidth - ellipsisSize, y, paint);

            canvas.save();
            hasSavedState = true;
            canvas.clipRect(x, y + fm.ascent, x + maxWidth - ellipsisSize, y + fm.descent);

            this.drawnSize = maxWidth;
        }
        canvas.drawText(text, x, y, paint);
        /** In case the state was saved for clipping text, restore state. */
        if (hasSavedState) {
            canvas.restore();
        }
        if (CircaTextConsts.DEBUG) {
            float ds = this.drawnSize;
            if (this.alignment == DrawableText.Align.RIGHT)
                ds = -ds;
            canvas.drawLine(x, y, x + ds, y, this.paint);
            float a = this.paint.ascent();
            canvas.drawLine(x, y + a, x + ds, y + a, this.paint);
            float d = this.paint.descent();
            canvas.drawLine(x, y + d, x + ds, y + d, this.paint);
            canvas.drawLine(x, y + a, x, y + d, this.paint);
        }
    }

    public float getHeight() {
        //if (this.hidden) return 0;
        Paint.FontMetrics fm = this.paint.getFontMetrics();
        return -fm.ascent + fm.descent;
    }

    @Override
    public float getWidth() {
        //if (this.hidden) return 0;
        if (this.alignment != Align.LEFT && this.bounds != null) {
            return this.bounds.width();
        } else {
            return paint.measureText(text);
        }
    }

    public void setTextSize(float s) {
        this.paint.setTextSize(s);
    }

    public void setDefaultTextSize(float s) {
        this.defaultTextSize = s;
    }

    public void setAmbientMode(boolean inAmbientMode) {
        this.isInAmbientMode = inAmbientMode;
        this.paint.setAntiAlias(!inAmbientMode);
        if (inAmbientMode) {
            this.paint.setColor(Color.WHITE);
        } else {
            this.paint.setColor(this.color);
        }
    }

    public void setTypeface(Typeface t) {
        this.paint.setTypeface(t);
    }

    public void setColor(int c) {
        this.color = c;
        if (!this.isInAmbientMode) {
            this.paint.setColor(c);
        }
    }

    public void setAlpha(int a) {
        this.paint.setAlpha(a);
    }

    @Override
    public void hide() {
        this.hidden = true;
    }

    @Override
    public void show() {
        this.hidden = false;
    }

    public float getDefaultTextSize() {
        return this.defaultTextSize;
    }

    public class Align {
        public static final int LEFT = 0x01;
        public static final int RIGHT = 0x02;
        public static final int CENTER = 0x03;
        public static final int TOP = 0x10;
        public static final int MIDDLE = 0x20;
        public static final int BOTTOM = 0x30;
    }

    public static float getMaximumTextSize(Typeface f, String t, Rect bounds) {
        Paint p = new Paint();
        p.setTypeface(f);
        p.setAntiAlias(true);

        float width = bounds.width();
        float lowerWidth = width * 0.99f;

        p.setTextSize(0);
        while (p.measureText(t) < width) {
            p.setTextSize(p.getTextSize()+1);
        }
        while (p.measureText(t) > lowerWidth) {
            p.setTextSize(p.getTextSize()-0.1f);
        }
        while (p.measureText(t) < width) {
            p.setTextSize(p.getTextSize()+0.01f);
        }
        return p.getTextSize();
    }
}
