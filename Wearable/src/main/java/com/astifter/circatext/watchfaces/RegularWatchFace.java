package com.astifter.circatext.watchfaces;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.R;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatext.graphicshelpers.HorizontalStack;
import com.astifter.circatext.graphicshelpers.VerticalStack;

import java.util.ArrayList;
import java.util.HashMap;

public class RegularWatchFace extends BaseWatchFace {
    private final HashMap<Integer, DrawableText> mTF = new HashMap<>();
    private final ArrayList<DrawableText> mTFFading = new ArrayList<>();
    private final ArrayList<DrawableText> mTFAnimated = new ArrayList<>();

    private final VerticalStack topDrawable = new VerticalStack();
    private float textScaleFactor = 1.0f;

    public RegularWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);

        for (int i = 0; i < eTF.SIZE; i++) {
            mTF.put(i, new DrawableText());
            mTF.get(i).setTextSource(i, mTexts);
        }

        mTF.get(eTF.BATTERY).setAlignment(DrawableText.Align.RIGHT);
        mTF.get(eTF.DATE).setAlignment(DrawableText.Align.RIGHT);
        mTF.get(eTF.WEATHER_DESC).setAlignment(DrawableText.Align.RIGHT);

        HorizontalStack hours = new HorizontalStack();
        hours.add(mTF.get(eTF.HOUR));
        hours.add(mTF.get(eTF.COLON_1));
        hours.add(mTF.get(eTF.MINUTE));
        hours.add(mTF.get(eTF.COLON_2));
        hours.add(mTF.get(eTF.SECOND));
        topDrawable.addAbove(hours);
        topDrawable.addAbove(mTF.get(eTF.BATTERY));
        HorizontalStack date = new HorizontalStack();
        date.add(mTF.get(eTF.DAY_OF_WEEK));
        date.add(mTF.get(eTF.DATE));
        topDrawable.addBelow(date);
        topDrawable.addBelow(mTF.get(eTF.CALENDAR_1));
        topDrawable.addBelow(mTF.get(eTF.CALENDAR_2));
        HorizontalStack weather = new HorizontalStack();
        weather.add(mTF.get(eTF.WEATHER_TEMP));
        weather.add(mTF.get(eTF.WEATHER_AGE));
        weather.add(mTF.get(eTF.WEATHER_DESC));
        topDrawable.addBelow(weather);

        mTFFading.add(mTF.get(eTF.CALENDAR_1));
        mTFFading.add(mTF.get(eTF.CALENDAR_2));
        mTFFading.add(mTF.get(eTF.COLON_2));
        mTFFading.add(mTF.get(eTF.SECOND));
        mTFFading.add(mTF.get(eTF.BATTERY));
        mTFFading.add(mTF.get(eTF.WEATHER_TEMP));
        mTFFading.add(mTF.get(eTF.WEATHER_AGE));
        mTFFading.add(mTF.get(eTF.WEATHER_DESC));

        mTFAnimated.add(mTF.get(eTF.HOUR));
        mTFAnimated.add(mTF.get(eTF.COLON_1));
        mTFAnimated.add(mTF.get(eTF.MINUTE));
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);

        int mYOffset = (int)resources.getDimension(R.dimen.digital_y_offset);
        topDrawable.setOffset(mYOffset);

        float textSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "00:00:00", mBounds);
        float biggerTextSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "00:00", mBounds);
        textScaleFactor = biggerTextSize / textSize;

        for (Integer i : mTF.keySet()) {
            DrawableText t = mTF.get(i);
            t.setTextSize(textSize);
            t.setDefaultTextSize(textSize);
        }
        if (this.ambientMode) {
            for (DrawableText t : mTFAnimated) {
                t.setTextSize(textSize*textScaleFactor);
            }
        }
        mTF.get(eTF.DAY_OF_WEEK).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
        mTF.get(eTF.DATE).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
        mTF.get(eTF.CALENDAR_1).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.CALENDAR_2).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.BATTERY).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.WEATHER_TEMP).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
        mTF.get(eTF.WEATHER_AGE).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size)/1.5f);
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

    @Override
    public void startTapHighlight() {
        Animator.AnimatorListener listener = new ReverseListener();
        startAnimation(topDrawable, "alpha", 255, 0, 100, listener);
    }

    private class ReverseListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            startAnimation(topDrawable, "alpha", 0, 255, 100, null);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    protected void updateVisibilty() {
        for(Integer i : mTF.keySet()) {
            mTF.get(i).hide();
        }

        mTF.get(eTF.HOUR).show();
        mTF.get(eTF.COLON_1).show();
        mTF.get(eTF.MINUTE).show();

        // draw the rest only when not in mute mode
        if (mMute) return;

        if (this.peekCardPosition.isEmpty()) {
            mTF.get(eTF.DAY_OF_WEEK).show();
            mTF.get(eTF.DATE).show();
        }

        // draw the rest only when not in ambient mode
        if(this.ambientMode) return;

        mTF.get(eTF.COLON_2).show();
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
        topDrawable.onDraw(canvas, mBounds);
    }
}
