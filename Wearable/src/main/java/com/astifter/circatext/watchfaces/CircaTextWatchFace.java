package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.WindowInsets;

import com.astifter.circatext.R;
import com.astifter.circatext.datahelpers.CircaTextStringer;
import com.astifter.circatext.datahelpers.CircaTextStringerV1;
import com.astifter.circatext.datahelpers.CircaTextStringerV2;
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

    CircaTextStringer cTS = new CircaTextStringerV2();

    public CircaTextWatchFace(CanvasWatchFaceService.Engine parent) {
        super(parent);
        {
            stackTop(mTF, topDrawable, myETF.FIRST_LINE, myTexts, DrawableText.Align.CENTER);
            HorizontalStack topInfo = new HorizontalStack();
            stackRight(mTF, topInfo, eTF.WEATHER_TEMP, mTexts);
            stackRight(mTF, topInfo, eTF.BATTERY, mTexts, DrawableText.Align.RIGHT);
            topDrawable.addAbove(topInfo);
            stackBottom(mTF, topDrawable, myETF.SECOND_LINE, myTexts, DrawableText.Align.CENTER);
            stackBottom(mTF, topDrawable, myETF.THIRD_LINE, myTexts, DrawableText.Align.CENTER);
            HorizontalStack date = new HorizontalStack();
            stackRight(mTF, date, eTF.DAY_OF_WEEK, mTexts);
            stackRight(mTF, date, eTF.DATE, mTexts, DrawableText.Align.RIGHT);
            topDrawable.addBelow(date);
            stackBottom(mTF, topDrawable, eTF.CALENDAR_1, mTexts);
        }
        {
            HorizontalStack hours = new HorizontalStack();
            stackRight(ambientTF, hours, eTF.HOUR, mTexts);
            stackRight(ambientTF, hours, eTF.COLON_1, mTexts);
            stackRight(ambientTF, hours, eTF.MINUTE, mTexts);

            HorizontalStack ambientDate = new HorizontalStack();
            stackRight(ambientTF, ambientDate, eTF.DAY_OF_WEEK, mTexts);
            stackRight(ambientTF, ambientDate, eTF.DATE, mTexts, DrawableText.Align.RIGHT);

            ambientDrawable.addAbove(hours);
            ambientDrawable.addBelow(ambientDate);
        }
    }

    @Override
    public void setMetrics(Resources resources, WindowInsets insets) {
        super.setMetrics(resources, insets);

        {
            int mYOffset = (int) resources.getDimension(R.dimen.digital_y_offset_circatext);
            topDrawable.setOffset(mYOffset);
            float textSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "dreiviertel", mBounds);

            for (int i = myETF.FIRST_LINE; i <= myETF.THIRD_LINE; i++) {
                mTF.get(i).setTextSize(textSize);
            }
            mTF.get(eTF.DAY_OF_WEEK).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mTF.get(eTF.DATE).setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mTF.get(eTF.CALENDAR_1).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTF.get(eTF.BATTERY).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            mTF.get(eTF.WEATHER_TEMP).setTextSize(resources.getDimension(R.dimen.digital_small_date_text_size));
            for (int i = myETF.FIRST_LINE; i <= myETF.THIRD_LINE; i++) {
                DrawableText dt = mTF.get(i);
                dt.setLineHeight(0.75f);
            }
        }
        {
            int mYOffset = (int) resources.getDimension(R.dimen.digital_y_offset);
            ambientDrawable.setOffset(mYOffset);

            float textSize = DrawableText.getMaximumTextSize(DrawableText.NORMAL_TYPEFACE, "00:00", mBounds);
            float dateTextSize = resources.getDimension(R.dimen.digital_date_text_size);

            for (DrawableText dt : ambientTF.values()) {
                dt.setTextSize(dateTextSize);
            }
            for (int i = eTF.HOUR; i <= eTF.MINUTE; i++) {
                DrawableText dt = ambientTF.get(i);
                dt.setTextSize(textSize); dt.setDefaultTextSize(textSize);
            }
        }
        if (this.ambientMode) {
            topDrawable.setAlpha(0);
            ambientDrawable.setAlpha(255);
        } else {
            topDrawable.setAlpha(255);
            ambientDrawable.setAlpha(0);
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
        if (this.ambientMode) {
            createIntAnimation(topDrawable, "alpha", 255, 0);
            createIntAnimation(ambientDrawable, "alpha", 0, 255);
        } else {
            createIntAnimation(topDrawable,     "alpha", 0, 255);
            createIntAnimation(ambientDrawable, "alpha", 255, 0);
        }
    }

    @Override
    public void startTapHighlight() {
        startTapHighlight(topDrawable);
    }

    protected void updateVisibilty() {
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        setTexts();
        String[] circaTexts = cTS.getString();
        int sourceIdx = 0, targetIdx = 0;
        while(sourceIdx < circaTexts.length) {
            if (circaTexts[sourceIdx] != "") {
                myTexts.put(myETF.FIRST_LINE + targetIdx, circaTexts[sourceIdx]);
                targetIdx++;
            }
            sourceIdx++;
        }
        for (int j = myETF.FIRST_LINE + targetIdx; j <= myETF.THIRD_LINE; j++) {
            myTexts.put(j, "");
        }

        // Draw the background.
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
        topDrawable.onDraw(canvas, mBounds);
        ambientDrawable.onDraw(canvas, mBounds);
    }
}
