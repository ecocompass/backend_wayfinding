package org.ecocompass.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.ecocompass.api.utility.Coordinates;
import org.ecocompass.api.utility.Station;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
public class HelloController {

    @Value("${openweathermap.api.key}")
    private String openWeatherMapApiKey;

    @Value("${openweathermap.api.host}")
    private String openWeatherMapApiHost;

    @PostMapping("/hello")
    public String sayHello(@RequestBody Coordinates coordinates) throws IOException {
        double currentLatitude = coordinates.getLatitude();
        double currentLongitude = coordinates.getLongitude();

        // Construct the OpenWeatherMap API URL using the configured host
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + currentLatitude
                + "&lon=" + currentLongitude
                + "&appid=" + openWeatherMapApiKey
                + "&units=metric";

        // Make the HTTP request to OpenWeatherMap API
        ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(apiUrl, String.class);
        String responseBody = responseEntity.getBody();

        String cityName = JsonPath.read(responseBody, "$.name");
        String country = JsonPath.read(responseBody, "$.sys.country");
        String weatherDescription = JsonPath.read(responseBody, "$.weather[0].description");
        Double temperature = JsonPath.read(responseBody, "$.main.temp");

        // Find nearest DART stations
        List<Station> nearestStations = findNearestDARTStations(currentLatitude, currentLongitude, 3);

        // Create a custom string with the extracted information and nearest DART stations
        StringBuilder customResponseBuilder = new StringBuilder("City: ");
        customResponseBuilder.append(cityName != null ? cityName : "N/A");
        customResponseBuilder.append(", Country: ");
        customResponseBuilder.append(country != null ? country : "N/A");
        customResponseBuilder.append(", Temperature: ");
        customResponseBuilder.append(temperature).append("°C");
        customResponseBuilder.append(", Weather Description: ");
        customResponseBuilder.append(weatherDescription != null ? weatherDescription : "N/A");

        // Add nearest DART stations to the response
        customResponseBuilder.append(" Nearest DART Stations: ");
        for (Station station : nearestStations) {
            customResponseBuilder.append(station.getStationDesc()).append(": ").append(station.getLatitude()).append(", ").append(station.getLongitude()).append(" ");
        }

        String customResponse = customResponseBuilder.toString();

        return "Hello World! Response from MapEngine! Received coordinates: Latitude " + currentLatitude +
                ", Longitude " + currentLongitude + ", OpenWeatherMap Response: " + customResponse;
    }

    public List<Station> findNearestDARTStations(double currentLatitude, double currentLongitude, int numStations) throws IOException {
        // Make a request to the DART API to get the station information
        String dartApiUrl = "http://api.irishrail.ie/realtime/realtime.asmx/getAllStationsXML_WithStationType?StationType=D";
        URL url = new URL(dartApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            // Parse the XML response and create a list of Station objects
            // With these lines to convert BufferedReader to String
            StringBuilder xmlStringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlStringBuilder.append(line);
            }
            String xmlString = xmlStringBuilder.toString();

            // Then call the method with the String parameter
            List<Station> dartStations = parseDARTStationsXML(xmlString);

            // Calculate distances and sort the stations based on distance
            for (Station station : dartStations) {
                double distance = calculateDistance(currentLatitude, currentLongitude, station.getLatitude(), station.getLongitude());
                station.setDistance(distance);
            }

            dartStations.sort(Comparator.comparingDouble(Station::getDistance));

            // Get the nearest stations
            List<Station> nearestStations = new ArrayList<>();
            for (int i = 0; i < numStations && i < dartStations.size(); i++) {
                nearestStations.add(dartStations.get(i));
            }

            return nearestStations;
        } finally {
            connection.disconnect();
        }
    }

    private List<Station> parseDARTStationsXML(String xml) {
        List<Station> stations = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);

            NodeList stationNodes = document.getElementsByTagName("objStation");

            for (int i = 0; i < stationNodes.getLength(); i++) {
                Element stationElement = (Element) stationNodes.item(i);

                String stationDesc = stationElement.getElementsByTagName("StationDesc").item(0).getTextContent();
                String latitudeStr = stationElement.getElementsByTagName("StationLatitude").item(0).getTextContent();
                String longitudeStr = stationElement.getElementsByTagName("StationLongitude").item(0).getTextContent();

                // Try parsing latitude and longitude as doubles
                double latitude, longitude;
                try {
                    latitude = Double.parseDouble(latitudeStr);
                    longitude = Double.parseDouble(longitudeStr);
                } catch (NumberFormatException e) {
                    // Handle the case where latitude or longitude is not a valid double
                    System.err.println("Skipping station " + stationDesc + " due to invalid coordinates.");
                    continue;
                }

                Station station = new Station(stationDesc, latitude, longitude);
                stations.add(station);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        final double R = 6371.0;

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
