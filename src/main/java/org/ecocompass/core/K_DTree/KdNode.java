package org.ecocompass.core.K_DTree;

import org.ecocompass.core.graph.Node;

// Node class representing a point in the k-dimensional space
public class KdNode {
    double[] coordinates; // Array of coordinates for each dimension
    Long nodeID;
    KdNode left;
    KdNode right;

    Node node;

    public KdNode(double[] coordinates, Long nodeID, Node node) {
        this.coordinates = coordinates;
        this.node = node;
        this.nodeID = nodeID;
    }

    public Long getNodeID() {
        return nodeID;
    }
}