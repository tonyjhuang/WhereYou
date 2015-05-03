package com.tonyjhuang.whereyou.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tonyjhuang.whereyou.SettingsActivity;

/**
 * Created by tony on 4/29/15.
 */
public class AppRouter {
    public static void redirectTo(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        if(context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public static void openSettingsActivity(Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }
}
