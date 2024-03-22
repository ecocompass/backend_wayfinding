package org.ecocompass.api.response;
import lombok.Getter;
import lombok.Setter;
import org.ecocompass.api.utility.RecommendationPath;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TransitionRouteResponse {
    List<RecommendationPath> recommendationList;

    public TransitionRouteResponse(){
        this.recommendationList = new ArrayList<>();
    }

    public void addRecommendation(RecommendationPath recommendationPath){
        this.recommendationList.add(recommendationPath);
    }

    public List<RecommendationPath> getRecommendationList() {
        return this.recommendationList;
    }

}
