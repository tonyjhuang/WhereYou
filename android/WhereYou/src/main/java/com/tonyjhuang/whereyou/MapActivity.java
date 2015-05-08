package com.tonyjhuang.whereyou;

import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import com.tonyjhuang.whereyou.api.ParseHelper;
import com.tonyjhuang.whereyou.helpers.StreetAddress;
import com.tonyjhuang.whereyou.helpers.WhereYouActivity;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.InjectView;

/**
 * Created by tony on 4/30/15.
 */
public class MapActivity extends WhereYouActivity implements OnMapReadyCallback {

    private static final int ZOOM_LEVEL = 14;

    @InjectView(R.id.name_container)
    FrameLayout nameContainer;
    @InjectView(R.id.name)
    TextView nameView;
    @InjectView(R.id.address_container)
    RelativeLayout addressContainer;
    @InjectView(R.id.address)
    TextView addressView;

    private GoogleMap googleMap;
    private LocationInfo locationInfo;
    private Marker currentLocationMarker;
    private Circle currentLocationAccuracy;
    private Vibrator vibrator;
    private ParseHelper parseHelper = new ParseHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        IntentLogger.dump("Map", getIntent());
        try {
            JSONObject json = new JSONObject(getIntent().getExtras().getString("com.parse.Data"));
            final String name = json.getString("name");
            double lat = json.getDouble("lat");
            double lng = json.getDouble("lng");
            double acc = json.getDouble("acc");
            locationInfo = new LocationInfo(lat, lng, acc);

            nameView.setText(name);
            nameContainer.setBackgroundColor(ColorPicker.getColor(name));
            addressContainer.setBackgroundColor(ColorPicker.getColor(name));
            nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parseHelper.poke(name);
                    vibrator.vibrate(25);
                    YoYo.with(Techniques.Swing)
                            .duration(150)
                            .playOn(nameView);
                }
            });


            String formattedAddress = StreetAddress.getStreetAddress(this, lat, lng, true);
            if (formattedAddress != null) {
                addressView.setText(formattedAddress);
            } else {
                addressContainer.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            Log.e("MapActivity", e.getMessage());
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
            currentLocationMarker.showInfoWindow();

            // accuracy circle
            CircleOptions circleOptions = new CircleOptions()
                    .center(currentLocation)
                    .radius(locationInfo.acc)
                    .strokeColor(setTransparency(getResources().getColor(R.color.blue), 150))
                    .strokeWidth(5f)
                    .fillColor(setTransparency(getResources().getColor(R.color.cyan), 100));
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
