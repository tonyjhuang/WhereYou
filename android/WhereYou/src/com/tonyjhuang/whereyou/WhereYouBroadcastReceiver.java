package com.tonyjhuang.whereyou;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by tony on 4/29/15.
 */
public class WhereYouBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.d(TAG, "onPushOpen");
        IntentLogger.dump(TAG, intent);
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        Log.d(TAG, "onPushDismiss");
        IntentLogger.dump(TAG, intent);
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.d(TAG, "onPushReceive");
        IntentLogger.dump(TAG, intent);
        super.onPushReceive(context, intent);
    }
}
