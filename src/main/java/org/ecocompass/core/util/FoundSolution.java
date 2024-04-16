package org.ecocompass.core.util;

import lombok.Getter;
import lombok.Setter;
import org.ecocompass.api.utility.Traffic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class FoundSolution {
    private PossibleSolution possibleSolution;
    private String route;
    private String modeNumber;
    private double distance;
    private List<double[]> traceCoordinates;
    private List<Long> waitTime;
    private Set<Traffic> traffic;

    public FoundSolution() {
        this.possibleSolution = new PossibleSolution();
        this.route = null;
        this.modeNumber = "";
        this.distance = 0;
        this.traceCoordinates = new ArrayList<>();
        this.waitTime = new ArrayList<>();
        this.traffic = new HashSet<>();
    }

    public void addTraffic(Traffic traffic) {
        for(Traffic addedtraffic: this.traffic){
            if(addedtraffic.equals(traffic)){
                return;
            }
        }
        this.traffic.add(traffic);
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

    public void addWaitTimeOffset(Long timeOffset){
        this.waitTime.replaceAll(aLong -> aLong + timeOffset);
    }

    public Set<Traffic> getTraffic() { return this.traffic; }

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

    public void setTraffic(Set<Traffic> traffic) { this.traffic = traffic; }

}
