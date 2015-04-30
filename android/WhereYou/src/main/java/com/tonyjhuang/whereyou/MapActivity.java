package com.tonyjhuang.whereyou;

import android.os.Bundle;
import android.widget.TextView;

import com.drivemode.intentlog.IntentLogger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseAnalytics;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.InjectView;

/**
 * Created by tony on 4/30/15.
 */
public class MapActivity extends WhereYouActivity implements OnMapReadyCallback{

    @InjectView(R.id.name)
    TextView nameView;

    private GoogleMap googleMap;
    private LocationInfo locationInfo;

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
            locationInfo = new LocationInfo(lat, lng, acc);
            nameView.setText(name);
        } catch (JSONException e) {

        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if(locationInfo != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationInfo.getLatLng(), 16));
        }
    }

    private static class LocationInfo {
        double lat;
        double lng;
        double acc;

        public LocationInfo(double lat, double lng, double acc) {
            this.lat = lat;
            this.lng = lng;
            this.acc = acc;
        }

        public LatLng getLatLng() {
            return new LatLng(lat, lng);
        }
    }
}
