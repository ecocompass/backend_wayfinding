package org.ecocompass.core.overpass;

import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.PathFinder.FinderCore;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.graph.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Overpass {
    private static final Logger logger = LogManager.getLogger(Overpass.class);
    private final String overpassUrl = "http://overpass-api.de/api/interpreter";
    private final HttpClient client;
    private final FinderCore finderCore;

    public Overpass() {
        client = HttpClient.newHttpClient();
        finderCore = new FinderCore();
    }
    public String queryLocation(Map<String, String> geoMap) {
        String varsString = "";
        String geofilterString = "(way";

        if (geoMap.containsKey("country")) {
            varsString += String.format("area[\"ISO3166-1:alpha2\"=\"%s\"]->.country;", geoMap.get("country"));
            geofilterString += "(area.country)";
        }

        if (geoMap.containsKey("county")) {
            varsString += String.format("area[\"admin_level\"=\"6\"][\"name\"=\"%s\"]->.county;", geoMap.get("county"));
            geofilterString += "(area.county)";
        }

        if (geoMap.containsKey("city")) {
            varsString += String.format("area[\"admin_level\"=\"7\"][\"name\"=\"%s\"]->.city;", geoMap.get("city"));
            geofilterString += "(area.city)";
        }

        if (geoMap.containsKey("neighborhood")) {
            varsString += String.format("area[\"name\"=\"%s\"]->.neighborhood;", geoMap.get("neighborhood"));
            geofilterString += "(area.neighborhood)";
        }

        String queryString = "[out:json];" + varsString + geofilterString + "[highway];>;);out body;";
        logger.info("SENDING REQUEST TO: " + overpassUrl);
        logger.info("QUERY STRING\n" + queryString);
        String encodedQueryString = URLEncoder.encode(queryString, Charset.defaultCharset());


        URI requestUrl = URI.create(overpassUrl + "?data=" + encodedQueryString);

        logger.info("REQUEST URL: " + requestUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(requestUrl)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Successfully queried Overpass API!");
                return response.body();
            } else {
                logger.info("Error: " + response.statusCode() + "\n" + response.body());
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
            logger.info("Response body written to file: " + filePath);
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
        logger.info("Creating Graph from Overpass query data");
        JSONObject parsedJSON = new JSONObject(data);
        JSONArray elementsArray = parsedJSON.getJSONArray("elements");

        Graph graph = new Graph();

        logger.info("Adding Nodes and Edges to Graph");
        for (int i = 0; i < elementsArray.length(); i++) {
            JSONObject element = elementsArray.getJSONObject(i);

            if (Objects.equals(element.getString("type"), "node")) {
                String nodeID = element.optString("id");
                double lat = element.getDouble("lat");
                double lon = element.getDouble("lon");
                graph.addNode(nodeID, lat, lon);

            } else if (Objects.equals(element.getString("type"), "way")) {
                JSONArray nodesArray = element.getJSONArray("nodes");
                String lastRef = null;

                for (int j = 0; j < nodesArray.length(); j++) {
                    String nodeID = nodesArray.optString(j);
                    if (lastRef != null) {
                        graph.addEdge(nodeID, lastRef, "road");
                    }
                    lastRef = nodeID;
                }
            }
        }
        logger.info("Successfully finished creating Graph");

        return graph;
    }

    public KDTree createTreeFromGraph(String mode, String gtfsFileName, String roadDataFileName) throws IOException {
        logger.info("Creating KD-Tree for " + mode);
        if (Objects.equals(mode, "road") || Objects.equals(mode, "bike")) {
            String transitData = Files.readString(Path.of(roadDataFileName));
            JSONObject jsonObject = new JSONObject(transitData);
            return finderCore.buildKDTree(jsonObject);
        } else {
            String transitData = Files.readString(Path.of(gtfsFileName));
            JSONObject jsonObject = new JSONObject(transitData);
            return finderCore.buildKDTree(jsonObject.getJSONObject(mode + "_stops"));
        }
    }

    public KDTree createTreeFromGraph(Graph graph, String mode) {
        List<KdNode> nodes = new ArrayList<>();

        Map<String, Node> allNodes = graph.getAllNodes(mode);

        logger.info("Adding nodes to KD-Tree of " + mode);
        for (Map.Entry<String, Node> entry : allNodes.entrySet()) {
            String nodeID = String.valueOf(entry.getKey());
            Node node = entry.getValue();
            double[] coordinates = {node.latitude, node.longitude};
            KdNode kdNode = new KdNode(coordinates, nodeID, node, "nodePoint");
            nodes.add(kdNode);
        }

        return new KDTree(nodes);
    }
}
