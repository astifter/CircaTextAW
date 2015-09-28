package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;
import android.graphics.Typeface;

public class DrawingHelpers {
    public static Typeface NORMAL_TYPEFACE;

    public static Rect percentageToRect(Rect percentage, Rect screenDims) {
        return new Rect((int)(screenDims.width() *percentage.left/100f),
                        (int)(screenDims.height()*percentage.top/100f),
                        (int)(screenDims.width() *percentage.right/100f),
                        (int)(screenDims.height()*percentage.bottom/100f));
    }
}
