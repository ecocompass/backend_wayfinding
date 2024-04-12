package org.ecocompass.core.util.Cache;

import java.util.HashMap;
import java.util.Map;

import org.ecocompass.api.utility.RecommendationPath;
import org.springframework.stereotype.Component;
@Component
public class RecommendationsCache {

    private final Map<String, CacheEntry<RecommendationPath>> recommendationPathCache = new HashMap<>();

    public void put(String key, RecommendationPath value) {
        recommendationPathCache.put(key, new CacheEntry<>(value, 60));
    }

    public RecommendationPath get(String key) {
        CacheEntry<RecommendationPath> cacheEntry = recommendationPathCache.get(key);
        return (cacheEntry != null) ? cacheEntry.getData() : null;
    }

    public boolean containsKey(String key) {
        return recommendationPathCache.containsKey(key);
    }
}

