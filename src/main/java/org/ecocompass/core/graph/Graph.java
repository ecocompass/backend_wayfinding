package org.ecocompass.core.graph;

import java.util.HashMap;
import java.util.Map;

public class Graph {
    Map<Long, Node> nodes;

    public Graph() {
        this.nodes = new HashMap<>();
    }

    public void addCustomNode(Long nodeId, Node node) {
        this.nodes.put(nodeId, node);
    }

    public void addNode(Long nodeId, double latitude, double longitude) {
        Node node = new Node(latitude, longitude);
        this.nodes.put(nodeId, node);
    }

    public void addEdge(Long node1Id, Long node2Id, String transportType) throws Exception {
        Node node1 = this.nodes.get(node1Id);
        Node node2 = this.nodes.get(node2Id);
        if (node1 == null) {
            throw new Exception("Node '" + node1Id + "' not found!");
        }
        if (node2 == null) {
            throw new Exception("Node '" + node2Id + "' not found!");
        }
        double distance = calculateDistance(node1, node2);

        node1.addNeighbor(transportType, node2Id, distance);
    }

    private double calculateDistance(Node node1, Node node2) {
        double latDistance = Math.toRadians(node2.latitude - node1.latitude);
        double lonDistance = Math.toRadians(node2.longitude - node1.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(node1.latitude)) * Math.cos(Math.toRadians(node2.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }
}

class Neighbor {
    Long nodeId;
    Double weight;

    public Neighbor(Long nodeId, Double weight) {
        this.nodeId = nodeId;
        this.weight = weight;
    }
}