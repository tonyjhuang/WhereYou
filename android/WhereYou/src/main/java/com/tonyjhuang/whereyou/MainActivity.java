package com.tonyjhuang.whereyou;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends WhereYouActivity {

    @InjectView(R.id.hello)
    TextView helloText;
    @InjectView(R.id.username_input)
    EditText usernameInput;
    @InjectView(R.id.friend_input)
    EditText friendInput;
    @InjectView(R.id.location)
    TextView locationText;
    @InjectView(R.id.friends)
    ListView friendsListView;

    private FriendsListAdapter friendsAdapter;


    private void redirectToSignup() {
        Intent intent = new Intent(this, com.tonyjhuang.whereyou.SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Main", "HELLO WORLD! **************************************************");
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        String name = currentInstallation.getString("name");
        if (name == null) {
            redirectToSignup();
        } else {
            setContentView(R.layout.activity_main);

            setNameView(name);
            friendsAdapter = new FriendsListAdapter(currentInstallation.getJSONArray("friends"));
            friendsListView.setAdapter(friendsAdapter);
        }
    }

    @OnClick(R.id.username_save)
    public void onUsernameSaveClick(View view) {
        final String newName = usernameInput.getText().toString();
        setNameView(newName);

        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put("name", newName);
        currentInstallation.saveInBackground();
    }

    private void getInstallation(String name, GetCallback<ParseObject> callback) {
        ParseQuery<ParseObject> query = new ParseQuery<>("Installation");
        query.whereEqualTo("name", name);
        query.getFirstInBackground(callback);
    }

    private void setNameView(String name) {
        helloText.setText("Hello " + name + "!");
    }

    private void poke(String name) {
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        String myName = currentInstallation.getString("name");

        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo("name", name);

        JSONObject data = new JSONObject();
        try {
            data.put("name", myName);
            data.put("alert", myName + " wants to know where you at! Tap here to share your location.");
            data.put("action", "com.tonyjhuang.whereyou.ASK");
        } catch (JSONException e) {
            Log.e("Main", e.getMessage());
        }

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        push.setData(data);
        push.sendInBackground();
    }

    @OnClick(R.id.friend_add)
    public void onFriendAddClick(View view) {
        final String newFriend = friendInput.getText().toString();
        final ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        JSONArray friendsList = currentInstallation.getJSONArray("friends");
        if (friendsList != null) {
            for (int i = 0; i < friendsList.length(); i++) {
                try {
                    if (newFriend.equals(friendsList.getString(i))) {
                        showToast(newFriend + " is already on your friends list");
                        return;
                    }
                } catch (JSONException e) {
                    Log.e("Main", e.getMessage());
                    showToast("Sorry, hit an error. Try again?");
                }
            }
        }

        checkName(newFriend, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean nameExists, ParseException e) {
                if (e == null) {
                    if (nameExists) {
                        // Update current installation friends list with new friend.
                        JSONArray friendsList = currentInstallation.getJSONArray("friends");
                        if (friendsList == null) friendsList = new JSONArray();
                        friendsList.put(newFriend);
                        currentInstallation.put("friends", friendsList);
                        currentInstallation.saveInBackground();

                        friendsAdapter.update(friendsList);
                    } else {
                        showToast("Yo, no one exists with that name!");
                    }
                } else {
                    Log.e("Main", e.getMessage());
                    showToast(e.getMessage());
                }
            }
        });
    }

    private void checkName(String name, FunctionCallback<Boolean> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        ParseCloud.callFunctionInBackground("checkName", params, callback);
    }

    @OnClick(R.id.location_get)
    public void onLocationGetClick(View view) {
        showToast("Getting location...");
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        String loc = "latitude: " + location.getLatitude() + "\n" +
                                "longitude: " + location.getLongitude();
                        locationText.setText(loc);
                    }
                });
    }

    class FriendsListAdapter extends BaseAdapter {

        private JSONArray friends;

        public FriendsListAdapter(JSONArray friends) {
            this.friends = friends;
        }

        public void update(JSONArray friends) {
            this.friends = friends;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return friends != null ? friends.length() : 0;
        }

        @Override
        public String getItem(int i) {
            try {
                return friends != null ? friends.getString(i) : null;
            } catch (JSONException e) {
                return null;
            }

        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new TextView(MainActivity.this);
            }

            final String friend = getItem(i);
            ((TextView) view).setText(friend);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showToast("poke!");
                    poke(friend);
                }
            });

            return view;
        }
    }
}
