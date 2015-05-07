package com.tonyjhuang.whereyou;

import android.content.Context;

import com.tonyjhuang.whereyou.common.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by tony on 5/1/15.
 */
public class ColorPicker {

    private static int[] colors;
    private static Random random = new Random();

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

    public static int getRandomColor() {
        return colors[random.nextInt(colors.length)];
    }

    public static int[] getRandomColors(int n) {
        // Get the colors
        ArrayList<Integer> randomColors = new ArrayList<>();
        while(randomColors.size() < n) {
            int color = getRandomColor();
            if (!randomColors.contains(color))
                randomColors.add(color);
        }

        // Change into int array
        int[] retval = new int[randomColors.size()];
        Iterator<Integer> iterator = randomColors.iterator();
        for (int i = 0; i < retval.length; i++) {
            retval[i] = iterator.next();
        }
        return retval;
    }

}
