package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

import java.lang.ref.WeakReference;

public class DrawableText implements CircaTextDrawable {
    public static final Typeface BOLD_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    public static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private Rect bounds;
    private final Paint paint;
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
        this.paint = createTextPaint(NORMAL_TYPEFACE, Paint.Align.LEFT);
    }
    public DrawableText(int c, Paint.Align a) {
        this.color = c;
        this.paint = createTextPaint(NORMAL_TYPEFACE, a);
    }
    public DrawableText(int c, Typeface t) {
        this.color = c;
        this.paint = createTextPaint(t, Paint.Align.LEFT);
    }

    private TextPaint createTextPaint(Typeface t, Paint.Align a) {
        TextPaint paint = new TextPaint();
        paint.setColor(this.color);
        paint.setTypeface(t);
        paint.setAntiAlias(true);
        paint.setTextAlign(a);
        return paint;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void onDraw(Canvas canvas, Rect bounds) {
        if (this.text == "" || hidden) return;
        this.bounds = bounds;

        this.drawnSize = this.getWidth();
        Paint.FontMetrics fm = this.paint.getFontMetrics();

        float x = 0;
        Paint.Align a = this.paint.getTextAlign();
        if (a == Paint.Align.LEFT)
            x = bounds.left;
        else if (a == Paint.Align.RIGHT)
            x = bounds.right;
        else if (a == Paint.Align.CENTER)
            x = (bounds.left + bounds.right) / 2;
        float y = bounds.top + -fm.ascent;
        int maxWidth = -1; //bounds.width();

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
        //{
        //    float ds = this.drawnSize;
        //    if (this.paint.getTextAlign() == Paint.Align.RIGHT)
        //        ds = -ds;
        //    canvas.drawLine(x, y, x + ds, y, this.paint);
        //    float a = this.paint.ascent();
        //    canvas.drawLine(x, y + a, x + ds, y + a, this.paint);
        //    float d = this.paint.descent();
        //    canvas.drawLine(x, y + d, x + ds, y + d, this.paint);
        //    canvas.drawLine(x, y + a, x, y + d, this.paint);
        //}
    }

    public float getHeight() {
        Paint.FontMetrics fm = this.paint.getFontMetrics();
        return -fm.ascent + fm.descent;
    }

    @Override
    public float getWidth() {
        return paint.measureText(text);
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
}
