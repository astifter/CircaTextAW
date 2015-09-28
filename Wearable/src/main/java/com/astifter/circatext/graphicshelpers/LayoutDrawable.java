package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.HashMap;

/**
 * Created by astifter on 28.09.15.
 */
public class LayoutDrawable implements CircaTextDrawable {
    @Override
    public void onDraw(Canvas canvas, Rect bounds) {

    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public void setAmbientMode(boolean inAmbientMode) {

    }

    @Override
    public void setAlpha(int a) {

    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }

    public DrawableText addText(HashMap<Integer, String> mTexts, int hour) {
        // TODO
        return null;
    }
}
