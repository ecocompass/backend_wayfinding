package org.ecocompass.core;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.ecocompass.core.overpass.Overpass;
import org.ecocompass.core.overpass.OverpassEdge;
import org.ecocompass.core.overpass.OverpassNode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Ecocompass Core Engine");

        Overpass overpass = new Overpass();
//        String response = overpass.queryLocation("IE", "Dublin", "Rathmines");
//        overpass.saveQueryOutput(response, "query_data.json");

        String response = overpass.loadSavedQueryOutput("query_data.json");

//        System.out.println("hello................................................." + response);

        Map<Long, OverpassNode> mapNodes = new HashMap<>();
        List<OverpassEdge> mapEdges = new ArrayList<>();
        overpass.parseQueryOutput(response, mapNodes, mapEdges);

        System.out.println(mapNodes.get(56570286L).latitude);
        System.out.println(mapNodes.get(56570286L).longitude);
    }
}
