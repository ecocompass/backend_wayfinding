package org.ecocompass.core.util.Cache;

import org.ecocompass.core.Reroute.Incident;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class IncidentsCache {

    private final Map<String, CacheEntry<Incident>> incidentMap;

    public IncidentsCache() {
        this.incidentMap = new HashMap<>();
    }

    public void addIncident(String key, Incident incident) {
        CacheEntry<Incident> cacheEntry = new CacheEntry<>(incident, 60);
        incidentMap.put(key, cacheEntry);
    }

    public Incident getIncident(String key) {
        CacheEntry<Incident> cacheEntry = incidentMap.get(key);
        return (cacheEntry != null && !cacheEntry.isExpired()) ? cacheEntry.getData() : null;
    }

    public boolean containsIncident(String key) {
        CacheEntry<Incident> cacheEntry = incidentMap.get(key);
        return (cacheEntry != null && !cacheEntry.isExpired());
    }

    public List<Incident> getAllIncidents() {
        return incidentMap.values().stream()
                .filter(entry -> !entry.isExpired())
                .map(CacheEntry::getData)
                .collect(Collectors.toList());
    }

    public void printAllIncidents() {
        List<Incident> incidents = getAllIncidents();
        for (Incident incident : incidents) {
            System.out.println(incident);
        }
    }

    public Map<String, Incident> getAllIncidentsWithKey() {
        Map<String, Incident> incidentsWithKey = new HashMap<>();
        for (Map.Entry<String, CacheEntry<Incident>> entry : incidentMap.entrySet()) {
            String key = entry.getKey();
            CacheEntry<Incident> cacheEntry = entry.getValue();
            if (!cacheEntry.isExpired()) {
                incidentsWithKey.put(key, cacheEntry.getData());
            }
        }
        return incidentsWithKey;
    }

    public void deleteIncident(String key) {
        incidentMap.remove(key);
    }

    public String generateUniqueKey() {
        String key;
        do {
            key = UUID.randomUUID().toString();
        } while (incidentMap.containsKey(key));
        return key;
    }
}