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
    private List<double[]> pathPointList;
    double distance;

    public PathWithMode(){
        this.mode = "";
        this.pathPointList = new ArrayList<>();
        this.distance = 0;
    }

    public void setMode(String mode){
        this.mode = mode;
    }
    public void setPathPointList(List<double[]> pathPointList){
        this.pathPointList = pathPointList;
    }

    public void setDistance (double distance) {
        this.distance = distance;
    }

    public String getMode(){
        return this.mode;
    }

    public List<double[]> getPathPointList(){
        return this.pathPointList;
    }

    public double getDistance () {
        return this.distance;
    }
}

