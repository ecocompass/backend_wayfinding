package org.ecocompass.core.util;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FoundSolution {
    private PossibleSolution possibleSolution;
    private String route;
    private String modeNumber;
    private double distance;
    private List<double[]> traceCoordinates;
    private List<Long> waitTime;

    public FoundSolution(PossibleSolution solution, String route, String modeNumber,
                         double distance, List<double[]> trace, List<Long> waitTime) {
        this.possibleSolution = solution;
        this.route = route;
        this.modeNumber = modeNumber;
        this.distance = distance;
        this.traceCoordinates = trace;
        this.waitTime = waitTime;
    }

    public FoundSolution() {
        this.possibleSolution = new PossibleSolution();
        this.route = null;
        this.modeNumber = "";
        this.distance = 0;
        this.traceCoordinates = new ArrayList<>();
        this.waitTime = new ArrayList<>();
    }

    public double getDistance(){
        return this.distance;
    }

    public PossibleSolution getPossibleSolution(){
        return this.possibleSolution;
    }

    public List<double[]> getTraceCoordinates(){
        return this.traceCoordinates;
    }

    public String getModeNumber() {
        return this.modeNumber;
    }

    public String getRoute() {
        return this.route;
    }

    public List<Long> getWaitTime(){
        return this.waitTime;
    }

    public void setPossibleSolution(PossibleSolution possibleSolution){
        this.possibleSolution = possibleSolution;
    }

    public void setRoute(String route){
        this.route = route;
    }

    public void setModeNumber(String modeNumber) { this.modeNumber = modeNumber;}

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setTraceCoordinates(List<double[]> traceCoordinates) {
        this.traceCoordinates = traceCoordinates;
    }

    public void setWaitTime(List<Long> waitTime) {
        this.waitTime = waitTime;
    }

}
