package org.ecocompass.core.overpass;

import java.util.ArrayList;
import java.util.HashSet;

public class OverpassNode {
    private final HashSet<String> allowedTypes = new HashSet<String>() {{
        add("dart");
        add("luas");
        add("road");
        add("walk");
        add("cycle");
    }};

    public boolean isDart;
    public boolean isLuas;
    public boolean isRoad;
    public boolean isWalk;
    public boolean isCycle;
    public final double latitude;
    public final double longitude;

    public OverpassNode(double latitude, double longitude, ArrayList<String> types) {
        this.latitude = latitude;
        this.longitude = longitude;

        for (String type : types) {
            if (!allowedTypes.contains(type)) {
                throw new RuntimeException("Invalid Node type '" + type + "'");
            } else {
                switch (type) {
                    case "dart": {
                        isDart = true;
                    } break;
                    case "luas": {
                        isLuas = true;
                    } break;
                    case "road": {
                        isRoad = true;
                    } break;
                    case "walk": {
                        isWalk = true;
                    } break;
                    case "cycle": {
                        isCycle = true;
                    } break;
                }
            }
        }
    }
}
