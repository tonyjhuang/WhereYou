package com.tonyjhuang.whereyou;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
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


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());
		ButterKnife.inject(this);

		ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
		setNameView(currentInstallation.getString("name"));
		ParseQuery<ParseInstallation> query = new ParseQuery<>("_Installation");
		query.findInBackground(new FindCallback<ParseInstallation>() {
			public void done(List<ParseInstallation> installations, ParseException e) {
				if (e == null) {
					Log.d("score", "Retrieved " + installations.size() + " results");
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
	}

	@OnClick(R.id.username_save)
	public void onUsernameSaveClick(View view) {
		String newName = usernameInput.getText().toString();
		setNameView(newName);
		ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
		currentInstallation.put("name", newName);
		currentInstallation.saveInBackground();

	}

	private void setNameView(String name) {
		helloText.setText("Hello " + name + "!");
	}
}
