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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

public class DestMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline polyline;
    private Marker placeMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest_map);

        // Using existing database
        if ( /*!checkDataBase()*/true) {
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
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
                    stationsLocations.add(new LatLng(station.lat, station.lng));
                }
                /*
                mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                        .width(5).color(Color.BLUE)
                        .visible(true));*/

                // Get Nearest Station from user location
                LatLng currentLocation = new LatLng(30.097987, 31.310127);
                Station nearestStationForUser = GetNearestStationFromGivenPlace(currentLocation, stationsList);
                DrawNavPathfromFinalStationToDest(new LatLng(nearestStationForUser.lat, nearestStationForUser.lng), currentLocation, mMap);

                // Get Nearest Station from destination place
                Station nearestStationForPlace = GetNearestStationFromGivenPlace(place.getLatLng(), stationsList);
                DrawNavPathfromFinalStationToDest(new LatLng(nearestStationForPlace.lat, nearestStationForPlace.lng), place.getLatLng(), mMap);

                // Get Stations Path based on selected Place
                DrawStationsPath(nearestStationForUser, nearestStationForPlace);

                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLocation, 12);
                mMap.animateCamera(update);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Stations Locations
        List<Station> stationsList = Station.listAll(Station.class);
        List<LatLng> stationsLocations = new ArrayList<LatLng>();

        for (Station station : stationsList ) {
            //mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
            stationsLocations.add(new LatLng(station.lat, station.lng));
        }

/*        mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                .width(5).color(Color.BLUE)
                .visible(true));*/

        // Get current location
        LatLng currentLocation = new LatLng(30.097987, 31.310127);
        Station nearestStation = GetNearestStationFromGivenPlace(currentLocation, stationsList);
        DrawNavPathfromFinalStationToDest(new LatLng(nearestStation.lat, nearestStation.lng), currentLocation, mMap);
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

    public Station GetNearestStationFromGivenPlace(LatLng givenPlace, List<Station> stationsList){
        List<LatLng> stationsLocations = new ArrayList<LatLng>();

        for (Station station : stationsList ) {
            //mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
            stationsLocations.add(new LatLng(station.lat, station.lng));
        }

/*        mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                .width(5).color(Color.BLUE)
                .visible(true));*/

        LatLng nearestStation = null;
        double nearestDistance = 10000000000000.0;
        int index = -1;
        int nearestStationIndex = 0;
        for (LatLng currentStationLocation : stationsLocations){
            index++;
            double userStationDistance = Helpers.CalculationByDistance(givenPlace, currentStationLocation);
            if (userStationDistance < nearestDistance){
                nearestDistance = userStationDistance;
                nearestStation = currentStationLocation;
                nearestStationIndex = index;
            }
        }
        return stationsList.get(nearestStationIndex);
    }

    public void DrawStationsPath(Station startStation, Station finalStation){
        // Get Stations Path
        List<Station> stationsPath = GetStationsPath(startStation, finalStation);
        List<LatLng> stationsLocations = new ArrayList<LatLng>();

        for (Station station : stationsPath ) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(station.lat, station.lng)).title(station.title));
            stationsLocations.add(new LatLng(station.lat, station.lng));
        }

        mMap.addPolyline((new PolylineOptions()).addAll(stationsLocations)
                .width(5).color(Color.YELLOW)
                .visible(true));
    }

    private List<Station> GetStationsPath(Station currentStation, Station finalStation){
        List<Station> stationsPath = new ArrayList<Station>();
        Queue<StationNode> stationNodesQueue = new LinkedList<StationNode>();
        StationNode currentNode = new StationNode(currentStation);
        List<StationNode> visitedStationNodes = new ArrayList<StationNode>();

        currentNode.rootStations.add(currentNode);
        stationNodesQueue.add(currentNode);

        while(!stationNodesQueue.isEmpty()){
            // remove from queue the station
            currentNode = (StationNode) stationNodesQueue.poll();
            List<StationNode> stationNodesLinks = new ArrayList<StationNode>();

            // Check if current station is the final station
            if (currentNode.station.getId() == finalStation.getId()){
                currentNode.addRootStation(currentNode);
                break;
            }

            // Check if it has been visited before
            if(!IsVisited(currentNode, visitedStationNodes)) {
                // Get links of current station
                List<Station> stationLinks = GetStationLinksByStationID(currentNode.station.getId());
                // Convert station links to nodes and put them with root path
                for (Station currentStationLink : stationLinks) {
                    Log.i("Station Links", "station :" + currentNode.station.title + "," + "Link : " + currentStationLink.title);
                    StationNode tempNode = new StationNode(currentStationLink);
                    tempNode.rootStations.addAll(currentNode.rootStations);
                    tempNode.rootStations.add(currentNode);
                    stationNodesLinks.add(tempNode);
                }
                // Enqueue the current station child nodes
                stationNodesQueue.addAll(stationNodesLinks);
                // add current node as visited node in visited nodes array
                visitedStationNodes.add(currentNode);
            }
        }
        // Convert nodes to stations
        for (StationNode currentStationNode : currentNode.rootStations) {
            stationsPath.add(currentStationNode.station);
        }
        return stationsPath;
    }

    protected  boolean IsVisited(StationNode stationNode, List<StationNode> visitedStationNodes){
        boolean IsVisited = false;
        for (StationNode currentStationNode : visitedStationNodes) {
            if (currentStationNode.station.getId() == stationNode.station.getId()){
                IsVisited = true;
                break;
            }
        }
        return IsVisited;
    }

    protected  List<Station> GetStationLinksByStationID(Long stationID){
        List<Station> stationLinks = Station.findWithQuery(Station.class, "select links.* from Station s\n" +
                                                                        "inner join Station_Link sl on s.ID = sl.Station_ID\n" +
                                                                        "inner join Station links on links.ID = sl.Link_ID\n" +
                                                                        "where s.ID = ?", stationID.toString());;
        return stationLinks;
    }

    protected  List<Station> GetStationLinksExludePreviousByStationID(Long stationID, Long previousStationID){
        List<Station> stationLinks = Station.findWithQuery(Station.class, "select links.* from Station s\n" +
                                                                        "inner join Station_Link sl on s.ID = sl.Station_ID\n" +
                                                                        "inner join Station links on links.ID = sl.Link_ID\n" +
                                                                        "where s.ID = ? and sl.Link_ID  <> ?", stationID.toString(), previousStationID.toString());;
        return stationLinks;
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
            Log.e("Database", "database not found: " + e.getMessage() );

        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }




}
