package com.tonyjhuang.whereyou;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

/**
 * Created by Tony on 3/23/14.
 * Contains a static cache of inflated Typefaces to avoid recreating a Typeface each time it's used.
 */
public class TypefaceManager {
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();
    private static String defaultTypeface;

    /**
     * Returns a default typeface if the passed in typeface path is not found
     */
    public static Typeface get(Context c, String assetPath) {
        initDefaultTypeface(c);
        if (assetPath == null || assetPath.equals("null") || assetPath.equals("null.ttf"))
            assetPath = defaultTypeface;
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e("TypefaceManager", "Could not get typeface '" + assetPath
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }

    private static void initDefaultTypeface(Context context) {
        if (defaultTypeface == null)
            defaultTypeface = context.getString(R.string.fonttype) + ".ttf";
    }
}
