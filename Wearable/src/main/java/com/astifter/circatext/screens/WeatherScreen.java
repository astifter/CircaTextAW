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
        String detailInfo = "Rq: " + reqAge + "m, Rc: " + recAge + "m, ";

        String srvAge, temp, condition, detailCondition, loc, code;
        try {
            srvAge = CTU.getAge(now, mWeather.lastupdate);
        } catch (Throwable t) {
            srvAge = "?";
        }
        try {
            temp = String.format("%2.0f°C", mWeather.temperature.getTemp());
        } catch (Throwable t) {
            temp = "-";
        }
        try {
            condition = mWeather.currentCondition.getDescr();
            detailCondition = mWeather.currentCondition.getDescr();
            code = ", WC:" + mWeather.currentCondition.getWeatherId();
        } catch (Throwable t) {
            condition = "-";
            detailCondition = "-";
            code = "";
        }
        try {
            loc = mWeather.location.getCity() + ", " + mWeather.location.getCountry();
        } catch (Throwable t) {
            loc = "-";
        }
        detailInfo += "Up: " + srvAge + "m" + code;

        ScreenHelpers.createHeadline(drawables, r, isRound, "Wetter");

        ScreenHelpers.createStaticTest(drawables, temp, 0, new Rect(5, 32, 95, 52), Drawable.Align.CENTER, bgColor);
        ScreenHelpers.createStaticTest(drawables, condition, 0, new Rect(5, 52, 95, 67), Drawable.Align.CENTER, bgColor);
        ScreenHelpers.createStaticTest(drawables, loc, 0, new Rect(5, 67, 95, 76), Drawable.Align.CENTER, bgColor);
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
