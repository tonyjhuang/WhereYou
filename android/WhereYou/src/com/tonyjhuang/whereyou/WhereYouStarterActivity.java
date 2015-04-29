package com.tonyjhuang.whereyou;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class WhereYouStarterActivity extends Activity {

	@InjectView(R.id.hello)
	TextView helloText;
	@InjectView(R.id.username_input)
	EditText usernameInput;
	@InjectView(R.id.target_input)
	EditText targetInput;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());
		ButterKnife.inject(this);

		ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
		setNameView(currentInstallation.getString("name"));
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

	@OnClick(R.id.target_push)
	public void onTargetPushClick(View view) {
		String targetName = targetInput.getText().toString();
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereEqualTo("name", targetName);

		ParsePush push = new ParsePush();
		push.setQuery(pushQuery);
		push.setMessage("Giants scored against the A's! It's now 2-2.");
		push.sendInBackground();
	}
}
