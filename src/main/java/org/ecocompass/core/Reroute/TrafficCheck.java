package org.ecocompass.core.Reroute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecocompass.api.utility.PathWithMode;
import org.ecocompass.api.utility.RecommendationPath;
import org.ecocompass.core.util.Cache.CacheEntry;
import org.ecocompass.core.util.Cache.IncidentsCache;
import org.ecocompass.core.util.Cache.RecommendationsCache;
import org.ecocompass.core.util.DistanceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TrafficCheck {

    private final RecommendationsCache recommendationsCache;
    private final IncidentsCache incidentsCache;

    private Map<String, CacheEntry<List<Incident>>> incidentsCacheRealTime;

    private static final Logger logger = LoggerFactory.getLogger(TrafficCheck.class);

    @Autowired
    public TrafficCheck(RecommendationsCache recommendationsCache, IncidentsCache incidentsCache){
        this.recommendationsCache = recommendationsCache;
        this.incidentsCache = incidentsCache;
        this.incidentsCacheRealTime = new HashMap<>();
    }

    public List<Incident> getIncidents(double[] start, double[] end) {
        String cacheKey = Arrays.toString(start) + Arrays.toString(end);
        CacheEntry<List<Incident>> cacheEntry = incidentsCacheRealTime.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            return cacheEntry.getData();
        }
        List<Incident> incidents = new ArrayList<>();
        double southLat = Math.min(start[0], end[0]);
        double westLon = Math.min(start[1], end[1]);
        double northLat = Math.max(start[0], end[0]);
        double eastLon = Math.max(start[1], end[1]);

        String url = "https://dev.virtualearth.net/REST/v1/Traffic/Incidents/" +
                southLat + "," + westLon + "," + northLat + "," + eastLon+
                "?includeJamcidents=true&" +
                "key=AoLyqxM1ItZtV2274ZI8ZHjSAxm0WADxIuegCm8m0WfYg-2_Ir0Pt5GrEdPpsT5H";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> responseEntity = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (Exception e) {
            logger.error("An error occurred while making the HTTP request: {}", e.getMessage());
            return incidents;
        }

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String responseBody = responseEntity.getBody();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                JsonNode resourcesNode = jsonNode.path("resourceSets").get(0).path("resources");
                for (JsonNode resource : resourcesNode) {
                    double[] coordinates = {
                            resource.path("point").path("coordinates").get(0).asDouble(),
                            resource.path("point").path("coordinates").get(1).asDouble()
                    };
                    String description = resource.path("description").asText();
                    boolean isJamcident = resource.path("isJamcident").asBoolean();
                    boolean roadClosed = resource.path("roadClosed").asBoolean();

                    Incident incident = new Incident();
                    incident.setCoordinates(coordinates);
                    incident.setDescription(description);
                    incident.setIsJamcident(isJamcident);
                    incident.setRoadClosed(roadClosed);

                    incidents.add(incident);
                }
            } catch (Exception e) {
                logger.error("Error parsing JSON response: {}", e.getMessage());
            }
        } else {
            logger.error("Failed to get the response or received non-OK status code");
        }
        incidentsCacheRealTime.put(cacheKey, new CacheEntry<>(incidents, 1));
        return incidents;
    }

    public Incident isIncidentOnPath(List<double[]> pathCoordinates){
        List<Incident> incidents = incidentsCache.getAllIncidents();
        incidents.addAll(getIncidents(pathCoordinates.get(0), pathCoordinates.get(pathCoordinates.size()-1)));
        for(double[] pathCoordinate : pathCoordinates){
            for(Incident incident: incidents){
                if(incident.getRoadClosed() || incident.getIsJamcident()) {
                    double incidentLat = incident.getCoordinates()[0];
                    double incidentLon = incident.getCoordinates()[1];
                    double pointLat = pathCoordinate[0];
                    double pointLon = pathCoordinate[1];
                    DistanceUtility distanceUtility = new DistanceUtility();
                    double incidenceDistance = distanceUtility.haversineDistance(incidentLat, incidentLon, pointLat, pointLon);
                    if (incidenceDistance < 0.1) {
                        return incident;
                    }
                }
            }
        }
        return null;
    }

    private static List<double[]> swapCoordinates(List<double[]> coordinates) {
        List<double[]> swappedCoordinates = new ArrayList<>();
        for (double[] coordinate : coordinates) {
            double[] swappedCoordinate = new double[]{coordinate[1], coordinate[0]};
            swappedCoordinates.add(swappedCoordinate);
        }
        return swappedCoordinates;
    }

    public Incident getTransitReRoute(String recommendationId) {
        RecommendationPath recommendationPath = recommendationsCache.get(recommendationId);
        if(recommendationPath != null) {
            List<double[]> pathCoordinates = new ArrayList<>();
            for (PathWithMode pathWithMode : recommendationPath.getModePathList()) {
                pathCoordinates.addAll(pathWithMode.getPathPointList());
                pathCoordinates = swapCoordinates(pathCoordinates);
            }
            return isIncidentOnPath(pathCoordinates);
        }
        logger.info("Recommendation Not Found in Cache!");
        return null;
    }
}
