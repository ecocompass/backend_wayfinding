package org.ecocompass.core.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransitRoute {
    private String mode;
    private FoundSolution foundSolution;
    private List<double[]> pathListStart;
    private List<double[]> pathListEnd;
    private double distanceStart;
    private double distanceEnd;

    // Getter methods
    public String getMode() {
        return mode;
    }

    public FoundSolution getFoundSolution() {
        return foundSolution;
    }

    public List<double[]> getPathListStart() {
        return pathListStart;
    }

    public List<double[]> getPathListEnd() {
        return pathListEnd;
    }

    public double getDistanceStart() {
        return distanceStart;
    }

    public double getDistanceEnd() {
        return distanceEnd;
    }

    // Setter methods
    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setFoundSolution(FoundSolution foundSolution) {
        this.foundSolution = foundSolution;
    }

    public void setPathListStart(List<double[]> pathListStart) {
        this.pathListStart = pathListStart;
    }

    public void setPathListEnd(List<double[]> pathListEnd) {
        this.pathListEnd = pathListEnd;
    }

    public void setDistanceStart(double distanceStart) {
        this.distanceStart = distanceStart;
    }

    public void setDistanceEnd(double distanceEnd) {
        this.distanceEnd = distanceEnd;
    }
}
