package org.ecocompass.api.utility;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RecommendationPath {
    private String transitions;
    private List<PathWithMode> modePathList;

    public RecommendationPath() {
        this.modePathList = new ArrayList<>();
        this.transitions = "-";
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

}
