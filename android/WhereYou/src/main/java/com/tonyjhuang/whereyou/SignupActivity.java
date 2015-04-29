package com.tonyjhuang.whereyou;

import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseAnalytics;

/**
 * Created by tony on 4/29/15.
 */
public class SignupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}
