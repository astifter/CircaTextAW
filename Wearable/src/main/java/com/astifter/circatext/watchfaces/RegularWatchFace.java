package com.astifter.circatext.watchfaces;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.R;
import com.astifter.circatext.graphicshelpers.Drawable;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatext.graphicshelpers.StackHorizontal;
import com.astifter.circatext.graphicshelpers.StackVertical;

import java.util.ArrayList;
import java.util.HashMap;

public class RegularWatchFace extends BaseWatchFace {
    private final HashMap<Integer, DrawableText> mTF = new HashMap<>();
    private final ArrayList<DrawableText> mTFFading = new ArrayList<>();
    private final ArrayList<DrawableText> mTFAnimated = new ArrayList<>();

    private final StackVertical topDrawable = new StackVertical();
    private float textScaleFactor = 1.0f;
    private Rect topDrawableBounds;

    public RegularWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);

        setTexts();

        StackHorizontal hours = new StackHorizontal();
        stackRight(mTF, hours, eTF.HOUR, mTexts);
        stackRight(mTF, hours, eTF.SECOND, mTexts);
        topDrawable.addAbove(hours);
        stackTop(mTF, topDrawable, eTF.BATTERY, mTexts, DrawableText.Align.RIGHT);
        StackHorizontal date = new StackHorizontal();
        stackRight(mTF, date, eTF.DAY_OF_WEEK, mTexts);
        stackRight(mTF, date, eTF.DATE, mTexts, DrawableText.Align.RIGHT);
        topDrawable.addBelow(date);
        stackBottom(mTF, topDrawable, eTF.CALENDAR_1, mTexts);
        stackBottom(mTF, topDrawable, eTF.CALENDAR_2, mTexts);
        StackHorizontal weather = new StackHorizontal();
        stackRight(mTF, weather, eTF.WEATHER_TEMP, mTexts);
        stackRight(mTF, weather, eTF.WEATHER_AGE, mTexts);
        stackRight(mTF, weather, eTF.WEATHER_DESC, mTexts, DrawableText.Align.RIGHT);
        topDrawable.addBelow(weather);

        int fadeList[] = {eTF.CALENDAR_1, eTF.CALENDAR_2, eTF.SECOND,
                          eTF.BATTERY, eTF.WEATHER_TEMP, eTF.WEATHER_AGE, eTF.WEATHER_DESC};
        addToUIList(mTF, mTFFading, fadeList);

        int animList[] = {eTF.HOUR};
        addToUIList(mTF, mTFAnimated, animList);
    }

    private void addToUIList(HashMap<Integer, DrawableText> c, ArrayList<DrawableText> l, int[] list) {
        for (int i : list) {
            l.add(c.get(i));
        }
    }

    private void stackBottom(HashMap<Integer, DrawableText> c, StackVertical s, int i, HashMap<Integer, String> t) {
        stackBottom(c, s, i, t, Drawable.Align.LEFT);
    }

    private void stackTop(HashMap<Integer, DrawableText> c, StackVertical s, int i, HashMap<Integer, String> t, Drawable.Align a) {
        DrawableText dt = new DrawableText(i, t);
        dt.setAlignment(a);
        s.addAbove(dt);
        c.put(i, dt);
    }

    private void stackBottom(HashMap<Integer, DrawableText> c, StackVertical s, int i, HashMap<Integer, String> t, Drawable.Align a) {
        DrawableText dt = new DrawableText(i, t);
        dt.setAlignment(a);
        s.addBelow(dt);
        c.put(i, dt);
    }

    private void stackRight(HashMap<Integer, DrawableText> c, StackHorizontal s, int i, HashMap<Integer, String> t, Drawable.Align a) {
        DrawableText dt = new DrawableText(i, t);
        dt.setAlignment(a);
        s.add(dt);
        c.put(i, dt);
    }

    private void stackRight(HashMap<Integer, DrawableText> c, StackHorizontal s, int i, HashMap<Integer, String> t) {
        stackRight(c, s, i, t, Drawable.Align.LEFT);
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);

        int left = (int) resources.getDimension(R.dimen.digital_x_offset);
        this.topDrawableBounds =
                new Rect(left, left,
                         this.mBounds.width() - left, this.mBounds.height() - left);

        int mYOffset = (int) resources.getDimension(R.dimen.digital_y_offset);
        topDrawable.setOffset(mYOffset);

        float textSize = DrawableText.getMaximumTextWidth(DrawingHelpers.NORMAL_TYPEFACE, this.topDrawableBounds, "00:00:00");
        float biggerTextSize = DrawableText.getMaximumTextWidth(DrawingHelpers.NORMAL_TYPEFACE, this.topDrawableBounds, "00:00");
        textScaleFactor = biggerTextSize / textSize;

        for (Integer i : mTF.keySet()) {
            DrawableText t = mTF.get(i);
            t.setTextSize(textSize);
            t.setDefaultTextSize(textSize);
        }
        if (this.ambientMode) {
            for (DrawableText t : mTFAnimated) {
                t.setTextSize(textSize * textScaleFactor);
            }
        }
        mTF.get(eTF.DAY_OF_WEEK).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
        mTF.get(eTF.DATE).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
        mTF.get(eTF.CALENDAR_1).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.CALENDAR_2).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.BATTERY).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.WEATHER_TEMP).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.WEATHER_AGE).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size) / 1.5f);
        mTF.get(eTF.WEATHER_DESC).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        super.setAmbientMode(inAmbientMode);

        if (mLowBitAmbient) {
            for (Integer i : mTF.keySet()) {
                mTF.get(i).setAmbientMode(inAmbientMode);
            }
        }
        if (!inAmbientMode) {
            for (DrawableText dt : mTFFading) {
                createIntAnimation(dt, "alpha", 0, 255);
            }
            for (DrawableText dt : mTFAnimated) {
                createTextSizeAnimation(dt, dt.getDefaultTextSize() * textScaleFactor, dt.getDefaultTextSize());
            }
        } else {
            for (DrawableText dt : mTFAnimated) {
                createTextSizeAnimation(dt, dt.getDefaultTextSize(), dt.getDefaultTextSize() * textScaleFactor);
            }
        }
    }

    private void createIntAnimation(Drawable t, String attribute, int start, int stop) {
        ValueAnimator anim = ObjectAnimator.ofInt(t, attribute, start, stop);
        startAnimation(anim);
    }

    private void createTextSizeAnimation(Drawable t, float from, float to) {
        createFloatAnimation(t, "textSize", from, to);
    }

    private void createFloatAnimation(Drawable t, String attribute, float start, float stop) {
        ValueAnimator anim = ObjectAnimator.ofFloat(t, attribute, start, stop);
        startAnimation(anim);
    }

    private void startAnimation(ValueAnimator anim) {
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        anim.start();
    }

    @Override
    public void startTapHighlight() {
        startTapHighlight(topDrawable);
    }

    protected void updateVisibilty() {
        for (Integer i : mTF.keySet()) {
            mTF.get(i).hide();
        }

        mTF.get(eTF.HOUR).show();

        // draw the rest only when not in mute mode
        if (mMute) return;

        if (this.peekCardPosition.isEmpty()) {
            mTF.get(eTF.DAY_OF_WEEK).show();
            mTF.get(eTF.DATE).show();
        }

        // draw the rest only when not in ambient mode
        if (this.ambientMode) return;

        mTF.get(eTF.SECOND).show();
        mTF.get(eTF.BATTERY).show();

        // if peek card is shown, exit
        if (this.peekCardPosition.isEmpty()) {
            mTF.get(eTF.CALENDAR_1).show();
            mTF.get(eTF.CALENDAR_2).show();

            if (haveWeather()) {
                mTF.get(eTF.WEATHER_TEMP).show();
                mTF.get(eTF.WEATHER_AGE).show();
                mTF.get(eTF.WEATHER_DESC).show();
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        setTexts();

        // Draw the background.
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
        topDrawable.onDraw(canvas, topDrawableBounds);
    }
}
