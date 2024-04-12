package org.ecocompass.api.controller;

import org.ecocompass.api.response.ShortestPathResponse;
import org.ecocompass.api.response.TrafficResponse;
import org.ecocompass.api.response.TransitionRouteResponse;
import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.PathFinder.Query;
import org.ecocompass.core.Reroute.Incident;
import org.ecocompass.core.Reroute.TrafficCheck;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.ecocompass.core.util.Cache.IncidentsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoutesController {

    private final Graph graph;
    private final Query query;
    private final TrafficCheck trafficCheck;
    private final KDTree kdTreeRoad;
    private final IncidentsCache incidentsCache;

    @Autowired
    public RoutesController(Graph graph, Query query, TrafficCheck trafficCheck,
                            IncidentsCache incidentsCache, @Qualifier("kdTreeRoad") KDTree kdTreeRoad) {
        this.graph = graph;
        this.query = query;
        this.kdTreeRoad = kdTreeRoad;
        this.trafficCheck = trafficCheck;
        this.incidentsCache = incidentsCache;
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
    ) throws Exception {
        // Swap the coordinates (lon, lat instead of lat, lon)
        double[] startSwapped = {startCoordinates[1], startCoordinates[0]};
        double[] endSwapped = {endCoordinates[1], endCoordinates[0]};

        return ResponseEntity.ok(query.getTransitRecommendations(startSwapped, endSwapped, graph));
    }

    @GetMapping(value = "/api/transit/incident", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrafficResponse> getTrafficIncidents(
            @RequestParam String recommendationId) {
        TrafficResponse response = new TrafficResponse();
        Incident incident = trafficCheck.getTransitReRoute(recommendationId);
        if(incident != null) {
            response.setReRoute(true);
            response.setIncident(incident);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createIncident")
    public ResponseEntity<String> createIncident(@RequestBody Incident incident) {
        String incidentId = incidentsCache.generateUniqueKey();
        incidentsCache.addIncident(incidentId, incident);
        return ResponseEntity.status(HttpStatus.CREATED).body("Incident "  + incidentId + " created successfully");
    }

    @DeleteMapping("/deleteIncident/{incidentId}")
    public ResponseEntity<String> deleteIncident(@PathVariable String incidentId) {
        if (incidentsCache.containsIncident(incidentId)) {
            incidentsCache.deleteIncident(incidentId);
            return ResponseEntity.status(HttpStatus.OK).body("Incident " + incidentId + " deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incident with ID " + incidentId + " not found");
        }
    }

}