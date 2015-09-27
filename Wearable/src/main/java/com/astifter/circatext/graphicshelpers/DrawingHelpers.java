package com.astifter.circatext.graphicshelpers;

import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Created by astifter on 27.09.15.
 */
public class DrawingHelpers {
    public static Typeface NORMAL_TYPEFACE;

    Rect percentageToRect(Rect percentage, Rect screenDims) {
        Rect r = new Rect((int)(screenDims.width() *percentage.left/100f),
                          (int)(screenDims.height()*percentage.top/100f),
                          (int)(screenDims.width() *percentage.right/100f),
                          (int)(screenDims.height()*percentage.bottom/100f));
        return r;
    }
}
