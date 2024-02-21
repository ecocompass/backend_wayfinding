package org.ecocompass.core.K_DTree;

import lombok.Getter;
import lombok.Setter;
import org.ecocompass.core.graph.Node;

// Node class representing a point in the k-dimensional space
@Getter
@Setter
public class KdNode {
    double[] coordinates;
    Long nodeID;
    KdNode left;
    KdNode right;

    Node node;

    public KdNode(double[] coordinates, Long nodeID, Node node) {
        this.coordinates = coordinates;
        this.node = node;
        this.nodeID = nodeID;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public Long getNodeID() {
        return nodeID;
    }

    public Node getNode() {
        return node;
    }

    public KdNode getLeft() {
        return left;
    }

    public void setLeft(KdNode left) {
        this.left = left;
    }

    public KdNode getRight() {
        return right;
    }

    public void setRight(KdNode right) {
        this.right = right;
    }

}