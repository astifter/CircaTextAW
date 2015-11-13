package com.astifter.circatext.watchfaces;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.WindowInsets;

import com.astifter.circatext.datahelpers.BatteryHelper;
import com.astifter.circatext.datahelpers.CalendarHelper;
import com.astifter.circatext.drawables.Drawable;
import com.astifter.circatextutils.CTCs;
import com.astifter.circatextutils.Weather;

import java.util.Date;

public interface WatchFace {
    void localeChanged();

    void setMetrics(Resources r, WindowInsets insets);

    void setAmbientMode(boolean inAmbientMode);

    void setLowBitAmbientMode(boolean aBoolean);

    void setPeekCardPosition(Rect rect);

    void setMuteMode(boolean inMuteMode);

    void startTapHighlight();

    void setBatteryInfo(BatteryHelper.BatteryInfo batteryInfo);

    void setEventInfo(CalendarHelper.EventInfo[] meetings);

    void setWeatherInfo(Weather weather, Date requested, Date received);

    void onDraw(Canvas canvas, Rect bounds);

    void updateVisibilty();

    int getTouchedText(int x, int y);

    void setRoundMode(Drawable.RoundEmulation b);

    void setSelectedConfig(CTCs.Config cfg);

    void setStringer(CTCs.Stringer cfg);
}
