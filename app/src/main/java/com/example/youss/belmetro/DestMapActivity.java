package com.example.youss.belmetro;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class DestMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline polyline;
    private Marker placeMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Remove last place rendered
                if (polyline != null) {
                    polyline.remove();
                }

                if( placeMarker != null){
                        placeMarker.remove();
                }

                Log.i("", "Place: " + place.getName());
                placeMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));

                // Get Nearest Station from user location
                LatLng currentLocation = new LatLng(30.097987, 31.310127);
                LatLng nearestStationForUser = GetNearestStationFromGivenPlace(currentLocation);
                DrawNavPathfromFinalStationToDest(nearestStationForUser, currentLocation, mMap);

                // Get Nearest Station from destination place
                LatLng nearestStationForPlace = GetNearestStationFromGivenPlace(place.getLatLng());
                DrawNavPathfromFinalStationToDest(nearestStationForPlace, place.getLatLng(), mMap);





                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(nearestStationForUser, 12);
                mMap.animateCamera(update);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }
        });
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

        // Get current location
        LatLng currentLocation = new LatLng(30.097987, 31.310127);
        LatLng nearestStation = GetNearestStationFromGivenPlace(currentLocation);
        DrawNavPathfromFinalStationToDest(nearestStation, currentLocation, mMap);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLocation, 12);
        mMap.animateCamera(update);

    }

    public void DrawNavPathfromFinalStationToDest(LatLng finalStation, LatLng finalPlace, final GoogleMap mMap){
        String serverKey = "AIzaSyCFR2gn7gEg-fLQAvvC5MMylBL3DisFz8o";
        final LatLng origin = finalStation;
        LatLng destination = finalPlace;
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
                        polyline =  mMap.addPolyline(polylineOptions);
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                    }
                });

    }

    public LatLng GetNearestStationFromGivenPlace(LatLng givenPlace){
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

        LatLng nearestStation = null;
        double nearestDistance = 10000000000000.0;

        for (LatLng currentStationLocation : stationsList){
            double userStationDistance = Helpers.CalculationByDistance(givenPlace, currentStationLocation);
            if (userStationDistance < nearestDistance){
                nearestDistance = userStationDistance;
                nearestStation = currentStationLocation;
            }
        }

        return nearestStation;
    }

}
