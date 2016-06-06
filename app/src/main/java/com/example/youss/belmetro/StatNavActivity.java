package com.example.youss.belmetro;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/*import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
*/
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class StatNavActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
/*
        String serverKey = "AIzaSyCFR2gn7gEg-fLQAvvC5MMylBL3DisFz8o";
        final LatLng origin = new LatLng(30.097959, 31.310031);
        LatLng destination = new LatLng(30.097719, 31.304478);
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.WALKING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        // Do something here
                        Log.i("success", direction.getStatus());

                        Route route = direction.getRouteList().get(0);
                        Leg leg = route.getLegList().get(0);
                        ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                        PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                        mMap.addPolyline(polylineOptions);

                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(origin, 17);
                        mMap.animateCamera(update);

                        // Stations Locations
                        LatLng sarayobba = new LatLng(30.097705, 31.304484);
                        LatLng hamamatobba = new LatLng(30.091263, 31.298905);
                        LatLng kobriobba = new LatLng(30.087230, 31.294136);

                        ArrayList<LatLng> stationsList = new ArrayList<LatLng>();
                        stationsList.add(sarayobba);
                        stationsList.add(hamamatobba);
                        stationsList.add(kobriobba);


                        mMap.addMarker(new MarkerOptions().position(sarayobba).title("sarayobba"));
                        mMap.addMarker(new MarkerOptions().position(hamamatobba).title("hamamatobba"));
                        mMap.addMarker(new MarkerOptions().position(kobriobba).title("kobriobba"));

                        mMap.addPolyline((new PolylineOptions())
                                .add(sarayobba, hamamatobba, kobriobba).width(6).color(Color.BLUE)
                                .visible(true));
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                    }
                });
                */
    }
}
