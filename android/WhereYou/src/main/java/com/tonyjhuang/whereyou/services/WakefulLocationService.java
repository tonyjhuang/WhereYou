package com.tonyjhuang.whereyou.services;

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
import com.tonyjhuang.whereyou.receivers.WakefulWhereYouBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by tony on 4/29/15.
 */
public class WakefulLocationService extends IntentService {
    private static final int LOCATION_TIMEOUT = 15000; // 15 seconds

    private Looper wakefulLooper;
    private Intent intent;

    public WakefulLocationService() {
        super("WakefulLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("LocationService", "onHandleIntent");
        IntentLogger.dump("LocationService", intent);

        this.intent = intent;

        // Keep this service alive while we get the location.
        if(Looper.myLooper() == null) Looper.prepare();
        wakefulLooper = Looper.myLooper();

        // Set a timeout for getting location.
        final Handler timeoutHandler = new Handler();
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("WakefulService", "Time out!");
                finish();
            }
        }, LOCATION_TIMEOUT);

        // Get location
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        timeoutHandler.removeCallbacksAndMessages(null);

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        String loc = "latitude: " + lat + "\n" +
                                "longitude: " + lng;
                        Log.d("LocationService", loc);
                        respond(lat, lng);
                    }
                });

        Looper.loop();
    }

    private void finish() {
        WakefulWhereYouBroadcastReceiver.completeWakefulIntent(intent);
        wakefulLooper.quit();
    }

    private void respond(double lat, double lng) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String name = json.getString("name");

            ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
            String myName = currentInstallation.getString("name");

            ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
            pushQuery.whereEqualTo("name", name);

            JSONObject data = new JSONObject();
            try {
                data.put("name", myName);
                data.put("alert", myName + " is at " + lat + ", " + lng + "build/intermediates/exploded-aar/com.google.android.gms/play-services-maps/6.5.87/res");
                data.put("action", WhereYouAction.RESPOND);
            } catch (JSONException e) {
                Log.e("Main", e.getMessage());
            }

            ParsePush push = new ParsePush();
            push.setQuery(pushQuery);
            push.setData(data);
            push.sendInBackground();
        } catch (JSONException e) {
            Log.e("WakefulService", e.getMessage());
        } finally {
            finish();
        }
    }
}
