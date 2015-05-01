package com.tonyjhuang.whereyou;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.tonyjhuang.whereyou.api.ParseHelper;
import com.tonyjhuang.whereyou.helpers.AppRouter;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends WhereYouActivity {

    @InjectView(R.id.empty_container)
    LinearLayout emptyContainer;
    @InjectView(R.id.taunt)
    TextView taunt;
    @InjectView(R.id.friends)
    FriendsListView friendsListView;
    @InjectView(R.id.username)
    TextView username;

    private ParseHelper parseHelper = new ParseHelper();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Main", "HELLO WORLD! **************************************************");
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        String name = currentInstallation.getString("name");
        if (name == null) {
            AppRouter.redirectTo(this, SignupActivity.class);
        } else {
            setContentView(R.layout.activity_main);
            username.setText("Hello, " + name + ".");

            JSONArray friends = currentInstallation.getJSONArray("friends");
            friendsListView.setFriends(friends);

            if (friends != null && friends.length() > 0) {
                emptyContainer.setVisibility(View.GONE);
            } else {
                taunt.setText("...where are all of your friends, " + name + "?");
            }
        }
    }

    @OnClick(R.id.empty_container)
    public void onEmptyContainerClick(View view) {
        if(!friendsListView.onBackPressed())
            friendsListView.shakeAddFooter();
    }

    private boolean addFriendLock = false;

    public void addFriend(String name, final ParseHelper.Callback<Boolean> callback) {
        if (addFriendLock) return;
        addFriendLock = true;

        JSONArray friendsList = ParseInstallation.getCurrentInstallation().getJSONArray("friends");

        // Does the user already have this guy on his friends list?
        if (friendsList != null) {
            for (int i = 0; i < friendsList.length(); i++) {
                try {
                    if (name.equals(friendsList.getString(i))) {
                        showToast(name + " is already on your friends list");
                        if (callback != null) callback.onFinish(false);
                        addFriendLock = false;
                        return;
                    }
                } catch (JSONException e) {
                    Log.e("Main", e.getMessage());
                }
            }
        }

        parseHelper.addFriend(name, new ParseHelper.Callback<JSONArray>() {
            @Override
            public void onFinish(JSONArray friendsList) {
                addFriendLock = false;
                friendsListView.setFriends(friendsList);
                if (callback != null) callback.onFinish(true);
            }

            @Override
            public void onError(Throwable e) {
                addFriendLock = false;
                if (e.getMessage().equals("No such user")) {
                    showToast("Yo, no one exists with that name!");
                } else {
                    Log.e("Main", e.getMessage());
                    showToast("UHHHHH SOMETHING WENT WRONG. TRY AGAIN?????");
                }
                if (callback != null) callback.onFinish(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!friendsListView.onBackPressed())
            super.onBackPressed();
    }
}
