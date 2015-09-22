package com.astifter.circatext.watchfaces;

import android.animation.Animator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.CircaTextService;
import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.graphicshelpers.CircaTextDrawable;
import com.astifter.circatext.graphicshelpers.DrawableText;
import com.astifter.circatext.graphicshelpers.HorizontalStack;
import com.astifter.circatext.graphicshelpers.VerticalStack;

import java.util.HashMap;

public class CircaTextWatchFace extends BaseWatchFace {
    protected class myETF {
        public static final int FIRST_LINE = eTF.SIZE;
        public static final int SECOND_LINE = FIRST_LINE + 1;
        public static final int THIRD_LINE = SECOND_LINE + 1;
        public static final int SIZE = THIRD_LINE + 1;
    }
    private final HashMap<Integer, DrawableText> mTF = new HashMap<>();
    private final HashMap<Integer, String> myTexts = new HashMap<>();
    private final HashMap<Integer, DrawableText> ambientTF = new HashMap<>();

    private final VerticalStack topDrawable = new VerticalStack();
    private final VerticalStack ambientDrawable = new VerticalStack();

    CircaTextStringer cTS = new CircaTextStringer();

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);
        {
            for (int i = 0; i < eTF.SIZE; i++) {
                mTF.put(i, new DrawableText());
                mTF.get(i).setTextSource(i, mTexts);
            }
            for (int i = myETF.FIRST_LINE; i < myETF.SIZE; i++) {
                DrawableText t = new DrawableText();
                t.setTextSource(i, myTexts);
                t.setAlignment(DrawableText.Align.CENTER);
                mTF.put(i, t);
            }

            mTF.get(eTF.BATTERY).setAlignment(DrawableText.Align.RIGHT);
            mTF.get(eTF.DATE).setAlignment(DrawableText.Align.RIGHT);
            mTF.get(eTF.WEATHER_DESC).setAlignment(DrawableText.Align.RIGHT);

            topDrawable.addAbove(mTF.get(myETF.FIRST_LINE));
            HorizontalStack topInfo = new HorizontalStack();
            topInfo.add(mTF.get(eTF.WEATHER_TEMP));
            topInfo.add(mTF.get(eTF.BATTERY));
            topDrawable.addAbove(topInfo);
            topDrawable.addBelow(mTF.get(myETF.SECOND_LINE));
            topDrawable.addBelow(mTF.get(myETF.THIRD_LINE));
            HorizontalStack date = new HorizontalStack();
            date.add(mTF.get(eTF.DAY_OF_WEEK));
            date.add(mTF.get(eTF.DATE));
            topDrawable.addBelow(date);
            topDrawable.addBelow(mTF.get(eTF.CALENDAR_1));
        }
        {
            ambientTF.put(eTF.HOUR, new DrawableText(eTF.HOUR, mTexts));
            ambientTF.put(eTF.COLON_1, new DrawableText(eTF.COLON_1, mTexts));
            ambientTF.put(eTF.MINUTE, new DrawableText(eTF.MINUTE, mTexts));
            ambientTF.put(eTF.DAY_OF_WEEK, new DrawableText(eTF.DAY_OF_WEEK, mTexts));
            ambientTF.put(eTF.DATE, new DrawableText(eTF.DATE, mTexts));

            ambientTF.get(eTF.DATE).setAlignment(DrawableText.Align.RIGHT);

            HorizontalStack hours = new HorizontalStack();
            hours.add(ambientTF.get(eTF.HOUR));
            hours.add(ambientTF.get(eTF.COLON_1));
            hours.add(ambientTF.get(eTF.MINUTE));
            ambientDrawable.addAbove(hours);
            HorizontalStack ambientDate = new HorizontalStack();
            ambientDate.add(ambientTF.get(eTF.DAY_OF_WEEK));
            ambientDate.add(ambientTF.get(eTF.DATE));
            ambientDrawable.addBelow(ambientDate);
        }
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);

        int mYOffset = (int) resources.getDimension(R.dimen.digital_y_offset);
        topDrawable.setOffset(mYOffset);
        ambientDrawable.setOffset(mYOffset);

        {
            float textSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "dreiviertel", mBounds);

            for (int i = myETF.FIRST_LINE; i <= myETF.THIRD_LINE; i++) {
                mTF.get(i).setTextSize(textSize);
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
        {
            float textSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "00:00", mBounds);
            float dateTextSize = resources.getDimension(R.dimen.digital_date_text_size);

            for (DrawableText dt : ambientTF.values()) {
                dt.setTextSize(dateTextSize);
            }
            ambientTF.get(eTF.HOUR).setTextSize(textSize);
            ambientTF.get(eTF.COLON_1).setTextSize(textSize);
            ambientTF.get(eTF.MINUTE).setTextSize(textSize);
        }
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {
        super.setAmbientMode(inAmbientMode);

        if (mLowBitAmbient) {
            for (Integer i : mTF.keySet()) {
                mTF.get(i).setAmbientMode(inAmbientMode);
            }
        }
        if (inAmbientMode) {
            createIntAnimation(topDrawable,     "alpha", 255, 0);
            createIntAnimation(ambientDrawable, "alpha", 0, 255);
        } else {
            createIntAnimation(topDrawable,     "alpha", 0, 255);
            createIntAnimation(ambientDrawable, "alpha", 255, 0);
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
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        setTexts();
        String[] circaTexts = cTS.getString();
        myTexts.put(myETF.FIRST_LINE, circaTexts[0]);
        myTexts.put(myETF.SECOND_LINE, circaTexts[1]);
        myTexts.put(myETF.THIRD_LINE, circaTexts[2]);

        // Draw the background.
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
        topDrawable.onDraw(canvas, mBounds);
        ambientDrawable.onDraw(canvas, mBounds);
    }
}
