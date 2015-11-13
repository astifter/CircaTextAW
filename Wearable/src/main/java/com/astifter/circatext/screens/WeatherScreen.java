package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.graphicshelpers.DrawingHelpers;
import com.astifter.circatext.watchfaces.BaseWatchFace;
import com.astifter.circatextutils.CTU;
import com.astifter.circatextutils.Weather;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class WeatherScreen implements Screen {
    ArrayList<Drawable> drawables = new ArrayList<>();

    public WeatherScreen(Resources r, boolean isRound, Weather mWeather, Date mWeatherReq, Date mWeatherRec, int bgColor) {
        long now = System.currentTimeMillis();
        String reqAge = CTU.getAge(now, mWeatherReq);
        String recAge = CTU.getAge(now, mWeatherRec);
        String srvAge, temp, cond1, cond2, loc;
        if (mWeather != null) {
            srvAge = CTU.getAge(now, mWeather.lastupdate);
            temp = String.format("%2.0f°C", mWeather.temperature.getTemp());
            cond1 = mWeather.currentCondition.getCondition();
            cond2 = mWeather.currentCondition.getDescr() + ", " + Weather.translate(mWeather.currentCondition.getWeatherId());
            loc = mWeather.location.getCity() + "," + mWeather.location.getCountry();
        } else {
            srvAge = "?";
            temp = "-";
            cond1 = "-";
            cond2 = "-";
            loc = "-";
        }

        DrawingHelpers.createHeadline(drawables, r, isRound, "Wetter");

        DrawingHelpers.createStaticTest(drawables, loc, 0, new Rect(5, 40, 95, 50), Drawable.Align.LEFT, bgColor);
        DrawingHelpers.createStaticTest(drawables, temp + ", " + cond1, 0, new Rect(5, 50, 95, 60), Drawable.Align.LEFT, bgColor);
        DrawingHelpers.createStaticTest(drawables, cond2, 0, new Rect(5, 60, 95, 70), Drawable.Align.LEFT, bgColor);

        DrawingHelpers.createStaticTest(drawables, "Rq: " + reqAge + "m, Rc: " + recAge + "m, Up: " + srvAge + "m", 0, new Rect(5, 87, 95, 95), Drawable.Align.CENTER, bgColor);
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
