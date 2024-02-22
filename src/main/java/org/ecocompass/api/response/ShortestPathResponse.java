package org.ecocompass.api.response;

import java.util.List;

public class ShortestPathResponse {
    private List<double[]> shortestPathCoordinates;

    public ShortestPathResponse(List<double[]> shortestPathCoordinates) {
        this.shortestPathCoordinates = shortestPathCoordinates;
    }

    public List<double[]> getShortestPathCoordinates() {
        return shortestPathCoordinates;
    }

    public void setShortestPathCoordinates(List<double[]> shortestPathCoordinates) {
        this.shortestPathCoordinates = shortestPathCoordinates;
    }
}
