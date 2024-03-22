package org.ecocompass.api.controller;

import org.ecocompass.api.response.ShortestPathResponse;
import org.ecocompass.api.response.TransitionRouteResponse;
import org.ecocompass.api.utility.RecommendationPath;
import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.PathFinder.Query;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RoutesController {

    private final Graph graph;
    private final Query query;

    private final KDTree kdTreeRoad;

    @Autowired
    public RoutesController(Graph graph, Query query, @Qualifier("kdTreeRoad") KDTree kdTreeRoad) {
        this.graph = graph;
        this.query = query;
        this.kdTreeRoad = kdTreeRoad;
    }

    @GetMapping(value = "/api/routes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShortestPathResponse> getShortestPath(
            @RequestParam double[] startCoordinates,
            @RequestParam double[] endCoordinates
    ) throws Exception {
        // Swap the coordinates (lon, lat instead of lat, lon)
        double[] startSwapped = {startCoordinates[1], startCoordinates[0]};
        double[] endSwapped = {endCoordinates[1], endCoordinates[0]};
        KdNode startNode = kdTreeRoad.findNode(startSwapped);
        KdNode endNode = kdTreeRoad.findNode(endSwapped);

        List<Node> shortestPathNodes = graph.shortestPath(startNode.getNodeID(), endNode.getNodeID(), "road");

        List<double[]> shortestPathCoordinates = graph.extractCoordinates(shortestPathNodes);

        return ResponseEntity.ok(new ShortestPathResponse(shortestPathCoordinates));
    }

    @GetMapping(value = "/api/routes2", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransitionRouteResponse> getShortestPath2(
            @RequestParam double[] startCoordinates,
            @RequestParam double[] endCoordinates
    ) {
        // Swap the coordinates (lon, lat instead of lat, lon)
        double[] startSwapped = {startCoordinates[1], startCoordinates[0]};
        double[] endSwapped = {endCoordinates[1], endCoordinates[0]};

        return ResponseEntity.ok(query.getTransitRecommendations(startSwapped, endSwapped));
    }
}