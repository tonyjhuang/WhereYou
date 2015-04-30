package com.tonyjhuang.whereyou;

import android.os.Bundle;
import android.widget.TextView;

import com.drivemode.intentlog.IntentLogger;
import com.parse.ParseAnalytics;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.InjectView;

/**
 * Created by tony on 4/30/15.
 */
public class MapActivity extends WhereYouActivity {

    @InjectView(R.id.name)
    TextView nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        IntentLogger.dump("Map", getIntent());
        try {
            JSONObject json = new JSONObject(getIntent().getExtras().getString("com.parse.Data"));
            String name = json.getString("name");
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            double acc = json.getDouble("acc");
            nameView.setText(name);
        } catch (JSONException e) {

        }
    }
}
