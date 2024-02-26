package org.ecocompass.api.controller;

import org.ecocompass.api.response.ShortestPathResponse;
import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoutesController {

    private final Graph graph;

    private final KDTree kdTreeRoad;
    private final KDTree kdTreeBus;
    private final KDTree kdTreeLuas;
    private final KDTree kdTreeDart;
    private final KDTree kdTreeBike;

    @Autowired
    public RoutesController(Graph graph, @Qualifier("kdTreeRoad") KDTree kdTreeRoad,
                            @Qualifier("kdTreeBus") KDTree kdTreeBus, @Qualifier("kdTreeLuas") KDTree kdTreeLuas,
                            @Qualifier("kdTreeDart") KDTree kdTreeDart, @Qualifier("kdTreeBike") KDTree kdTreeBike) {
        this.graph = graph;
        this.kdTreeRoad = kdTreeRoad;
        this.kdTreeBus = kdTreeBus;
        this.kdTreeLuas = kdTreeLuas;
        this.kdTreeDart = kdTreeDart;
        this.kdTreeBike = kdTreeBike;
    }

    @GetMapping("/api/routes")
    public ShortestPathResponse getShortestPath(
            @RequestParam double[] startCoordinates,
            @RequestParam double[] endCoordinates
    ) throws Exception {
        // Swap the coordinates (lon, lat instead of lat, lon)
        double[] startSwapped = {startCoordinates[1], startCoordinates[0]};
        double[] endSwapped = {endCoordinates[1], endCoordinates[0]};

        Long startNode = kdTreeRoad.findNode(startSwapped);
        Long endNode = kdTreeRoad.findNode(endSwapped);

        List<Node> shortestPathNodes = graph.shortestPath(startNode, endNode, "road");

        List<double[]> shortestPathCoordinates = graph.extractCoordinates(shortestPathNodes);

        return new ShortestPathResponse(shortestPathCoordinates);
    }
}