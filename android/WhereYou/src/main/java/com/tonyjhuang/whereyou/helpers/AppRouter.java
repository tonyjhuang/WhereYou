package com.tonyjhuang.whereyou.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tonyjhuang.whereyou.AboutActivity;
import com.tonyjhuang.whereyou.BlacklistActivity;
import com.tonyjhuang.whereyou.R;
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
        openActivity(activity, SettingsActivity.class);
    }

    public static void openAboutActivity(Activity activity) {
        openActivity(activity, AboutActivity.class);
    }

    public static void openBlacklistActivity(Activity activity) {
        openActivity(activity, BlacklistActivity.class);
    }

    private static void openActivity(Activity activity, Class clazz) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}
