package com.tonyjhuang.whereyou.wear;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.tonyjhuang.whereyou.Constants;
import com.tonyjhuang.whereyou.api.ParseHelper;

/**
 * Created by tony on 5/6/15.
 */
public class WearDataSync {
    private static final String TAG = "WearDataSync";
    private static GoogleApiClient client;

    public static void syncData(Context context) {
        if (client == null || !client.isConnected()) {
            client = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                            syncDataAfterConnect(client);
                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.e(TAG, "onConnectionFailed: " + result);
                        }
                    }) // Request access only to the Wearable API
                    .addApiIfAvailable(Wearable.API)
                    .build();
            client.connect();
        } else {
            syncDataAfterConnect(client);
        }
    }

    private static void syncDataAfterConnect(GoogleApiClient client) {
        Log.d("WearDataSync", "syncing friends!");
        ParseHelper parseHelper = new ParseHelper();
        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.WEAR_DATA_PATH_FRIENDS);
        dataMap.getDataMap().putStringArrayList(Constants.WEAR_DATA_KEY_FRIENDS, parseHelper.getFriends());
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(client, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess())
                    Log.d(TAG, "*****DATA SYNCED*****");
                else
                    Log.e(TAG, "Failed to sync friends data :(");
            }
        });
    }

}
