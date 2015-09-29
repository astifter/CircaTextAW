package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.astifter.circatextutils.CircaTextConsts;

import java.util.HashMap;

public class DrawableText implements CircaTextDrawable {
    private static final String TAG = "CircaTextDrawable";

    private final Paint textPaint;
    private Rect drawnBounds = new Rect(0, 0, 0, 0);
    private Align textAlignment = Align.LEFT;
    private float lineHeight = 1.0f;

    private boolean hidden = false;
    private boolean isInAmbientMode = false;

    private Integer textSourceName;
    private HashMap<Integer, String> textSource;
    private String currentText = null;

    public DrawableText(int where, HashMap<Integer, String> source) {
        this.textPaint = createTextPaint(DrawingHelpers.NORMAL_TYPEFACE);
        setAlignment(Align.LEFT);
        this.textSourceName = where;
        this.textSource = source;
    }

    public static float getMaximumTextHeight(Typeface f, Rect bounds, float lineHeight) {
        Paint p = new Paint();
        p.setTypeface(f);
        p.setAntiAlias(true);

        if (bounds == null) return 0;

        float height = bounds.height();
        float lowerHeight = height * 0.99f;

        p.setTextSize(0);
        while (getHeightForPaint(p, lineHeight) < height) {
            p.setTextSize(p.getTextSize() + 1);
        }
        while (getHeightForPaint(p, lineHeight) > lowerHeight) {
            p.setTextSize(p.getTextSize() - 0.1f);
        }
        while (getHeightForPaint(p, lineHeight) < height) {
            p.setTextSize(p.getTextSize() + 0.01f);
        }
        while (getHeightForPaint(p, lineHeight) > lowerHeight) {
            p.setTextSize(p.getTextSize() - 0.001f);
        }
        return p.getTextSize();
    }

    private static float getHeightForPaint(Paint p, float lineHeight) {
        Paint.FontMetrics fm = p.getFontMetrics();
        return (-fm.ascent + fm.descent) * lineHeight;
    }

    private TextPaint createTextPaint(Typeface t) {
        TextPaint paint = new TextPaint();
        paint.setColor(Color.WHITE);
        paint.setTypeface(t);
        paint.setAntiAlias(true);
        return paint;
    }

    public void onDraw(Canvas canvas, Rect b) {
        setTextFromSource();
        if (this.currentText == "") return;

        this.drawnBounds = b;

        float targetWidth = this.getWidth();
        Paint.FontMetrics fm = this.textPaint.getFontMetrics();

        float x = 0;
        if (this.textAlignment == DrawableText.Align.LEFT) {
            x = drawnBounds.left;
            this.drawnBounds.right = this.drawnBounds.left + (int) targetWidth;
        } else if (this.textAlignment == DrawableText.Align.RIGHT) {
            x = drawnBounds.right;
        } else if (this.textAlignment == DrawableText.Align.CENTER) {
            x = (drawnBounds.left + drawnBounds.right) / 2;
        }

        float y = drawnBounds.top + (-fm.ascent * lineHeight);
        this.drawnBounds.bottom = this.drawnBounds.top + (int) getHeight();

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
        int maxWidth = b.width();
        if (targetWidth > maxWidth && this.textAlignment == Align.LEFT) {
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

    private void setTextFromSource() {
        if (this.textSource != null && this.textSource.get(this.textSourceName) != null && !hidden) {
            this.currentText = this.textSource.get(this.textSourceName);
        } else {
            this.currentText = "";
        }
    }

    public float getHeight() {
        if (this.hidden) return 0;
        return getHeightForPaint(this.textPaint, this.lineHeight);
    }

    @Override
    public float getWidth() {
        if (this.hidden) return 0;
        setTextFromSource();
        if (this.currentText == "")
            return 0;
        else
            return textPaint.measureText(currentText);
    }

    public void setTextSize(float s) {
        this.textPaint.setTextSize(s);
    }

    public void setAmbientMode(boolean inAmbientMode) {
        this.isInAmbientMode = inAmbientMode;
        this.textPaint.setAntiAlias(!inAmbientMode);
    }

    public void setAlpha(int a) {
        this.textPaint.setAlpha(a);
    }

    @Override
    public void hide() {
        this.hidden = true;
    }

    @Override
    public void show() {
        this.hidden = false;
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
}
