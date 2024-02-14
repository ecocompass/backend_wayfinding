package org.ecocompass.core;

import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.ecocompass.core.overpass.Overpass;
import org.json.JSONArray;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Ecocompass Core Engine");

        Overpass overpass = new Overpass();
        String response = overpass.queryLocation("IE", "Dublin", "Rathmines");
        overpass.saveQueryOutput(response, "query_data.json");
        //String response = overpass.loadSavedQueryOutput("query_data.json");

        Graph graph =  overpass.createGraphFromOverpassQuery(response);

        KDTree tree = overpass.createTreeFromGraph(graph);
        //double[] point = {101234, 12235};
        //tree.findNearestNeighbor(point);

        // grosvenor square to trinity
        List<Node> route = graph.shortestPath(59802249L, 2414635995L, "road");

        JSONArray routeJSON = graph.convertPathToJSON(route);
        System.out.println(routeJSON);
    }
}
