package com.tonyjhuang.whereyou.helpers;

import android.content.Context;

import com.tonyjhuang.whereyou.R;

/**
 * Created by tony on 5/1/15.
 */
public class ColorPicker {

    private static int[] colors;

    private ColorPicker() {}

    public static void init(Context context) {
        colors = context.getResources().getIntArray(R.array.accents);
    }

    public static int getColor(String name) {
        if(colors == null) {
            throw new RuntimeException("You must initialize ColorPicker with #init before using #getColor!");
        } else {
            return colors[Math.abs(name.hashCode()) % colors.length];
        }
    }

}
