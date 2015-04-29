package com.tonyjhuang.whereyou;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by tony on 4/29/15.
 * Calls a wakeful service to handle getting location.
 */
public class WakefulWhereYouBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WakefulBR", "got intent");
        Intent service = new Intent(context, WakefulLocationService.class);
        service.putExtras(intent);
        startWakefulService(context, service);
    }
}
