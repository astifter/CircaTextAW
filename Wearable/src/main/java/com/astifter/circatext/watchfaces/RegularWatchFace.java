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

        HorizontalStack hours = new HorizontalStack();
        stackRight(mTF, hours, eTF.HOUR, mTexts);
        stackRight(mTF, hours, eTF.COLON_1, mTexts);
        stackRight(mTF, hours, eTF.MINUTE, mTexts);
        stackRight(mTF, hours, eTF.COLON_2, mTexts);
        stackRight(mTF, hours, eTF.SECOND, mTexts);
        topDrawable.addAbove(hours);
        stackTop(mTF, topDrawable, eTF.BATTERY, mTexts, DrawableText.Align.RIGHT);
        HorizontalStack date = new HorizontalStack();
        stackRight(mTF, date, eTF.DAY_OF_WEEK, mTexts);
        stackRight(mTF, date, eTF.DATE, mTexts, DrawableText.Align.RIGHT);
        topDrawable.addBelow(date);
        stackBottom(mTF, topDrawable, eTF.CALENDAR_1, mTexts);
        stackBottom(mTF, topDrawable, eTF.CALENDAR_2, mTexts);
        HorizontalStack weather = new HorizontalStack();
        stackRight(mTF, weather, eTF.WEATHER_TEMP, mTexts);
        stackRight(mTF, weather, eTF.WEATHER_AGE, mTexts);
        stackRight(mTF, weather, eTF.WEATHER_DESC, mTexts, DrawableText.Align.RIGHT);
        topDrawable.addBelow(weather);

        int fadeList[] = {eTF.CALENDAR_1, eTF.CALENDAR_2, eTF.COLON_2, eTF.SECOND,
                          eTF.BATTERY, eTF.WEATHER_TEMP, eTF.WEATHER_AGE, eTF.WEATHER_DESC};
        addToUIList(mTF, mTFFading, fadeList);

        int animList[] = {eTF.HOUR, eTF.COLON_1, eTF.MINUTE};
        addToUIList(mTF, mTFAnimated, animList);
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
        startTapHighlight(topDrawable);
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
