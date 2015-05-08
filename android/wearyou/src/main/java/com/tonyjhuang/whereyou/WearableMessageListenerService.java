package com.tonyjhuang.whereyou;

import android.app.Notification;
import android.app.NotificationManager;
import android.net.Uri;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Created by tony on 5/7/15.
 */
public class WearableMessageListenerService extends WearableListenerService {

    private static final String TAG = "WMLService";

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
                makeMapNotification(dataEvent.getDataItem());
                break;
        }
    }

    private void makeMapNotification(DataItem dataItem) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        String content = dataMap.getString(Constants.WEAR_DATA_KEY_CONTENT);
        String title = dataMap.getString(Constants.WEAR_DATA_KEY_TITLE);


        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content);

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(0, builder.build());
    }

}
