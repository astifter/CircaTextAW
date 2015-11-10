package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatextutils.Weather;

import java.util.ArrayList;

public class WeatherScreen implements Screen {

    public WeatherScreen(Resources r, Weather mWeather) {
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
    }

    @Override
    public int getTouchedText(int x, int y) {
        return Drawable.Touched.CLOSEME;
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return new ArrayList<Rect>();
    }
}
