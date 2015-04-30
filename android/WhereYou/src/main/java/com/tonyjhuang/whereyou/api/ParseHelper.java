package com.tonyjhuang.whereyou.api;

import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.tonyjhuang.whereyou.services.WhereYouAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tony on 4/29/15.
 */
public class ParseHelper {

    public void checkName(String name, FunctionCallback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name.toLowerCase());
        ParseCloud.callFunctionInBackground("checkName", params, callback);
    }

    public void updateName(String name) {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put("name", name);
        currentInstallation.put("nameLowercase", name.toLowerCase());
        currentInstallation.saveInBackground();
    }

    public void addFriend(final String name, final Callback<JSONArray> callback) {
        checkName(name, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean nameExists, ParseException e) {
                if (e == null) {
                    if (nameExists) {
                        // Update current installation friends list with new friend.
                        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
                        JSONArray friendsList = currentInstallation.getJSONArray("friends");
                        if (friendsList == null) friendsList = new JSONArray();
                        friendsList.put(name);
                        currentInstallation.put("friends", friendsList);
                        currentInstallation.saveInBackground();

                        callback.onFinish(friendsList);
                    } else {
                        callback.onError(new Error("No such user"));
                    }
                } else {
                    Log.e("ParseHelper", e.getMessage());
                    callback.onError(e);
                }
            }
        });
    }

    public void poke(String name) {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        String myName = currentInstallation.getString("name");

        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("name", name);

        JSONObject data = new JSONObject();
        try {
            data.put("name", myName);
            data.put("alert", myName + " wants to know where you at! Tap here to share your location.");
            data.put("action", WhereYouAction.ASK);
        } catch (JSONException e) {
            Log.e("ParseHelper", e.getMessage());
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }

    public interface Callback<T> {
        void onFinish(T t);

        void onError(Throwable e);
    }
}
