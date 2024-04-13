package org.ecocompass.core.util;

public class DistanceUtility {

    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static double getCaloriesBurned(double distance, String mode) {
        double averageWeight = 89.2;
        double duration = distance / Constants.AVERAGE_SPEEDS.get(mode) * 60;
        double kcalPerMinute = Constants.CALORIES_MAPPINGS.get(mode) * averageWeight * 3.5 * Math.pow(10, -3);
        return duration * kcalPerMinute;
    }

    public static double getCarbonEmissions(double distance, String mode) {
        return distance * Constants.EMISSION_MAPPINGS.get(mode);
    }

}
