package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.astifter.circatextutils.CircaTextConsts;

import java.util.HashMap;

public class DrawableText implements Drawable {
    private static final String TAG = "Drawable";

    private final Paint textPaint;
    private Rect drawnBounds = new Rect(0, 0, 0, 0);
    private Align textAlignment = Align.LEFT;
    private float lineHeight = 1.0f;

    private boolean hidden = false;

    private Integer textSourceName;
    private HashMap<Integer, String> textSource;
    private float defaultTextSize;
    private boolean strokeInAmbientMode;
    private boolean ensureMaxWidth;

    public DrawableText(int where, HashMap<Integer, String> source) {
        this.textPaint = createTextPaint(DrawingHelpers.NORMAL_TYPEFACE);
        setAlignment(Align.LEFT);
        this.textSourceName = where;
        this.textSource = source;
        if (this.textSource.get(this.textSourceName) == null)
            throw new IllegalArgumentException();
    }

    private static float getMaximumTextHeight(Typeface f, Rect bounds, float lineHeight) {
        Paint p = new Paint();
        p.setTypeface(f);
        p.setAntiAlias(true);

        if (bounds == null) return 0;

        float height = bounds.height();

        // First double the text size until its too big.
        p.setTextSize(1);
        while (getTextHeightForPaint(p, lineHeight) < height)
            p.setTextSize(p.getTextSize() * 2);

        // Now determine the high and low borders and define a cutoff threshold.
        float hi = p.getTextSize();
        float lo = 1;
        final float threshold = 0.5f;
        // When the borders are sufficiently close together, stop otherwise:
        // - calculate midpoint between borders
        // - set and measure text size, if:
        //   - the size is still to big, move upper border to size
        //   - else move lower border up to size
        while (hi - lo > threshold) {
            float size = (hi + lo) / 2;
            p.setTextSize(size);
            if (getTextHeightForPaint(p, lineHeight) >= height)
                hi = size;
            else
                lo = size;
        }
        return p.getTextSize();
    }

    public static float getMaximumTextWidth(Typeface f, Rect bounds, String text) {
        Paint p = new Paint();
        p.setTypeface(f);
        p.setAntiAlias(true);

        if (bounds == null) return 0;

        float width = bounds.width();

        // First double the text size until its too big.
        p.setTextSize(1);
        while (p.measureText(text) < width)
            p.setTextSize(p.getTextSize() * 2);

        // Now determine the high and low borders and define a cutoff threshold.
        float hi = p.getTextSize();
        float lo = 1;
        final float threshold = 0.5f;
        // When the borders are sufficiently close together, stop otherwise:
        // - calculate midpoint between borders
        // - set and measure text size, if:
        //   - the size is still to big, move upper border to size
        //   - else move lower border up to size
        while (hi - lo > threshold) {
            float size = (hi + lo) / 2;
            p.setTextSize(size);
            if (p.measureText(text) >= width)
                hi = size;
            else
                lo = size;
        }
        return p.getTextSize();
    }

    private static float getTextHeightForPaint(Paint p, float lineHeight) {
        Paint.FontMetrics fm = p.getFontMetrics();
        return (-fm.ascent + fm.descent) * lineHeight;
    }

    @Override
    public float getFutureHeight() {
        if (this.hidden) return 0;
        return getTextHeightForPaint(this.textPaint, this.lineHeight);
    }

    private float getFutureWidth() {
        if (this.hidden) return 0;
        return this.textPaint.measureText(getTextFromSource());
    }

    private TextPaint createTextPaint(Typeface t) {
        TextPaint paint = new TextPaint();
        paint.setColor(Color.WHITE);
        paint.setTypeface(t);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(3.0f);
        return paint;
    }

    public void onDraw(Canvas canvas, Rect b) {
        if (this.hidden) {
            return;
        }

        String currentText = getTextFromSource();
        this.drawnBounds = b;
        float maxWidth = b.width();
        float targetWidth = this.getFutureWidth();

        float x = 0;
        if (this.textAlignment == DrawableText.Align.LEFT) {
            x = drawnBounds.left;
            this.drawnBounds.right = this.drawnBounds.left + (int) targetWidth;
        } else if (this.textAlignment == DrawableText.Align.RIGHT) {
            x = drawnBounds.right;
            this.drawnBounds.left = this.drawnBounds.right - (int) targetWidth;
        } else if (this.textAlignment == DrawableText.Align.CENTER) {
            x = (drawnBounds.left + drawnBounds.right) / 2;
            if (targetWidth < this.drawnBounds.width()) {
                float inset = (this.drawnBounds.width() - targetWidth) / 2;
                this.drawnBounds.left += (int) inset;
                this.drawnBounds.right -= (int) inset;
            }
        }

        Paint.FontMetrics fm = this.textPaint.getFontMetrics();
        float y = drawnBounds.top + (-fm.ascent * lineHeight);
        this.drawnBounds.bottom = this.drawnBounds.top + (int) getTextHeightForPaint(this.textPaint, this.lineHeight);

        /**
         * Some comments are in order:
         * We first measure the currentText to be drawn. In case the maximum width is set and the
         * currentText will exceed it do:
         * - Get the font metrics and measure the overflow currentText "..." (ellipsis).
         * - Draw the ellpsis right at the end of the allowed area (defined by x, y and
         *   maxWidth).
         * - Save the canvas and set a clipping rectangle for the currentText minus the width of
         *   the ellipsis.
         * - Adjust the actually used size (drawnSize) to the maxWidth.
         */
        boolean hasSavedState = false;
        if (targetWidth > maxWidth && this.textAlignment == Align.LEFT && ensureMaxWidth) {
            float ellipsisSize = textPaint.measureText("...");
            canvas.drawText("...", x + maxWidth - ellipsisSize, y, textPaint);

            canvas.save();
            hasSavedState = true;
            canvas.clipRect(x, y + fm.ascent, x + maxWidth - ellipsisSize, y + fm.descent);

            targetWidth = maxWidth;
            this.drawnBounds.right = this.drawnBounds.left + (int) targetWidth;
        }
        canvas.drawText(currentText, x, y, textPaint);
        /** In case the state was saved for clipping currentText, restore state. */
        if (hasSavedState) {
            canvas.restore();
        }
        if (CircaTextConsts.DEBUG) {
            if (this.textAlignment == DrawableText.Align.RIGHT)
                targetWidth = -targetWidth;
            canvas.drawLine(x, y, x + targetWidth, y, this.textPaint);
            float a = this.textPaint.ascent();
            canvas.drawLine(x, y + a, x + targetWidth, y + a, this.textPaint);
            float d = this.textPaint.descent();
            canvas.drawLine(x, y + d, x + targetWidth, y + d, this.textPaint);
            canvas.drawLine(x, y + a, x, y + d, this.textPaint);

            Paint red = new Paint();
            red.setColor(Color.RED);
            canvas.drawLine(x - 10, y, x + 10, y, red);
            canvas.drawLine(x, y - 10, x, y + 10, red);
        }
    }

    private String getTextFromSource() {
        return this.textSource.get(this.textSourceName);
    }

    @Override
    public float getHeight() {
        return this.drawnBounds.height();
    }

    @Override
    public float getWidth() {
        return this.drawnBounds.width();
    }

    public void setTextSize(float s) {
        this.textPaint.setTextSize(s);
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        this.textPaint.setAntiAlias(!inAmbientMode);
        if (inAmbientMode && this.strokeInAmbientMode) {
            this.textPaint.setStyle(Paint.Style.STROKE);
        } else {
            this.textPaint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    public void setAlpha(int a) {
        this.textPaint.setAlpha(a);
    }

    @Override
    public void hide(boolean h) {
        this.hidden = h;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public int getTouchedText(int x, int y) {
        if (this.drawnBounds.contains(x, y)) {
            return this.textSourceName;
        } else {
            return -1;
        }
    }

    public void setAlignment(Align a) {
        this.textAlignment = a;
        switch (a) {
            case LEFT:
                textPaint.setTextAlign(Paint.Align.LEFT);
                break;
            case RIGHT:
                textPaint.setTextAlign(Paint.Align.RIGHT);
                break;
            case CENTER:
                textPaint.setTextAlign(Paint.Align.CENTER);
                break;
        }
    }

    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
    }

    public float getMaximumTextHeight(Rect pos) {
        return getMaximumTextHeight(this.textPaint.getTypeface(), pos, this.lineHeight);
    }

    public float getDefaultTextSize() {
        return defaultTextSize;
    }

    public void setDefaultTextSize(float defaultTextSize) {
        this.defaultTextSize = defaultTextSize;
    }

    public void strokeInAmbientMode(boolean s) {
        this.strokeInAmbientMode = s;
    }

    public void setTextFont(Typeface textFont) {
        this.textPaint.setTypeface(textFont);
    }

    public void setColor(int color) {
        this.textPaint.setColor(color);
    }

    public int getColor() {
        return this.textPaint.getColor();
    }

    public void ensureMaximumWidth(boolean m) {
        this.ensureMaxWidth = m;
    }
}
