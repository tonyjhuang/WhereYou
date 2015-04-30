package com.tonyjhuang.whereyou.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
}
