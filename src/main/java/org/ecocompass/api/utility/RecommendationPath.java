package org.ecocompass.api.utility;

import lombok.Getter;
import lombok.Setter;
import org.ecocompass.core.util.Cache.RecommendationsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Service
public class RecommendationPath {

    private static final SecureRandom random = new SecureRandom();

    private final RecommendationsCache recommendationsCache;

    private String recommendationId;
    private String transitions;
    private List<PathWithMode> modePathList;

    public RecommendationPath(RecommendationsCache recommendationsCache) {
        this.modePathList = new ArrayList<>();
        this.transitions = "-";
        this.recommendationsCache = recommendationsCache;
        this.recommendationId = generateUniqueId(10);
    }

    public String generateUniqueId(int length) {
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int digit = random.nextInt(10);
                sb.append(digit);
            }
            String id = sb.toString();
            if (!recommendationsCache.containsKey(id)) {
                return id;
            }
        }
    }

    public void addPath(PathWithMode path){
        this.modePathList.add(path);
    }
    public void addTransition(String mode){
        this.transitions += mode + "-";
    }

    public List<PathWithMode> getModePathList(){
        return this.modePathList;
    }

    public String getTransitions() { return this.transitions; }

    public String getRecommendationId() { return this.recommendationId; }

}
