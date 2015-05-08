package com.tonyjhuang.whereyou.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.drivemode.intentlog.IntentLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.tonyjhuang.whereyou.Constants;
import com.tonyjhuang.whereyou.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tony on 5/7/15.
 */
public class SendWearableNotificationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SendWearableNotif";

    private GoogleApiClient googleApiClient;
    private Intent intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        IntentLogger.dump(TAG, intent);

        this.intent = intent;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");


        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String name = json.getString("name");
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            String message = name + " is at " + lat + ", " + lng;

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.WEAR_DATA_PATH_NOTIF);
            DataMap dataMap = putDataMapRequest.getDataMap();
            dataMap.putString(Constants.WEAR_DATA_KEY_CONTENT, message);
            dataMap.putString(Constants.WEAR_DATA_KEY_TITLE, getString(R.string.app_name));
            dataMap.putDouble(Constants.WEAR_DATA_KEY_LAT, lat);
            dataMap.putDouble(Constants.WEAR_DATA_KEY_LNG, lng);

            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(googleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "buildWatchOnlyNotification(): Failed to set the data, "
                                        + "status: " + dataItemResult.getStatus().getStatusCode());
                            } else {
                                Log.d(TAG, "put map notification dataitem success!");
                            }
                            finish();
                        }
                    });
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
        finish();
    }

    private void finish() {
        stopSelf();
    }
}
