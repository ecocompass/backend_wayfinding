package org.ecocompass.core.util.Cache;

import org.ecocompass.core.K_DTree.KdNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KdNodeCache {
    private final Map<String, KdNode> cache = new HashMap<>();

    public KdNode get(String nodeId) {
        return cache.get(nodeId);
    }

    public void put(String nodeId, KdNode node) {
        cache.put(nodeId, node);
    }
}