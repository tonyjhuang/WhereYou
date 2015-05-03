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

    public boolean isFriend(String name) {
        JSONArray friendsList = currentInstallation.getJSONArray("friends");
        if (friendsList == null || friendsList.length() == 0)
            return false;

        try {
            for (int i = 0; i < friendsList.length(); i++) {
                String friend = friendsList.getString(i);
                if (friend.equals(name)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            // if we run into a JSONException, abort.
            logError(e);
        }

        return false;
    }

    // Returns the updated friends list
    public JSONArray removeFriend(String name) {
        JSONArray friendsList = currentInstallation.getJSONArray("friends");
        JSONArray friendsListMeta = currentInstallation.getJSONArray("friendsMeta");

        JSONArray newFriendsList = new JSONArray();
        JSONArray newFriendsListMeta = new JSONArray();

        // Move all friends over to new list except for specified friend.
        try {
            if (friendsList != null) {
                for (int i = 0; i < friendsList.length(); i++) {
                    String friend = friendsList.getString(i);
                    JSONObject meta = friendsListMeta.getJSONObject(i);
                    if (!friend.equals(name)) {
                        newFriendsList.put(friend);
                        newFriendsListMeta.put(meta);
                    }
                }
            }
        } catch (JSONException e) {
            // if we run into a JSONException, abort.
            logError(e);
            return friendsList;
        }

        currentInstallation.put("friends", newFriendsList);
        currentInstallation.put("friendsMeta", newFriendsListMeta);
        currentInstallation.saveInBackground();
        return newFriendsList;
    }

    public void addFriend(final String name, final Callback<JSONArray> callback) {
        if (isFriend(name)) {
            callback.onFinish(currentInstallation.getJSONArray("friends"));
        } else {
            checkName(name, new FunctionCallback<Boolean>() {
                @Override
                public void done(Boolean nameExists, ParseException e) {
                    if (e == null) {
                        if (nameExists) {
                            // Update current installation friends list with new friend.
                            JSONArray friendsList = currentInstallation.getJSONArray("friends");
                            JSONArray friendsListMeta = currentInstallation.getJSONArray("friendsMeta");

                            if (friendsList == null) friendsList = new JSONArray();
                            if (friendsListMeta == null) friendsListMeta = new JSONArray();

                            friendsList.put(name);
                            try {
                                JSONObject meta = new JSONObject();
                                meta.put("name", name);
                                meta.put("score", 0);
                                friendsListMeta.put(meta);
                            } catch (JSONException ex) {
                                logError(ex);
                            }

                            currentInstallation.put("friends", friendsList);
                            currentInstallation.put("friendsMeta", friendsListMeta);
                            currentInstallation.saveInBackground();

                            notifyFriend(name);

                            callback.onFinish(friendsList);
                        } else {
                            callback.onError(new Error("No such user"));
                        }
                    } else {
                        logError(e);
                        callback.onError(e);
                    }
                }
            });
        }
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
            logError(e);
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }

    public void giveFriendPoint(String name) {
        if (!isFriend(name)) return;

        JSONArray friendsMeta = currentInstallation.getJSONArray("friendsMeta");
        try {
            for (int i = 0; i < friendsMeta.length(); i++) {
                JSONObject meta = friendsMeta.getJSONObject(i);
                if (meta.getString("name").equals(name)) {
                    meta.put("score", meta.getInt("score") + 1);
                }
            }
        } catch (JSONException e) {
            logError(e);
        }

        currentInstallation.put("friendsMeta", friendsMeta);
        currentInstallation.saveInBackground();
    }

    public int getFriendScore(String name) {
        if (!isFriend(name)) return 0;

        JSONArray friendsMeta = currentInstallation.getJSONArray("friendsMeta");
        try {
            for (int i = 0; i < friendsMeta.length(); i++) {
                JSONObject meta = friendsMeta.getJSONObject(i);
                if (meta.getString("name").equals(name)) {
                    return meta.getInt("score");
                }
            }
        } catch (JSONException e) {
            logError(e);
        }
        return 0;
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
            logError(e);
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();

        giveFriendPoint(name);
    }

    public boolean isInBlacklist(String name) {
        JSONArray blacklist = currentInstallation.getJSONArray("blacklist");
        if (blacklist == null || blacklist.length() == 0)
            return false;

        try {
            for (int i = 0; i < blacklist.length(); i++) {
                if (blacklist.getString(i).equals(name))
                    return true;
            }
        } catch (JSONException e) {
            logError(e);
        }

        return false;
    }

    public void giveBlacklistPoint(String name) {
        if (!isInBlacklist(name)) return;

        JSONArray blacklistMeta = currentInstallation.getJSONArray("blacklistMeta");
        try {
            for (int i = 0; i < blacklistMeta.length(); i++) {
                JSONObject meta = blacklistMeta.getJSONObject(i);
                if (meta.getString("name").equals(name)) {
                    meta.put("score", meta.getInt("score") + 1);
                }
            }
        } catch (JSONException e) {
            logError(e);
        }

        currentInstallation.put("blacklistMeta", blacklistMeta);
        currentInstallation.saveInBackground();
    }

    public int getBlacklistScore(String name) {
        if (!isInBlacklist(name)) return 0;

        JSONArray blacklistMeta = currentInstallation.getJSONArray("blacklistMeta");
        try {
            for (int i = 0; i < blacklistMeta.length(); i++) {
                JSONObject meta = blacklistMeta.getJSONObject(i);
                if (meta.getString("name").equals(name)) {
                    return meta.getInt("score");
                }
            }
        } catch (JSONException e) {
            logError(e);
        }
        return 0;
    }

    public JSONArray addToBlacklist(String name) {
        JSONArray blacklist = currentInstallation.getJSONArray("blacklist");
        JSONArray blacklistMeta = currentInstallation.getJSONArray("blacklistMeta");
        if (isInBlacklist(name)) return blacklist;

        if (blacklist == null) blacklist = new JSONArray();
        if (blacklistMeta == null) blacklistMeta = new JSONArray();

        blacklist.put(name);
        try {
            JSONObject meta = new JSONObject();
            meta.put("name", name);
            meta.put("score", 0);
            blacklistMeta.put(meta);
        } catch (JSONException e) {
            logError(e);
        }

        currentInstallation.put("blacklist", blacklist);
        currentInstallation.put("blacklistMeta", blacklistMeta);
        currentInstallation.saveInBackground();
        return blacklist;
    }

    public JSONArray removeFromBlacklist(String name) {
        JSONArray blacklist = currentInstallation.getJSONArray("blacklist");
        JSONArray blacklistMeta = currentInstallation.getJSONArray("blacklistMeta");

        if (!isInBlacklist(name))
            return blacklist;

        JSONArray newBlacklist = new JSONArray();
        JSONArray newBlacklistMeta = new JSONArray();
        try {
            if (blacklist != null) {
                for (int i = 0; i < blacklist.length(); i++) {
                    String blacklisted = blacklist.getString(i);
                    JSONObject blacklistedMeta = blacklistMeta.getJSONObject(i);
                    if (!blacklisted.equals(name)) {
                        newBlacklist.put(blacklisted);
                        newBlacklistMeta.put(blacklistedMeta);
                    }
                }
            }
        } catch (JSONException e) {
            // if we run into a JSONException, abort.
            logError(e);
            return blacklist;
        }

        currentInstallation.put("blacklist", newBlacklist);
        currentInstallation.put("blacklistMeta", newBlacklistMeta);
        currentInstallation.saveInBackground();
        return newBlacklist;
    }

    private void logError(Throwable e) {
        Log.e("ParseHelper", e.getMessage());
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
