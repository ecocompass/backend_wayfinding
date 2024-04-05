package org.ecocompass.core.K_DTree;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecocompass.core.PathFinder.FinderCore;
import org.ecocompass.core.util.CacheEntry;

import java.util.*;

public class KDTree {
    private static final Logger logger = LogManager.getLogger(KDTree.class);

    private FinderCore finderCore;

    @Getter
    private final KdNode root;
    private final int k;

    private final Map<String, CacheEntry<KdNode>> nodeCache;

    public KDTree(List<KdNode> nodes) {
        this.k = nodes.get(0).getCoordinates().length;
        this.root = buildTree(nodes, 0);
        finderCore = new FinderCore();
        nodeCache = new HashMap<>();
    }

    public KdNode getRoot(){
        return this.root;
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
            distSq += finderCore.haversineDistance(point[0], point[1], node.getCoordinates()[0], node.getCoordinates()[1]);
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

    public KdNode findNearestNeighbor(double[] point) {
        return nearestNeighbor(root, point, 0, null, Double.MAX_VALUE);
    }

    public KdNode findNode(double[] point) {
        logger.debug("Finding nearest code to coordinates ({}, {})", point[0], point[1]);
        String cacheKey = Arrays.toString(point);
        CacheEntry<KdNode> cacheEntry = nodeCache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            return cacheEntry.getData();
        }
        KdNode exactMatch = findExactMatch(root, point, 0);

        if (exactMatch == null) {
            exactMatch = findNearestNeighbor(point);
        }
        nodeCache.put(cacheKey, new CacheEntry<>(exactMatch, 5));
        return exactMatch;
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

    public static class NodeWithDistance implements Comparable<NodeWithDistance> {
        public KdNode node;
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

    public PriorityQueue<NodeWithDistance> kNearestNeighbors(KdNode node, double[] point, int depth, int k, PriorityQueue<NodeWithDistance> pq) {
        if (node == null) {
            return pq;
        }

        int axis = depth % this.k;
        double distSq = finderCore.haversineDistance(point[0], point[1], node.getCoordinates()[0], node.getCoordinates()[1]);

        if (pq.size() < k) {
            pq.add(new NodeWithDistance(node, distSq));
        } else {
            assert pq.peek() != null;
            if (distSq < pq.peek().distSq) {
                pq.poll();
                pq.add(new NodeWithDistance(node, distSq));
            }
        }

        // Traverse the KDTree based on the current axis
        if (point[axis] < node.getCoordinates()[axis]) {
            pq = kNearestNeighbors(node.getLeft(), point, depth + 1, k, pq);
        } else {
            pq = kNearestNeighbors(node.getRight(), point, depth + 1, k, pq);
        }

        return pq;
    }
}