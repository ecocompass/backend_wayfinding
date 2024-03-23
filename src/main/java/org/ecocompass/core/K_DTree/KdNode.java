package org.ecocompass.core.K_DTree;

import lombok.Getter;
import lombok.Setter;
import org.ecocompass.core.graph.Node;

// Node class representing a point in the k-dimensional space
@Getter
@Setter
public class KdNode {
    double[] coordinates;
    String nodeID;
    KdNode left;
    KdNode right;
    Node node;
    String name;

    public KdNode(double[] coordinates, String nodeID, Node node, String name) {
        this.coordinates = coordinates;
        this.node = node;
        this.nodeID = nodeID;
        this.name = name;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public String getNodeID() {
        return nodeID;
    }

    public String getName(){
        return name;
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