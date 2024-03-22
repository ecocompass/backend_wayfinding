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
    private double distance;
    private List<double[]> traceCoordinates;
    private List<Long> waitTime;

    public FoundSolution(PossibleSolution solution, String route, double distance, List<double[]> trace, List<Long> time) {
        this.possibleSolution = solution;
        this.route = route;
        this.distance = distance;
        this.traceCoordinates = trace;
        this.waitTime = time;
    }

    public FoundSolution() {
        this.possibleSolution = new PossibleSolution();
        this.route = null;
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

    public void setPossibleSolution(PossibleSolution possibleSolution){
        this.possibleSolution = possibleSolution;
    }

    public void setRoute(String route){
        this.route = route;
    }

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
