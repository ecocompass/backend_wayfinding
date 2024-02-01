package org.ecocompass.api.utility;

public class Coordinates {
    private double latitude;
    private double longitude;

    public Coordinates(double lat, double longs) {
        latitude = lat;
        longitude = longs;
    }

    // getters and setters

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
