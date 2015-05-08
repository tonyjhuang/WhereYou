package com.tonyjhuang.whereyou.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.tonyjhuang.whereyou.Constants;
import com.tonyjhuang.whereyou.MapActivity;
import com.tonyjhuang.whereyou.api.ParseHelper;
import com.tonyjhuang.whereyou.services.GetLocationService;
import com.tonyjhuang.whereyou.services.SendWearableNotificationService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tony on 4/29/15.
 * Handles top level push notification processes and routing.
 */
public class WhereYouBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";
    private ParseHelper parseHelper = new ParseHelper();

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.d(TAG, "onPushOpen");
        IntentLogger.dump(TAG, intent);

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String action = json.getString(Constants.PARSE_KEY_ACTION);
            switch (action) {
                case Constants.ACTION_ASK:
                    ParseAnalytics.trackAppOpened(intent);
                    /*
                    User has clicked on our ASK notification.
                    Get location!
                     */
                    Intent locationService = new Intent(context, GetLocationService.class);
                    locationService.putExtras(intent);
                    context.startService(locationService);
                    break;
                default:
                    super.onPushOpen(context, intent);
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        Log.d(TAG, "onPushDismiss");
        IntentLogger.dump(TAG, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.d(TAG, "onPushReceive");
        IntentLogger.dump(TAG, intent);
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String action = json.getString(Constants.PARSE_KEY_ACTION);
            Log.i("WhereYouBR", "ACTION: " + action + " *****************");
            String name = json.getString("name");
            switch (action) {
                case Constants.ACTION_RESPOND:
                    /*
                    We got a response from our target. Hooray!
                     */
                    Log.i("WhereYouBR", "RESPONSE *************");
                    Intent openMap = new Intent(context, MapActivity.class);
                    openMap.putExtras(intent);
                    openMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(openMap);

                    Intent sendMapService = new Intent(context, SendWearableNotificationService.class);
                    sendMapService.putExtras(intent);
                    context.startService(sendMapService);
                    break;
                case Constants.ACTION_ASK:
                    /*
                    Let's take over the notification process to replace the current notification if there is one
                     */
                    if (!parseHelper.isInBlacklist(name)) {
                        showAskNotification(context, intent, name);
                    } else {
                        parseHelper.giveBlacklistPoint(name);
                    }
                    break;
                case Constants.ACTION_NOTIFY_ADD:
                    if (parseHelper.isInBlacklist(name))
                        return;
                    /*
                    Don't do anything here, let the user click the notification before acting.
                     */
                default:
                    super.onPushReceive(context, intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /* Taken from the guts of ParsePushBroadcastReceiver */
    private void showAskNotification(Context context, Intent intent, String name) {
        JSONObject pushData = null;

        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException e) {
            Log.e("WhereYouBR", "Unexpected JSONException when receiving push data: " + e.getMessage());
        }

        String action = null;
        if (pushData != null) {
            action = pushData.optString(Constants.PARSE_KEY_ACTION, null);
        }

        if (action != null) {
            Bundle notification = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(notification);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        Notification notification = this.getNotification(context, intent);
        if (notification != null) {
            showNotification(context, notification, name);
        }
    }

    private void showNotification(Context context, Notification notification, String name) {
        if (context != null && notification != null) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = name.hashCode();//(int) System.currentTimeMillis();

            try {
                nm.notify(notificationId, notification);
            } catch (SecurityException var6) {
                // uhhh taken from parse code
                notification.defaults = 5;
                nm.notify(notificationId, notification);
            }
        }

    }
}
