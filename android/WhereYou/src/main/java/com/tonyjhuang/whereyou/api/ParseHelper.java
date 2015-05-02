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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by tony on 4/29/15.
 */
public class ParseHelper {
    private static ArrayList<String> addedMessages = new ArrayList<>();
    static {
        addedMessages.add("{name} added you. Or whatever. I mean, big deal.");
        addedMessages.add("{name} added you as a 'friend'... ;)");
        addedMessages.add("WOOOOOOOO!!! {name} ADDED YOU!!!! WOAHHHHH");
        addedMessages.add("I have good news and bad news. {name} added you. Welp.");
        addedMessages.add("{name} added you. April fools! It's also opposite day!");
        addedMessages.add("{name} just friendzoned you on WhereYou? How's that make you feel?");
        addedMessages.add("{name} just friended you, did you even give them your username?");
        addedMessages.add("Wow, look at you, Mr./Ms. Popular. {name} just added you.");
        addedMessages.add("Wanna hear a joke? {name} just added you.");
        addedMessages.add("{name} added you. Cool.");
        addedMessages.add("{name} just added you. I guess you guys are friends now.");
        addedMessages.add("{name} just added you and they can totally stalk you now ;)");
        addedMessages.add("oooooo {name} just added you whaaaatttt swag swag");
        addedMessages.add("{name} just added you. Time to file for that restraining order!");
    }

    private Random random = new Random();
    private ParseInstallation currentInstallation;

    public ParseHelper() {
        currentInstallation = ParseInstallation.getCurrentInstallation();
    }

    public void checkName(String name, FunctionCallback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name.toLowerCase());
        ParseCloud.callFunctionInBackground("checkName", params, callback);
    }

    public void updateName(String name) {
        currentInstallation.put("name", name);
        currentInstallation.put("nameLowercase", name.toLowerCase());
        currentInstallation.saveInBackground();
    }

    // Returns the updated friends list
    public JSONArray removeFriend(String name) {
        JSONArray friendsList = currentInstallation.getJSONArray("friends");
        JSONArray newFriendsList = new JSONArray();
        try {
            if (friendsList != null) {
                for (int i = 0; i < friendsList.length(); i++) {
                    String friend = friendsList.getString(i);
                    if(!friend.equals(name)) {
                        newFriendsList.put(friend);
                    }
                }
            }
        } catch (JSONException e) {
            // if we run into a JSONException, abort.
            Log.e("ParseHelper", e.getMessage());
            return friendsList;
        }

        currentInstallation.put("friends", newFriendsList);
        currentInstallation.saveInBackground();
        return newFriendsList;
    }

    public void addFriend(final String name, final Callback<JSONArray> callback) {
        checkName(name, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean nameExists, ParseException e) {
                if (e == null) {
                    if (nameExists) {
                        // Update current installation friends list with new friend.
                        JSONArray friendsList = currentInstallation.getJSONArray("friends");
                        if (friendsList == null) friendsList = new JSONArray();
                        friendsList.put(name);
                        currentInstallation.put("friends", friendsList);
                        currentInstallation.saveInBackground();

                        notifyFriend(name);

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

    private void notifyFriend(String name) {
        String myName = currentInstallation.getString("name");
        String message = addedMessages.get(random.nextInt(addedMessages.size()));
        message = message.replace("{name}", myName);

        ParseQuery<ParseInstallation> pushQuery = getQuery(name);

        JSONObject data = new JSONObject();
        try {
            data.put("name", myName);
            data.put("alert", message);
            data.put("action", WhereYouAction.NOTIFY_ADD);
        } catch (JSONException e) {
            Log.e("ParseHelper", e.getMessage());
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }

    private ParseQuery<ParseInstallation> getQuery(String name) {
        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("name", name);
        return pushQuery;
    }

    public void poke(String name) {
        String myName = currentInstallation.getString("name");

        ParseQuery<ParseInstallation> pushQuery = getQuery(name);

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

    public static class SimpleCallback<T> implements Callback<T> {
        @Override
        public void onFinish(T o) {

        }

        @Override
        public void onError(Throwable e) {

        }
    }
}
