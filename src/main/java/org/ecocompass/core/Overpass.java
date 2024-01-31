package org.ecocompass.core;

import java.io.FileWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.io.IOException;


public class Overpass {
    private final String overpassUrl = "http://overpass-api.de/api/interpreter";
    private final HttpClient client;

    public Overpass() {
        client = HttpClient.newHttpClient();
    }
    public String queryLocation(String country, String city, String neighborhood) {
        String queryTemplate = "[out:xml];area[\"ISO3166-1:alpha2\"=\"%s\"]->.country;area[\"admin_level\"=\"7\"]"
            + "[\"name\"=\"%s\"]->.city;area[\"name\"=\"%s\"]->.nei;"
            + "(way(area.country)(area.city)(area.nei)[highway];>;);out body;";

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
}
