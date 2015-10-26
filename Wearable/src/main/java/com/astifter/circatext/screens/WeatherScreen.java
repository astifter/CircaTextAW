package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatextutils.Weather;

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
}
