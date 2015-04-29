package com.tonyjhuang.whereyou;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by tony on 4/29/15.
 */
public class WakefulLocationService extends IntentService {

    public WakefulLocationService() {
        super("WakefulLocationService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d("LocationService", "onHandleIntent");
        IntentLogger.dump("LocationService", intent);

        // Keep this service alive while we get the location.
        if(Looper.myLooper() == null) Looper.prepare();
        final Looper wakefulLooper = Looper.myLooper();

        // Set a timeout for getting location.
        final Handler timeoutHandler = new Handler();
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("WakefulService", "Time out!");
                WakefulWhereYouBroadcastReceiver.completeWakefulIntent(intent);
                wakefulLooper.quit();
            }
        }, 5000);

        // Get location
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        timeoutHandler.removeCallbacksAndMessages(null);
                        String loc = "latitude: " + location.getLatitude() + "\n" +
                                "longitude: " + location.getLongitude();
                        Log.d("LocationService", loc);
                        WakefulWhereYouBroadcastReceiver.completeWakefulIntent(intent);
                        wakefulLooper.quit();
                    }
                });

        Looper.loop();
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
