package org.ecocompass.core;

import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.ecocompass.core.overpass.Overpass;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Ecocompass Core Engine");

        Overpass overpass = new Overpass();
         String response = overpass.queryLocation(Map.of("country", "IE", "county", "County Dublin", "city", "Dublin"));
         overpass.saveQueryOutput(response, "query_data.json");
//        String response = overpass.loadSavedQueryOutput("query_data.json");

        Graph graph =  overpass.createGraphFromOverpassQuery(response);

        // grosvenor square to trinity
        List<Node> route = graph.shortestPath(59802249L, 2414635995L, "road");

        JSONArray routeJSON = graph.convertPathToJSON(route);
        System.out.println(routeJSON);
    }
}
