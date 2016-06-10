package com.example.youss.belmetro;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        // Using existing database
        if ( !checkDataBase()) {
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

                // Stations Locations
                List<Station> stationsList = Station.listAll(Station.class);
                List<LatLng> stationsLocations = new ArrayList<LatLng>();

                for (Station station : stationsList ) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
                    stationsLocations.add(new LatLng(station.lat, station.lng));
                }

                mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                        .width(6).color(Color.BLUE)
                        .visible(true));

                // Get Nearest Station from user location
                LatLng currentLocation = new LatLng(30.097987, 31.310127);
                LatLng nearestStationForUser = GetNearestStationFromGivenPlace(currentLocation, stationsList);
                DrawNavPathfromFinalStationToDest(nearestStationForUser, currentLocation, mMap);

                // Get Nearest Station from destination place
                LatLng nearestStationForPlace = GetNearestStationFromGivenPlace(place.getLatLng(), stationsList);
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
        List<Station> stationsList = Station.listAll(Station.class);
        List<LatLng> stationsLocations = new ArrayList<LatLng>();

        for (Station station : stationsList ) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
            stationsLocations.add(new LatLng(station.lat, station.lng));
        }

        mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                .width(6).color(Color.BLUE)
                .visible(true));

        // Get current location
        LatLng currentLocation = new LatLng(30.097987, 31.310127);
        LatLng nearestStation = GetNearestStationFromGivenPlace(currentLocation, stationsList);
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

    public LatLng GetNearestStationFromGivenPlace(LatLng givenPlace, List<Station> stationsList){
        List<LatLng> stationsLocations = new ArrayList<LatLng>();

        for (Station station : stationsList ) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
            stationsLocations.add(new LatLng(station.lat, station.lng));
        }

        mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                .width(6).color(Color.BLUE)
                .visible(true));

        LatLng nearestStation = null;
        double nearestDistance = 10000000000000.0;

        for (LatLng currentStationLocation : stationsLocations){
            double userStationDistance = Helpers.CalculationByDistance(givenPlace, currentStationLocation);
            if (userStationDistance < nearestDistance){
                nearestDistance = userStationDistance;
                nearestStation = currentStationLocation;
            }
        }

        return nearestStation;
    }

    protected void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = getApplicationContext().getAssets().open("belmetro.db");

        // Path to the just created empty db
        String outFileName = "/data/data/com.example.youss.belmetro/databases/" + "belmetro.db";

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    protected boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = "/data/data/com.example.youss.belmetro/databases/" + "belmetro.db";
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }




}
