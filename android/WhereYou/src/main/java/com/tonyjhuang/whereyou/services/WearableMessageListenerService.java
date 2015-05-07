package com.tonyjhuang.whereyou.services;

import android.app.Service;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.tonyjhuang.whereyou.api.ParseHelper;

import java.io.UnsupportedEncodingException;

/**
 * Created by tony on 5/6/15.
 */
public class WearableMessageListenerService extends WearableListenerService {
    ParseHelper parseHelper = new ParseHelper();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        switch (messageEvent.getPath()) {
            case "/ask":
                String decodedFriend;
                try {
                    decodedFriend = new String(messageEvent.getData(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e("WearableListener", "couldnt encode bytes as UTF-8");
                    decodedFriend = new String(messageEvent.getData());
                }
                Log.d("WearableListener", "poking " + decodedFriend);
                parseHelper.poke(decodedFriend);
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
