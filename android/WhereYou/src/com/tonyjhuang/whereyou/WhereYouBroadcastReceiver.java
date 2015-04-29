package com.tonyjhuang.whereyou;

import android.content.Context;
import android.content.Intent;
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
                    respond(name);
                    break;
                default:
                    super.onPushReceive(context, intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private void respond(String name) {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        String myName = currentInstallation.getString("name");

        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("name", name);

        JSONObject data = new JSONObject();
        try {
            data.put("name", myName);
            data.put("alert", myName + " wants to know where you at! Tap here to share your location.");
            data.put("action", "com.tonyjhuang.whereyou.RESPOND");
        } catch (JSONException e) {
            Log.e("Main", e.getMessage());
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }
}
