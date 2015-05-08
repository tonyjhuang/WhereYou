package com.tonyjhuang.whereyou;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.List;

/**
 * Created by tony on 5/7/15.
 */
public class WearableMessageListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WMLService";

    private GoogleApiClient googleApiClient;
    private DataItem mapDataItem;

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = GoogleApiClientBuilder.build(this, this, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (DataEvent event : events) {
            switch (event.getType()) {
                case DataEvent.TYPE_CHANGED:
                    handleChangedDataEvent(event);
                    break;
            }
        }
    }

    private void handleChangedDataEvent(DataEvent dataEvent) {
        Uri uri = dataEvent.getDataItem().getUri();
        String path = uri != null ? uri.getPath() : null;
        if (path == null) return;

        switch (path) {
            case Constants.WEAR_DATA_PATH_NOTIF:
                mapDataItem = dataEvent.getDataItem();
                googleApiClient.connect();
                break;
        }
    }

    private void makeMapNotification(GoogleApiClient googleApiClient, final DataItem dataItem, final CreateNotificationCallback callback) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        Asset mapAsset = dataMap.getAsset(Constants.WEAR_DATA_KEY_MAP_ASSET);
        Wearable.DataApi.getFdForAsset(googleApiClient, mapAsset).setResultCallback(new ResultCallback<DataApi.GetFdForAssetResult>() {
            @Override
            public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                InputStream assetInputStream = getFdForAssetResult.getInputStream();
                NotificationCompat.Builder mapNotificationBuilder = makeMapNotificationWithoutMapBuilder(dataItem);
                if (assetInputStream != null) {
                    Bitmap mapBitmap = BitmapFactory.decodeStream(assetInputStream);
                    callback.onNotificationCreated(addExtraPages(mapNotificationBuilder, dataItem, mapBitmap));
                } else {
                    callback.onNotificationCreated(mapNotificationBuilder.build());
                }
            }
        });
    }

    private Notification addExtraPages(NotificationCompat.Builder builder, DataItem dataItem, Bitmap mapBitmap) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                .bigPicture(mapBitmap)
                .setSummaryText("hey");
        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setStyle(bigPictureStyle)
                        .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                        .build();

        // Extend the notification builder with the second page
        return builder
                .setStyle(bigPictureStyle)
                .extend(new NotificationCompat.WearableExtender().addPage(secondPageNotification))
                .build();
    }

    private NotificationCompat.Builder makeMapNotificationWithoutMapBuilder(DataItem dataItem) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        String content = dataMap.getString(Constants.WEAR_DATA_KEY_CONTENT);
        String title = dataMap.getString(Constants.WEAR_DATA_KEY_TITLE);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content);
    }

    @Override
    public void onConnected(Bundle connectionResult) {
        Log.d(TAG, "connected to google apis");
        makeMapNotification(googleApiClient, mapDataItem, new CreateNotificationCallback() {
            @Override
            public void onNotificationCreated(Notification mapNotification) {
                showNotification(mapNotification);
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
        showNotification(makeMapNotificationWithoutMapBuilder(mapDataItem).build());
    }

    private void showNotification(Notification notification) {
        NotificationManagerCompat.from(this).notify(0, notification);
    }

    interface CreateNotificationCallback {
        void onNotificationCreated(Notification notification);
    }
}
