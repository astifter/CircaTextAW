package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.TextPaint;

import java.lang.ref.WeakReference;

public class DrawableText {
    public static final Typeface BOLD_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    public static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private final CanvasWatchFaceService.Engine engine;
    private final Paint paint;
    WeakReference<DrawableText> stack;
    StackDirection stackDirection;
    private float x;
    private float y;
    private float maxWidth = -1;
    private int color;
    private float drawnSize;

    public DrawableText(CanvasWatchFaceService.Engine engine) {
        this.engine = engine;
        this.paint = new Paint();
        this.stackDirection = new StackDirection(StackDirection.NONE);
    }
    public DrawableText(CanvasWatchFaceService.Engine engine, int c) {
        this.engine = engine;
        this.color = c;
        this.paint = createTextPaint(NORMAL_TYPEFACE, Paint.Align.LEFT);
    }
    public DrawableText(CanvasWatchFaceService.Engine engine, int c, Paint.Align a) {
        this.engine = engine;
        this.color = c;
        this.paint = createTextPaint(NORMAL_TYPEFACE, a);
    }
    public DrawableText(CanvasWatchFaceService.Engine engine, int c, Typeface t) {
        this.engine = engine;
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

    public void draw(Canvas canvas, String text) {
        float x = this.x;
        float y = this.y;
        if (this.stack != null && this.stack.get() != null) {
            switch (this.stackDirection.direction()) {
                case StackDirection.HORIZONTAL:
                    x = this.stack.get().getRight();
                    break;
                case StackDirection.BELOW:
                    y = this.stack.get().getBottom() + -this.paint.ascent();
                    break;
                case StackDirection.ABOVE:
                    y = this.stack.get().getTop() - this.paint.descent();
                    break;
            }
        }

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
        this.drawnSize = paint.measureText(text);
        if (this.maxWidth != -1 && this.drawnSize > this.maxWidth) {
            Paint.FontMetrics fm = this.paint.getFontMetrics();
            float ellipsisSize = paint.measureText("...");

            canvas.drawText("...", x + this.maxWidth - ellipsisSize, y, paint);

            canvas.save();
            hasSavedState = true;
            canvas.clipRect(x, y + fm.ascent, x + this.maxWidth - ellipsisSize, y + fm.descent);

            this.drawnSize = this.maxWidth;
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

    private float getRight() {
        if (this.stack != null && this.stack.get() != null) {
            return this.stack.get().getRight() + this.drawnSize;
        } else {
            return this.x + this.drawnSize;
        }
    }

    private float getHeigth() {
        Paint.FontMetrics fm = this.paint.getFontMetrics();
        return -fm.ascent + fm.descent;
    }

    private float getBottom() {
        if (this.stack != null && this.stack.get() != null) {
            return this.stack.get().getBottom() + this.getHeigth();
        } else {
            return this.y + this.paint.descent();
        }
    }

    private float getTop() {
        if (this.stack != null && this.stack.get() != null) {
            return this.stack.get().getTop() - this.getHeigth();
        } else {
            return this.y + this.paint.ascent();
        }
    }

    public void setCoord(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setCoord(DrawableText t, float y) {
        this.y = y;
        this.stack = new WeakReference<>(t);
        this.stackDirection = new StackDirection(StackDirection.HORIZONTAL);
    }

    public void setCoord(float x, DrawableText t, int d) {
        this.x = x;
        this.stack = new WeakReference<>(t);
        this.stackDirection = new StackDirection(d);
    }

    public void setTextSize(float s) {
        this.paint.setTextSize(s);
    }

    public void setAmbientMode(boolean inAmbientMode) {
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
        if (!engine.isInAmbientMode()) {
            this.paint.setColor(c);
        }
    }

    public void setAlpha(int a) {
        this.paint.setAlpha(a);
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public float getTextSize() {
        return this.paint.getTextSize();
    }

    public class StackDirection {
        public static final int NONE = -1;
        public static final int HORIZONTAL = 0;
        public static final int ABOVE = 1;
        public static final int BELOW = 2;

        private final int dir;

        protected StackDirection(int dir) {
            this.dir = dir;
        }

        protected int direction() {
            return dir;
        }
    }
}
