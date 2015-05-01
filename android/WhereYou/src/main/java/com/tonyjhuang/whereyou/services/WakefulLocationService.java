package com.tonyjhuang.whereyou.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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
    private Toast toast;

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
                toast.cancel();
                toast = Toast.makeText(WakefulLocationService.this, "Noooooooooooo couldn't get location :(", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        }, LOCATION_TIMEOUT);

        toast = Toast.makeText(this, "Getting location...", Toast.LENGTH_LONG);
        toast.show();

        // Get location
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        timeoutHandler.removeCallbacksAndMessages(null);

                        toast.cancel();
                        toast = Toast.makeText(WakefulLocationService.this,
                                "Got your location or whatever. Sent.", Toast.LENGTH_SHORT);
                        toast.show();

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        float  acc = location.getAccuracy();

                        String loc = "latitude: " + lat + "\n" +
                                "longitude: " + lng;
                        Log.d("LocationService", loc);
                        respond(lat, lng, acc);
                    }
                });

        Looper.loop();
    }

    private void finish() {
        WakefulWhereYouBroadcastReceiver.completeWakefulIntent(intent);
        wakefulLooper.quit();
    }

    private void respond(double lat, double lng, float acc) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String name = json.getString("name");

            ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
            String myName = currentInstallation.getString("name");

            Log.d("WakefulService", "Creating a push to " + name);

            ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
            pushQuery.whereEqualTo("name", name);

            JSONObject data = new JSONObject();
            try {
                data.put("name", myName);
                data.put("lat", lat);
                data.put("lng", lng);
                data.put("acc", acc);
                data.put("alert", myName + " is at " + lat + ", " + lng + ": " + acc);
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
