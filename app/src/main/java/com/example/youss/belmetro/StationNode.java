package com.example.youss.belmetro;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by youss on 14-Jun-16.
 */
public class StationNode {
    Station station;
    boolean IsVisited;
    List<StationNode> rootStations;

    public StationNode(Station station){
        this.station = station;
        IsVisited = false;
        rootStations = new ArrayList<StationNode>();
    }

    public void setStation(Station station){
        this.station = station;
    }

    public void setVisited(boolean isVisited){
        this.IsVisited = isVisited;
    }

    public List<StationNode> getRootStations(){
        return rootStations;
    }

    public  void addRootStation(StationNode rootStationNode){
        rootStations.add(rootStationNode);
    }
}
