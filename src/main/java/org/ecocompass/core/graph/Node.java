package org.ecocompass.core.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Node {
    private static final Logger logger = LogManager.getLogger(Node.class);
    public final double latitude;
    public final double longitude;
    boolean busStation;
    boolean dartStation;
    boolean luasStation;
    boolean road;
    boolean bikeStation;
    Map<String, List<Neighbor>> neighbors;

    private final HashSet<String> allowedTypes = new HashSet<String>() {{
        add("dart");
        add("luas");
        add("road");
        add("bus");
    }};

    public Node(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.busStation = false;
        this.dartStation = false;
        this.luasStation = false;
        this.road = false;
        this.bikeStation = false;

        this.neighbors = new HashMap<>();
        this.neighbors.put("road", new java.util.ArrayList<>());
        this.neighbors.put("bus", new java.util.ArrayList<>());
        this.neighbors.put("luas", new java.util.ArrayList<>());
        this.neighbors.put("dart", new java.util.ArrayList<>());
    }

    public void addNeighbor(String type, Long nodeID, Double weight) throws Exception {
        List<Neighbor> neighbors = this.neighbors.get(type);
        // Only add neighbor if type is correct
        if (neighbors != null) {
            neighbors.add(new Neighbor(nodeID, weight));
        } else {
            throw new Exception("Invalid neighbor type '" + type + "'");
        }
    }

    void setStationType(String type) throws Exception {
        if (!allowedTypes.contains(type)) {
            throw new Exception("Invalid Node type '" + type + "'");
        } else {
            switch (type) {
                case "dart": {
                    dartStation = !dartStation;
                } break;
                case "luas": {
                    luasStation = !luasStation;
                } break;
                case "road": {
                    road = !road;
                } break;
                case "bike": {
                    bikeStation = !bikeStation;
                } break;
            }
        }
    }

    void printDetails(){
        logger.info("GPS: ({}, {})\n", this.latitude, this.longitude);
        logger.info("Road neighbors:");

        for (Neighbor nei : this.neighbors.get("road")) {
            logger.info("   - {}\n", nei.nodeId);
        }
    }
}
