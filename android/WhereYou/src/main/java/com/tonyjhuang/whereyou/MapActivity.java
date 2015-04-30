package com.tonyjhuang.whereyou;

import android.os.Bundle;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParseAnalytics;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

/**
 * Created by tony on 4/30/15.
 */
public class MapActivity extends WhereYouActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        IntentLogger.dump("Map", getIntent());
    }
}
