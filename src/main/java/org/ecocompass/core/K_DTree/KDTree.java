package org.ecocompass.core.K_DTree;

import java.util.*;

public class KDTree {
    KdNode root;
    int k;

    public KDTree() {
        k = 2;
    }

    public void insert(KdNode node, int depth) {
        if (root == null) {
            root = node;
            return;
        }

        int axis = depth % k; // Determine the axis for splitting based on depth
        KdNode current = root;
        while (true) {
            if (node.coordinates[axis] < current.coordinates[axis]) {
                if (current.left == null) {
                    current.left = node;
                    break;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = node;
                    break;
                }
                current = current.right;
            }
        }
    }

    private KdNode nearestNeighbor(KdNode node, double[] point, int depth, KdNode bestNeighbor, double currentMinDist) {
        if (node == null) {
            return bestNeighbor;
        }

        int axis = depth % k;
        double distSq = 0.0;
        for (int i = 0; i < k; i++) {
            distSq += Math.pow(node.coordinates[i] - point[i], 2);
        }

        // Update best neighbor if this node is closer
        if (distSq < currentMinDist) {
            bestNeighbor = node;
            currentMinDist = distSq;
        }

        KdNode candidateNode;
        if (point[axis] < node.coordinates[axis]) {
            candidateNode = nearestNeighbor(node.left, point, depth + 1, bestNeighbor, currentMinDist);
            candidateNode = nearestNeighbor(node.right, point, depth + 1, candidateNode, Math.min(currentMinDist, Math.pow(node.coordinates[axis] - point[axis], 2))); // Search right subtree only if necessary
        } else {
            candidateNode = nearestNeighbor(node.right, point, depth + 1, bestNeighbor, currentMinDist);
            candidateNode = nearestNeighbor(node.left, point, depth + 1, candidateNode, Math.min(currentMinDist, Math.pow(node.coordinates[axis] - point[axis], 2))); // Search left subtree only if necessary
        }

        return candidateNode;
    }

    public Long findNearestNeighbor(double[] point) {
        return nearestNeighbor(root, point, 0, null, Double.MAX_VALUE).getNodeID();
    }

    private static class NodeWithDistance implements Comparable<NodeWithDistance> {
        KdNode node;
        double distSq;

        NodeWithDistance(KdNode node, double distSq) {
            this.node = node;
            this.distSq = distSq;
        }

        @Override
        public int compareTo(NodeWithDistance other) {
            return Double.compare(this.distSq, other.distSq);
        }
    }

    public List<KdNode> kNearestNeighbors(KdNode node, double[] point, int depth, int k, PriorityQueue<NodeWithDistance> pq) {
        if (node == null) {
            return new ArrayList<>();
        }

        int axis = depth % k;
        double distSq = 0.0;
        for (int i = 0; i < k; i++) {
            distSq += Math.pow(node.coordinates[i] - point[i], 2);
        }

        if (pq.size() < k || distSq < pq.peek().distSq) {
            pq.add(new NodeWithDistance(node, distSq));
            if (pq.size() > k) {
                pq.poll();
            }
        }

        List<KdNode> neighbors = new ArrayList<>();

        if (point[axis] < node.coordinates[axis]) {
            neighbors.addAll(kNearestNeighbors(node.left, point, depth + 1, k, pq));
            neighbors.addAll(kNearestNeighbors(node.right, point, depth + 1, k, pq));
        } else {
            neighbors.addAll(kNearestNeighbors(node.right, point, depth + 1, k, pq));
            neighbors.addAll(kNearestNeighbors(node.left, point, depth + 1, k, pq));
        }

        return neighbors;
    }
}
