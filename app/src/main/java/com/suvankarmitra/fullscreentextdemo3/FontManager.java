package com.suvankarmitra.fullscreentextdemo3;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by suvankar on 12/11/17.
 */

public class FontManager {

    private Typeface mTypefaceJosefinRegular;

    public FontManager(Context context) {
        AssetManager am = context.getAssets();
        mTypefaceJosefinRegular = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "JosefinSans-Regular.ttf"));
    }

    /**
     *
     * @param mViewElement TextView element
     * @param type {jos, jos-bo, lat, lat-bo}
     */
    public void setTypeface(TextView mViewElement, String type) {
        if(type !=null && !type.isEmpty()) {
            if (type.equalsIgnoreCase("jos")) {
                mViewElement.setTypeface(mTypefaceJosefinRegular);
            }
        }
    }

    /**
     *
     * @param mViewElements Array of TextView elements
     * @param type {jos, jos-bo, lat, lat-bo}
     */
    public void setTypeface(TextView []mViewElements, String type) {
        if(type !=null && !type.isEmpty()) {
            for (TextView tv: mViewElements) {
                setTypeface(tv,type);
            }
        }
    }
}
