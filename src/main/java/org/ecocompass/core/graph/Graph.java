package org.ecocompass.core.graph;

import java.util.*;
import org.json.JSONArray;

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
        node2.addNeighbor(transportType, node1Id, distance);
    }

    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes.values());
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

    public List<Node> shortestPath(Long startNodeId, Long endNodeId, String transportType) throws Exception {
        System.out.printf("Computing shortest-path from %d to %d over '%s'\n", startNodeId, endNodeId, transportType);
        PriorityQueue<NodeRecord> openList = new PriorityQueue<>(Comparator.comparingDouble(nr -> nr.estimatedTotalCost));
        Map<Long, NodeRecord> closedList = new HashMap<>();

        Node startNode = nodes.get(startNodeId);
        Node endNode = nodes.get(endNodeId);

        if (startNode == null) {
            throw new Exception("Start node not found in graph.");
        }

        if (endNode == null) {
            throw new Exception("End node not found in graph.");
        }

        openList.add(new NodeRecord(startNodeId, null, 0, calculateDistance(startNode, endNode)));

        while (!openList.isEmpty()) {
            NodeRecord currentRecord = openList.poll();
            Node currentNode = nodes.get(currentRecord.nodeId);

            if (currentRecord.nodeId.equals(endNodeId)) {
                closedList.put(currentRecord.nodeId, currentRecord);
                return reconstructPath(closedList, endNodeId);
            }

            for (Neighbor neighbor : currentNode.neighbors.get(transportType)) {
                Long neighborId = neighbor.nodeId;

                if (closedList.containsKey(neighborId)) continue;

                double tentativeCost = currentRecord.costSoFar + neighbor.weight;
                double heuristicCost = calculateDistance(nodes.get(neighborId), endNode);
                double totalCost = tentativeCost + heuristicCost;

                boolean shouldAdd = true;
                // Here, check if a better path exists to this neighbor
                if (openList.stream().anyMatch(nr -> nr.nodeId.equals(neighborId))) {
                    for (NodeRecord openRecord : openList) {
                        if (openRecord.nodeId.equals(neighborId) && openRecord.costSoFar <= tentativeCost) {
                            shouldAdd = false;
                            break;
                        }
                    }
                }

                if (shouldAdd) {
                    openList.add(new NodeRecord(neighborId, currentRecord.nodeId, tentativeCost, totalCost));
                }
            }
            closedList.put(currentRecord.nodeId, currentRecord);
        }

        return Collections.emptyList(); // Path not found
    }

    private List<Node> reconstructPath(Map<Long, NodeRecord> closedList, Long endNodeId) {
        LinkedList<Node> path = new LinkedList<>();

        Long currentNodeId = endNodeId;
        while (currentNodeId != null) {
            path.addFirst(this.nodes.get(currentNodeId));
            NodeRecord record = closedList.get(currentNodeId);
            currentNodeId = record.previousNodeId;
        }
        return path;
    }

    public JSONArray convertPathToJSON(List<Node> path) {
        JSONArray jsonArray = new JSONArray();

        for (Node node : path) {
            JSONArray coordinates = new JSONArray();
            coordinates.put(node.longitude);
            coordinates.put(node.latitude);
            jsonArray.put(coordinates);
        }

        return jsonArray;
    }

    static class NodeRecord {
        Long nodeId;
        Long previousNodeId;
        double costSoFar;
        double estimatedTotalCost;

        public NodeRecord(Long nodeId, Long previousNodeId, double costSoFar, double estimatedTotalCost) {
            this.nodeId = nodeId;
            this.previousNodeId = previousNodeId;
            this.costSoFar = costSoFar;
            this.estimatedTotalCost = estimatedTotalCost;
        }
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