package org.ecocompass.core;

import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.overpass.Overpass;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Ecocompass Core Engine");

        Overpass overpass = new Overpass();
        // String response = overpass.queryLocation("IE", "Dublin", "Rathmines");
        // overpass.saveQueryOutput(response, "query_data.json");
        String response = overpass.loadSavedQueryOutput("query_data.json");

        Graph graph =  overpass.createGraphFromOverpassQuery(response);
    }
}
