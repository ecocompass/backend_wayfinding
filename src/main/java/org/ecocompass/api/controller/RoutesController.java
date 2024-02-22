package org.ecocompass.api.controller;

import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoutesController {

    private final Graph graph;

    private final KDTree tree;

    public RoutesController(Graph graph, KDTree tree) {
        this.graph = graph;
        this.tree = tree;
    }

    @GetMapping("/api/routes")
    public List<double[]> getShortestPath(
            @RequestParam double[] startCoordinates,
            @RequestParam double[] endCoordinates
    ) throws Exception {
        Long startNode = tree.findNode(startCoordinates);
        Long endNode = tree.findNode(endCoordinates);

        List<Node> shortestPathNodes = graph.shortestPath(startNode, endNode, "road");

        return graph.extractCoordinates(shortestPathNodes);
    }
}