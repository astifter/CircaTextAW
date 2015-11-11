package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatextutils.Weather;

import java.util.ArrayList;

public class WeatherScreen implements Screen {
    private final Weather weather;
    ArrayList<Drawable> drawables = new ArrayList<>();

    public WeatherScreen(Resources r, boolean isRound, Weather mWeather, int bgColor) {
        this.weather = mWeather;
        DrawingHelpers.createHeadline(drawables, r, isRound, "Wetter");
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        for (Drawable d : drawables) {
            d.onDraw(canvas, bounds);
        }
    }

    @Override
    public int getTouchedText(int x, int y) {
        return Drawable.Touched.CLOSEME;
    }

    @Override
    public ArrayList<Rect> getDrawnRects() {
        return DrawingHelpers.getDrawnRects(drawables);
    }
}
