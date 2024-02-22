package org.ecocompass.core.K_DTree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecocompass.core.graph.Node;

import java.util.*;

public class KDTree {
    private static final Logger logger = LogManager.getLogger(KDTree.class);
    private final KdNode root;
    private final int k;

    public KDTree(List<KdNode> nodes) {
        this.k = nodes.get(0).getCoordinates().length;
        this.root = buildTree(nodes, 0);
    }

    private KdNode buildTree(List<KdNode> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        int axis = depth % k;
        points.sort(Comparator.comparingDouble(node -> node.getCoordinates()[axis]));

        int medianIndex = points.size() / 2;
        KdNode medianNode = points.get(medianIndex);

        List<KdNode> leftPoints = new ArrayList<>(points.subList(0, medianIndex));
        List<KdNode> rightPoints = new ArrayList<>(points.subList(medianIndex + 1, points.size()));

        medianNode.setLeft(buildTree(leftPoints, depth + 1));
        medianNode.setRight(buildTree(rightPoints, depth + 1));

        return medianNode;
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
            currentMinDist = Math.min(currentMinDist, Math.pow(node.coordinates[axis] - point[axis], 2));
        } else {
            candidateNode = nearestNeighbor(node.right, point, depth + 1, bestNeighbor, currentMinDist);
            currentMinDist = Math.min(currentMinDist, Math.pow(node.coordinates[axis] - point[axis], 2));
        }

        if (currentMinDist < distSq) {
            return candidateNode;
        } else {
            return bestNeighbor;
        }
    }

    public Long findNearestNeighbor(double[] point) {
        return nearestNeighbor(root, point, 0, null, Double.MAX_VALUE).getNodeID();
    }

    public Long findNode(double[] point) {
        logger.info("Finding nearest code to coordinates ({}, {})", point[0], point[1]);
        KdNode exactMatch = findExactMatch(root, point, 0);

        if (exactMatch != null) {
            return exactMatch.getNodeID();
        } else {
            return findNearestNeighbor(point);
        }
    }

    private KdNode findExactMatch(KdNode node, double[] point, int depth) {
        if (node == null) {
            return null;
        }

        int axis = depth % k;

        if (Arrays.equals(node.coordinates, point)) {
            return node;
        }

        if (point[axis] < node.coordinates[axis]) {
            return findExactMatch(node.left, point, depth + 1);
        } else {
            return findExactMatch(node.right, point, depth + 1);
        }
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
