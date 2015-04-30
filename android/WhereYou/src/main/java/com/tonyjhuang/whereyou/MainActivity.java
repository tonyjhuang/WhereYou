package com.tonyjhuang.whereyou;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

    @InjectView(R.id.hello)
    TextView helloText;
    @InjectView(R.id.username_input)
    EditText usernameInput;
    @InjectView(R.id.friend_input)
    EditText friendInput;
    @InjectView(R.id.friends)
    FriendsListView friendsListView;

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

            setNameView(name);
            friendsListView.setFriends(currentInstallation.getJSONArray("friends"));
        }
    }

    @OnClick(R.id.username_save)
    public void onUsernameSaveClick(View view) {
        final String newName = usernameInput.getText().toString();
        setNameView(newName);
        parseHelper.updateName(newName);
    }

    private void setNameView(String name) {
        helloText.setText("Hello " + name + "!");
    }


    @OnClick(R.id.friend_add)
    public void onFriendAddClick(View view) {
        final String newFriend = friendInput.getText().toString();
        JSONArray friendsList = ParseInstallation.getCurrentInstallation().getJSONArray("friends");

        // Does the user already have this guy on his friends list?
        if (friendsList != null) {
            for (int i = 0; i < friendsList.length(); i++) {
                try {
                    if (newFriend.equals(friendsList.getString(i))) {
                        showToast(newFriend + " is already on your friends list");
                        return;
                    }
                } catch (JSONException e) {
                    Log.e("Main", e.getMessage());
                }
            }
        }

        parseHelper.addFriend(newFriend, new ParseHelper.Callback<JSONArray>() {
            @Override
            public void onFinish(JSONArray friendsList) {
                friendsListView.setFriends(friendsList);
            }

            @Override
            public void onError(Throwable e) {
                if (e.getMessage().equals("No such user")) {
                    showToast("Yo, no one exists with that name!");
                } else {
                    Log.e("Main", e.getMessage());
                    showToast("UHHHHH SOMETHING WENT WRONG. TRY AGAIN?????");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!friendsListView.onBackPressed())
            super.onBackPressed();
    }
}
