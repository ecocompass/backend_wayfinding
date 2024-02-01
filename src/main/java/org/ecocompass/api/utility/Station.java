package org.ecocompass.api.utility;

public class Station {
    String stationDesc;
    double latitude;
    double longitude;
    double distance; // To store the calculated distance

    public Station(String stationDesc, double latitude, double longitude) {
        this.stationDesc = stationDesc;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStationDesc() {
        return stationDesc;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
