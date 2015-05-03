package com.tonyjhuang.whereyou.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParsePushBroadcastReceiver;
import com.tonyjhuang.whereyou.MapActivity;
import com.tonyjhuang.whereyou.api.ParseHelper;
import com.tonyjhuang.whereyou.services.WhereYouAction;

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
            String action = json.getString("action");
            switch (action) {
                case WhereYouAction.ASK:
                    /*
                    User has clicked on our ASK notification.
                    Start our wakeful broadcast receiver and send out our location.
                     */
                    Intent startWakeful = new Intent();
                    startWakeful.putExtras(intent);
                    startWakeful.setAction(WhereYouAction.GET_LOCATION);
                    context.sendBroadcast(startWakeful);
                    break;
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
            String action = json.getString("action");
            String message = json.getString("alert");
            String name = json.getString("name");
            switch (action) {
                case WhereYouAction.RESPOND:
                    /*
                    We got a response from our target. Hooray!
                     */
                    Intent openMap = new Intent(context, MapActivity.class);
                    openMap.putExtras(intent);
                    openMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(openMap);
                    break;
                case WhereYouAction.ASK:
                    /*
                    Let's take over the notification process to replace the current notification if there is one
                     */
                    if (!parseHelper.isInBlacklist(name)) {
                        showAskNotification(context, intent, name);
                    } else {
                        parseHelper.giveBlacklistPoint(name);
                    }
                    break;
                case WhereYouAction.NOTIFY_ADD:
                    if(parseHelper.isInBlacklist(name))
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
            action = pushData.optString("action", null);
        }

        if (action != null) {
            Bundle notification = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(notification);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        Notification notification1 = this.getNotification(context, intent);
        if (notification1 != null) {
            showNotification(context, notification1, name);
        }
    }

    private void showNotification(Context context, Notification notification, String name) {
        if (context != null && notification != null) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = name.hashCode();//(int) System.currentTimeMillis();

            try {
                nm.notify(notificationId, notification);
            } catch (SecurityException var6) {
                notification.defaults = 5;
                nm.notify(notificationId, notification);
            }
        }

    }
}
