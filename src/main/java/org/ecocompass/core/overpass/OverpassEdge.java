package org.ecocompass.core.overpass;

import java.util.HashSet;

public class OverpassEdge {
    private final HashSet<String> allowedTypes = new HashSet<String>() {{
        add("dart");
        add("luas");
        add("road");
        add("walk");
        add("cycle");
    }};
    final String type;
    public OverpassNode start;
    public OverpassNode end;

    public double geoDistance;

    public OverpassEdge(OverpassNode start, OverpassNode end, String type) {
        this.start = start;
        this.end = end;

        if (!allowedTypes.contains(type)) {
            throw new RuntimeException("Invalid Edge type '" + type + "'");
        } else {
            this.type = type;
        }

         calculateDistance();
    }

    private void calculateDistance() {
        double latDistance = Math.toRadians(end.latitude - start.latitude);
        double lonDistance = Math.toRadians(end.longitude - start.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        geoDistance = 6371 * c;
    }
}