package com.astifter.circatext.screens;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatext.drawables.DrawableHelpers;
import com.astifter.circatextutils.CTU;
import com.astifter.circatextutils.Weather;

import java.util.ArrayList;
import java.util.Date;

public class WeatherScreen implements Screen {
    private final ArrayList<Drawable> drawables = new ArrayList<>();

    public WeatherScreen(Resources r, boolean isRound, Weather mWeather, Date mWeatherReq, Date mWeatherRec, int bgColor) {
        long now = System.currentTimeMillis();
        String reqAge = CTU.getAge(now, mWeatherReq);
        String recAge = CTU.getAge(now, mWeatherRec);
        String srvAge, temp, condition, loc;
        if (mWeather != null) {
            srvAge = CTU.getAge(now, mWeather.lastupdate);
            temp = String.format("%2.0f°C", mWeather.temperature.getTemp());
            condition = mWeather.currentCondition.getDescr();
            loc = mWeather.location.getCity() + ", " + mWeather.location.getCountry();
        } else {
            srvAge = "?";
            temp = "-";
            condition = "-";
            loc = "-";
        }

        ScreenHelpers.createHeadline(drawables, r, isRound, "Wetter");

        ScreenHelpers.createStaticTest(drawables, temp, 0, new Rect(5, 32, 95, 52), Drawable.Align.CENTER, bgColor);
        ScreenHelpers.createStaticTest(drawables, condition, 0, new Rect(5, 52, 95, 67), Drawable.Align.CENTER, bgColor);
        ScreenHelpers.createStaticTest(drawables, loc, 0, new Rect(5, 67, 95, 76), Drawable.Align.CENTER, bgColor);

        String detailInfo = "Rq: " + reqAge + "m, Rc: " + recAge + "m, Up: " + srvAge + "m";
        String detailCondition = "";
        if (mWeather != null) {
            detailInfo += ", WC:" + mWeather.currentCondition.getWeatherId();
            detailCondition = mWeather.currentCondition.getDescr();
        }
        ScreenHelpers.createStaticTest(drawables, detailCondition, 0, new Rect(5, 83, 95, 89), Drawable.Align.CENTER, bgColor);
        ScreenHelpers.createStaticTest(drawables, detailInfo, 0, new Rect(5, 89, 95, 95), Drawable.Align.CENTER, bgColor);
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
        return DrawableHelpers.getDrawnRects(drawables);
    }
}
