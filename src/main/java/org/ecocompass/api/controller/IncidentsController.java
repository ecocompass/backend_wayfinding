package org.ecocompass.api.controller;

import org.ecocompass.api.response.TrafficResponse;
import org.ecocompass.core.Reroute.Incident;
import org.ecocompass.core.Reroute.TrafficCheck;
import org.ecocompass.core.util.Cache.IncidentsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IncidentsController {
    private final TrafficCheck trafficCheck;
    private final IncidentsCache incidentsCache;

    @Autowired
    public IncidentsController(TrafficCheck trafficCheck,
                            IncidentsCache incidentsCache) {
        this.trafficCheck = trafficCheck;
        this.incidentsCache = incidentsCache;
    }

    @GetMapping(value = "/api/transit/incidents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrafficResponse> getTrafficIncidents(
            @RequestParam String recommendationId) {
        TrafficResponse response = new TrafficResponse();
        Incident incident = trafficCheck.getTransitReRoute(recommendationId);
        if(incident != null) {
            response.setReRoute(true);
            response.setIncident(incident);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createIncident")
    public ResponseEntity<String> createIncident(@RequestBody Incident incident) {
        String incidentId = incidentsCache.generateUniqueKey();
        incidentsCache.addIncident(incidentId, incident);
        return ResponseEntity.status(HttpStatus.CREATED).body("Incident "  + incidentId + " created successfully");
    }

    @DeleteMapping("/deleteIncident/{incidentId}")
    public ResponseEntity<String> deleteIncident(@PathVariable String incidentId) {
        if (incidentsCache.containsIncident(incidentId)) {
            incidentsCache.deleteIncident(incidentId);
            return ResponseEntity.status(HttpStatus.OK).body("Incident " + incidentId + " deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incident with ID " + incidentId + " not found");
        }
    }

    @GetMapping(value = "/api/incidents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Incident>> getAllIncidents() {
        Map<String, Incident> incidents = incidentsCache.getAllIncidentsWithKey();
        if (incidents != null) {
            return ResponseEntity.ok(incidents);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
