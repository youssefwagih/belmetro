package com.example.youss.belmetro;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // User Location
        LatLng currentLocation = new LatLng(30.089720, 31.298159);

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

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(sarayobba, 12);
        mMap.animateCamera(update);
        mMap.addPolyline((new PolylineOptions())
                .add(sarayobba, hamamatobba, kobriobba).width(6).color(Color.BLUE)
                .visible(true));

        // Get Nearest Station from user location
        LatLng nearestStation = null;
        double nearestDistance = 10000000000000.0;
        for (LatLng currentStationLocation : stationsList){
            double userStationDistance = Helpers.CalculationByDistance(currentLocation, currentStationLocation);
            if (userStationDistance < nearestDistance){
                nearestDistance = userStationDistance;
                nearestStation = currentStationLocation;
            }
        }

        mMap.addPolyline((new PolylineOptions())
                .add(currentLocation, nearestStation).width(6).color(Color.GREEN)
                .visible(true));

    }
}
