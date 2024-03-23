package org.ecocompass.api.utility;

import lombok.Getter;
import lombok.Setter;
import org.ecocompass.core.util.FoundSolution;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PathWithMode {
    private String mode;
    private String startStopName;
    private String endStopName;
    private String modeNumber;
    private String routeNumber;
    private Long timeStamp;
    private List<double[]> pathPointList;
    private double distance;

    public PathWithMode(){
        this.mode = "";
        this.startStopName = "";
        this.endStopName = "";
        this.modeNumber = "";
        this.routeNumber = "";
        this.timeStamp = 0L;
        this.pathPointList = new ArrayList<>();
        this.distance = 0;
    }

    public void setMode(String mode){
        this.mode = mode;
    }

    public void setStartStopName(String startStopName) {this.startStopName = startStopName;}

    public void setEndStopName(String endStopName) {this.endStopName = endStopName;}

    public void setModeNumber(String modeNumber) {this.modeNumber = modeNumber;}

    public void setRouteNumber(String routeNumber) {this.routeNumber = routeNumber;}

    public void setTimeStamp(Long timeStamp) {this.timeStamp = timeStamp;}

    public void setPathPointList(List<double[]> pathPointList){this.pathPointList = pathPointList;}

    public void setDistance (double distance) {this.distance = distance;}

    public String getMode(){
        return this.mode;
    }

    public String getStartStopName() { return this.startStopName;}

    public String getEndStopName() { return this.endStopName;}

    public String getModeNumber() { return this.modeNumber; }

    public String getRouteNumber() { return this.routeNumber; }

    public Long getTimeStamp() { return this.timeStamp; }

    public List<double[]> getPathPointList(){
        return this.pathPointList;
    }

    public double getDistance () {
        return this.distance;
    }
}

