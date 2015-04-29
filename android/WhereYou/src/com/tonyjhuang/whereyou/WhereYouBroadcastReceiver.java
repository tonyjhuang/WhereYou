package com.tonyjhuang.whereyou;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.drivemode.intentlog.IntentLogger;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tony on 4/29/15.
 */
public class WhereYouBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.d(TAG, "onPushOpen");
        IntentLogger.dump(TAG, intent);
        Intent startWakeful = new Intent();
        startWakeful.putExtras(intent);
        startWakeful.setAction("com.tonyjhuang.whereyou.GET_LOCATION");
        context.sendBroadcast(startWakeful);
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
            switch(action) {
                case "com.tonyjhuang.whereyou.RESPOND":
                    Log.d(TAG, "hey!!!");
                    Toast.makeText(context, "GOT A RESPONSE!", Toast.LENGTH_SHORT).show();
                    break;
                case "com.tonyjhuang.whereyou.ASK":
                    String name = json.getString("name");
                    /*respond(name);
                    break;*/
                default:
                    super.onPushReceive(context, intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
