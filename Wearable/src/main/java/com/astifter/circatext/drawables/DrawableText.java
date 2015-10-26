package com.astifter.circatext.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatextutils.CTCs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DrawableText implements Drawable {
    private static final String TAG = "Drawable";

    private final Paint textPaint;
    private Rect drawnBounds = new Rect(0, 0, 0, 0);
    private int textAlignment = Drawable.Align.LEFT;
    private float lineHeight = 1.0f;

    private boolean hidden = false;

    private Integer textSourceName;
    private HashMap<Integer, String> textSource;
    private float defaultTextSize;
    private boolean strokeInAmbientMode;
    private boolean autoSize;
    private int backgroundColor;
    private int multiLine = 1;

    public DrawableText() {
        this.textPaint = createTextPaint(DrawingHelpers.NORMAL_TYPEFACE);
        setAlignment(Align.LEFT);
    }

    public DrawableText(int where, HashMap<Integer, String> source) {
        this.textPaint = createTextPaint(DrawingHelpers.NORMAL_TYPEFACE);
        setAlignment(Align.LEFT);
        setText(where, source);
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

    protected void setText(int where, HashMap<Integer, String> source) {
        this.textSourceName = where;
        this.textSource = source;
        if (this.textSource.get(this.textSourceName) == null)
            throw new IllegalArgumentException();
    }

    @Override
    public float getFutureHeight() {
        if (this.hidden) return 0;
        return getTextHeightForPaint(this.textPaint, this.lineHeight);
    }

    @Override
    public float getFutureWidth() {
        return getFutureWidth(getTextFromSource());
    }

    private float getFutureWidth(String text) {
        if (this.hidden) return 0;
        return this.textPaint.measureText(text);
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

        ArrayList<String> remainingText = new ArrayList<>();
        remainingText.addAll(Arrays.asList(getTextFromSource().split(" ")));
        if (this.autoSize) {
            float ts = this.getMaximumTextHeight(b);
            this.textPaint.setTextSize(ts);
        }

        canvas.save();
        canvas.clipRect(new Rect(b.left, b.top, b.right, b.top + (b.height() * multiLine)));

        int currentLine = 1;
        float lineWidth = 0;
        String lineString = "";
        switch (this.textAlignment) {
            case Align.LEFT:
                drawnBounds = new Rect(b.left, b.top, b.left, b.top);
                break;
            case Align.RIGHT:
                drawnBounds = new Rect(b.right, b.top, b.right, b.top);
                break;
            case Align.CENTER:
                drawnBounds = new Rect(b.centerX(), b.top, b.centerX(), b.top);
                break;
        }

        Paint.FontMetrics fm = this.textPaint.getFontMetrics();
        while (true) {
            if (remainingText.size() > 0) {
                String currentWord = remainingText.get(0);
                float wordWidth = this.getFutureWidth(" " + currentWord);
                if ((wordWidth + lineWidth) < b.width() || lineString.equals("")) {
                    lineWidth += wordWidth;
                    if (lineString.equals(""))
                        lineString = currentWord;
                    else
                        lineString += " " + currentWord;
                    remainingText.remove(0);
                    continue;
                }
            }

            if (currentLine == this.multiLine && remainingText.size() > 0) {
                lineString += "...";
                lineWidth += this.getFutureWidth("...");
            }

            float x = 0;
            if (textAlignment == DrawableText.Align.LEFT) {
                x = drawnBounds.left;
                if (lineWidth > drawnBounds.width())
                    drawnBounds.right = (int) (x + lineWidth);
            } else if (textAlignment == DrawableText.Align.RIGHT) {
                x = drawnBounds.right;
                if (lineWidth > drawnBounds.width())
                    drawnBounds.left = (int) (x - lineWidth);
            } else if (textAlignment == DrawableText.Align.CENTER) {
                x = drawnBounds.centerX();
                if (lineWidth > drawnBounds.width()) {
                    float inset = (b.width() - lineWidth) / 2;
                    drawnBounds.left = (int) (b.left + inset);
                    drawnBounds.right = (int) (b.right - inset);
                }
            }

            float y = drawnBounds.bottom + (-fm.ascent * lineHeight);
            drawnBounds.bottom += (int) getTextHeightForPaint(this.textPaint, this.lineHeight);

            canvas.drawText(lineString, x, y, textPaint);

            currentLine += 1;
            lineWidth = 0;
            lineString = "";
            if (remainingText.size() <= 0 || currentLine > multiLine)
                break;

//            /** In case the state was saved for clipping currentText, restore state. */
//            if (ellipsis != null) {
//                int fadeColor = backgroundColor & 0x00FFFFFF;
//
//                LinearGradient gradient =
//                        new LinearGradient(ellipsis.left, ellipsis.top, ellipsis.right, ellipsis.top,
//                                fadeColor, backgroundColor, android.graphics.Shader.TileMode.CLAMP);
//                Paint p = new Paint();
//                p.setDither(true);
//                p.setShader(gradient);
//                canvas.drawRect(ellipsis, p);
//
//                canvas.restore();
//            }
            if (CTCs.DEBUG) {
                if (this.textAlignment == DrawableText.Align.RIGHT)
                    lineWidth = -lineWidth;
                canvas.drawLine(x, y, x + lineWidth, y, this.textPaint);
                float a = this.textPaint.ascent();
                canvas.drawLine(x, y + a, x + lineWidth, y + a, this.textPaint);
                float d = this.textPaint.descent();
                canvas.drawLine(x, y + d, x + lineWidth, y + d, this.textPaint);
                canvas.drawLine(x, y + a, x, y + d, this.textPaint);

                Paint red = new Paint();
                red.setColor(Color.RED);
                canvas.drawLine(x - 10, y, x + 10, y, red);
                canvas.drawLine(x, y - 10, x, y + 10, red);
            }
        }
        canvas.restore();
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
        if (this.drawnBounds.contains(x, y))
            return this.textSourceName;
        return Touched.UNKNOWN;
    }

    public void setAlignment(int a) {
        this.textAlignment = a;
        switch (a) {
            case Drawable.Align.LEFT:
                textPaint.setTextAlign(Paint.Align.LEFT);
                break;
            case Drawable.Align.RIGHT:
                textPaint.setTextAlign(Paint.Align.RIGHT);
                break;
            case Drawable.Align.CENTER:
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

    @Override
    public int getColor() {
        return this.textPaint.getColor();
    }

    @Override
    public void setColor(int color) {
        this.textPaint.setColor(color);
    }

    public void autoSize(boolean b) {
        this.autoSize = b;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setMultiLine(int multiLine) {
        this.multiLine = multiLine;
    }
}
