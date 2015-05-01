package com.tonyjhuang.whereyou;

import android.os.Bundle;
import android.widget.TextView;

import com.drivemode.intentlog.IntentLogger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseAnalytics;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.InjectView;

/**
 * Created by tony on 4/30/15.
 */
public class MapActivity extends WhereYouActivity implements OnMapReadyCallback {

    private static final int ZOOM_LEVEL = 16;

    @InjectView(R.id.name)
    TextView nameView;

    private GoogleMap googleMap;
    private LocationInfo locationInfo;
    private Marker currentLocationMarker;
    private Circle currentLocationAccuracy;

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

        if (locationInfo != null) {
            LatLng currentLocation = locationInfo.getLatLng();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, ZOOM_LEVEL));
            currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)));

            // accuracy circle
            CircleOptions circleOptions = new CircleOptions()
                    .center(currentLocation)
                    .radius(locationInfo.acc)
                    .strokeColor(setTransparency(getResources().getColor(R.color.white), 100))
                    .strokeWidth(5f)
                    .fillColor(setTransparency(getResources().getColor(R.color.amber), 100));
            currentLocationAccuracy = googleMap.addCircle(circleOptions);
        }
    }

    private int setTransparency(int color, int transparency) {
        return (transparency << 24) + (color & 0x00FFFFFF);
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
