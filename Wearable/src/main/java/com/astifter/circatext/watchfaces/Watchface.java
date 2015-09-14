package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatextutils.Weather;

/**
 * Created by astifter on 14.09.15.
 */
public interface Watchface {
    void localeChanged();

    void setMetrics(Resources res, WindowInsets insets);

    void setAmbientMode(boolean inAmbientMode);

    void setLowBitAmbientMode(boolean aBoolean);

    void updateVisibilty();

    void setBackgroundColor(int color);

    void startTapHighlight();

    void setPeekCardPosition(Rect rect);

    void setMuteMode(boolean inMuteMode);

    void onDraw(Canvas canvas, Rect bounds);

    void setBatteryInfo(BatteryHelper.BatteryInfo batteryInfo);

    void setEventInfo(CalendarHelper.EventInfo[] meetings);

    void setWeatherInfo(Weather mWeather);
}
