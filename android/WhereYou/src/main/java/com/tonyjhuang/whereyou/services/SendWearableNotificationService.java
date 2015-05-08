package com.tonyjhuang.whereyou.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.drivemode.intentlog.IntentLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.tonyjhuang.whereyou.Constants;
import com.tonyjhuang.whereyou.GoogleApiClientBuilder;
import com.tonyjhuang.whereyou.R;
import com.tonyjhuang.whereyou.helpers.StreetAddress;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

        googleApiClient = GoogleApiClientBuilder.build(this, this, this);
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        String staticMapUrl = getStaticMapUrl();
        if(staticMapUrl == null) {
            sendCreateNotificationMessage(null);
        } else {
            Log.d(TAG, staticMapUrl);
            new DownloadMapBitmapTask().execute(getStaticMapUrl());
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

    private String getStaticMapUrl() {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            return buildStaticMapUrl(lat, lng);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("all")
    private String buildStaticMapUrl(double lat, double lng) {
        String icon = "http://tonyjhuang.com/b/currentlocation.png";
        String url = "https://maps.google.com/maps/api/staticmap" +
                "?markers=icon:" + icon + "|shadow:false|" + lat + "," + lng +
                "&zoom=16&size=400x400&sensor=false" +
                "&key=" + getString(R.string.maps_api_key);

        return url;
    }

    private void sendCreateNotificationMessage(Bitmap bitmap) {
        Asset randomAsset = null;

        if(bitmap != null) {
            randomAsset = createAssetFromBitmap(bitmap);
        }

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String name = json.getString("name");
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            String message = StreetAddress.getStreetAddress(this, lat, lng, false);

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.WEAR_DATA_PATH_NOTIF);
            DataMap dataMap = putDataMapRequest.getDataMap();
            dataMap.putString(Constants.WEAR_DATA_KEY_CONTENT, message);
            dataMap.putString(Constants.WEAR_DATA_KEY_TITLE, name);
            dataMap.putDouble(Constants.WEAR_DATA_KEY_LAT, lat);
            dataMap.putDouble(Constants.WEAR_DATA_KEY_LNG, lng);
            dataMap.putAsset(Constants.WEAR_DATA_KEY_MAP_ASSET, randomAsset);

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
            finish();
        }
    }

    private void finish() {
        stopSelf();
    }

    private Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private class DownloadMapBitmapTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            sendCreateNotificationMessage(bitmap);
        }
    }
}
