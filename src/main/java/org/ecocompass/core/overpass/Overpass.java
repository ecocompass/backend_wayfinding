package org.ecocompass.core.overpass;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Overpass {
    private final String overpassUrl = "http://overpass-api.de/api/interpreter";
    private final HttpClient client;

    public Overpass() {
        client = HttpClient.newHttpClient();
    }
    public String queryLocation(String country, String city, String neighborhood) {
        String queryTemplate = "[out:json];area[\"ISO3166-1:alpha2\"=\"%s\"]->.country;area[\"admin_level\"=\"7\"]"
            + "[\"name\"=\"%s\"]->.city;area[\"name\"=\"%s\"]->.nei;"
            + "(way(area.country)(area.city)[highway];>;);out body;";

        String queryString = String.format(queryTemplate, country, city, neighborhood);
        System.out.println("SENDING REQUEST TO: " + overpassUrl);
        System.out.println("QUERY STRING\n" + queryString);
        String encodedQueryString = URLEncoder.encode(queryString, Charset.defaultCharset());


        URI requestUrl = URI.create(overpassUrl + "?data=" + encodedQueryString);

        System.out.println("REQUEST URL: " + requestUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(requestUrl)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("Error: " + response.statusCode() + "\n" + response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveQueryOutput(String queryOutput, String filePath) {

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(queryOutput);
            System.out.println("Response body written to file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String loadSavedQueryOutput(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] data = new byte[fis.available()];
        fis.read(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    public Graph createGraphFromOverpassQuery(String data) throws Exception {
        JSONObject parsedJSON = new JSONObject(data);
        JSONArray elementsArray = parsedJSON.getJSONArray("elements");

        Graph graph = new Graph();

        for (int i = 0; i < elementsArray.length(); i++) {
            JSONObject element = elementsArray.getJSONObject(i);

            if (Objects.equals(element.getString("type"), "node")) {
                Long nodeID = element.getLong("id");
                double lat = element.getDouble("lat");
                double lon = element.getDouble("lon");
                graph.addNode(nodeID, lat, lon);

            } else if (Objects.equals(element.getString("type"), "way")) {
                JSONArray nodesArray = element.getJSONArray("nodes");
                Long lastRef = null;

                for (int j = 0; j < nodesArray.length(); j++) {
                    Long nodeID = nodesArray.getLong(j);
                    if (lastRef != null) {
                        graph.addEdge(nodeID, lastRef, "road");
                    }
                    lastRef = nodeID;
                }
            }
        }

        return graph;
    }

    public KDTree createTreeFromGraph(Graph graph) {

        KDTree tree = new KDTree();

        List<Node> allNodes = graph.getAllNodes();

        for (Node node : allNodes) {
            double[] coordinates = {node.latitude, node.longitude};
            KdNode kdNode = new KdNode(coordinates, node);
            tree.insert(kdNode,0);
        }


        return tree;
    }

}
