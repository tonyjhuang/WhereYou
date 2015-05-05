package com.tonyjhuang.whereyou.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.drivemode.intentlog.IntentLogger;
import com.tonyjhuang.whereyou.api.ParseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by tony on 5/4/15.
 */
public class GetLocationService extends Service {
    private static final int LOCATION_TIMEOUT = 30 * 1000;

    private Intent intent;
    private Handler toastHandler = new Handler();
    private ParseHelper parseHelper = new ParseHelper();
    private int startId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startId = startId;
        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void handleIntent(Intent intent) {
        Log.d("GetLocationService", "onHandleIntent");
        IntentLogger.dump("LocationService", intent);

        this.intent = intent;

        // Set a timeout for getting location.
        final Handler timeoutHandler = new Handler();
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("GetLocationService", "Time out!");
                showToast("Couldn't get location :(");
                finish();
            }
        }, LOCATION_TIMEOUT);

        showToast("Getting location...");

        // Get location
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        timeoutHandler.removeCallbacksAndMessages(null);

                        showToast("Got your location or whatever. Sent.");
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        float acc = location.getAccuracy();

                        String loc = "latitude: " + lat + "\n" +
                                "longitude: " + lng;
                        Log.d("GetLocationService", loc);
                        respond(lat, lng, acc);
                    }
                });
    }

    private void respond(double lat, double lng, float acc) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String name = json.getString("name");

            parseHelper.sendLocation(name, lat, lng, acc);
        } catch (JSONException e) {
            Log.e("GetLocationService", e.getMessage());
        } finally {
            finish();
        }
    }

    private void showToast(final String message) {
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GetLocationService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finish() {
        stopSelf(startId);
    }
}
