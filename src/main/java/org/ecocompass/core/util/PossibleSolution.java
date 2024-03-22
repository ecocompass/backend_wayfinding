package org.ecocompass.core.util;

import lombok.Getter;
import lombok.Setter;
import org.ecocompass.core.K_DTree.KdNode;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class PossibleSolution {
    private KdNode startNode;

    private KdNode endNode;

    private Set<String> transitionSet;

    public PossibleSolution(KdNode startNode, KdNode endNode, Set<String> transitionSet) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.transitionSet = transitionSet;
    }

    public PossibleSolution() {
        this.startNode = null;
        this.endNode = null;
        this.transitionSet = new HashSet<>();
    }

    public KdNode getStartNode(){
        return this.startNode;
    }

    public KdNode getEndNode(){
        return this.endNode;
    }

    public Set<String> getTransitionSet(){
        return this.transitionSet;
    }
}

