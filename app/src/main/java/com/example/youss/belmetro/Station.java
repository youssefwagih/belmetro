package com.example.youss.belmetro;

//import com.orm.SugarRecord;

import com.orm.SugarRecord;

/**
 * Created by youss on 14-May-16.
 */
public class Station extends SugarRecord {
    String title;
    double lng;
    double lat;
    int station_line;

    public Station(){
    }

    public Station(String title, double lat, double lng,  int station_line){
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.station_line = station_line;
    }
}
