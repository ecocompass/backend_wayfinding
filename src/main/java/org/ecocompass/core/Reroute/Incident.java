package org.ecocompass.core.Reroute;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Incident {
    private double[] coordinates;
    private String description;
    private boolean isJamcident;
    private boolean roadClosed;

    public double[] getCoordinates() {
        return coordinates;
    }

    // Setter for coordinates
    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsJamcident() {
        return isJamcident;
    }

    public void setIsJamcident(boolean isJamcident) {
        this.isJamcident = isJamcident;
    }

    public boolean getRoadClosed() {
        return roadClosed;
    }

    public void setRoadClosed(boolean roadClosed) {
        this.roadClosed = roadClosed;
    }
}

