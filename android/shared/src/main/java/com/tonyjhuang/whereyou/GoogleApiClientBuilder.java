package com.tonyjhuang.whereyou;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by tony on 5/7/15.
 */
public class GoogleApiClientBuilder {

    public static GoogleApiClient build(Context context,
                                        GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        return new GoogleApiClient.Builder(context)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
    }
}
