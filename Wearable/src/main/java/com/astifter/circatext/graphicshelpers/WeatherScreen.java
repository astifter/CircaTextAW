package com.astifter.circatext.graphicshelpers;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatextutils.Weather;

/**
 * Created by astifter on 22.10.15.
 */
public class WeatherScreen implements Screen {
    private final Weather weather;

    public WeatherScreen(Weather mWeather) {
        this.weather = mWeather;
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {

    }
}