package com.tonyjhuang.whereyou.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParsePushBroadcastReceiver;
import com.tonyjhuang.whereyou.MapActivity;
import com.tonyjhuang.whereyou.services.WhereYouAction;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tony on 4/29/15.
 * Handles top level push notification processes and routing.
 */
public class WhereYouBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.d(TAG, "onPushOpen");
        IntentLogger.dump(TAG, intent);

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String action = json.getString("action");
            switch(action) {
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
            switch(action) {
                case WhereYouAction.RESPOND:
                    /*
                    We got a response from our target. Hooray!
                     */
                    Intent openMap = new Intent(context, MapActivity.class);
                    openMap.putExtras(intent);
                    openMap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(openMap);
                    break;
                case WhereYouAction.NOTIFY_ADD:
                case WhereYouAction.ASK:
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


}
