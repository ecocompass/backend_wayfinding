package org.ecocompass.core.PathFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ecocompass.api.response.TransitionRouteResponse;
import org.ecocompass.api.utility.PathWithMode;
import org.ecocompass.api.utility.RecommendationPath;
import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.util.Constants;
import org.ecocompass.core.util.FoundSolution;
import org.ecocompass.core.util.TransitRoute;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Query {

    private final FinderCore finderCore;
    private final KDTree kdTreeRoad;
    private final KDTree kdTreeBus;
    private final KDTree kdTreeLuas;
    private final KDTree kdTreeDart;
    private final KDTree kdTreeBike;
    private final JSONObject transitMap;
    private final JSONObject roadMap;

    private static final Logger logger = LogManager.getLogger(Query.class);

    private final Map<String, List<TransitRoute>> transitRoutesCache;

    public Query(@Qualifier("kdTreeRoad") KDTree kdTreeRoad,
                 @Qualifier("kdTreeBus") KDTree kdTreeBus, @Qualifier("kdTreeLuas") KDTree kdTreeLuas,
                 @Qualifier("kdTreeDart") KDTree kdTreeDart, @Qualifier("kdTreeBike") KDTree kdTreeBike) throws IOException {
        this.kdTreeRoad = kdTreeRoad;
        this.kdTreeBus = kdTreeBus;
        this.kdTreeLuas = kdTreeLuas;
        this.kdTreeDart = kdTreeDart;
        this.kdTreeBike = kdTreeBike;

        String transitData = Files.readString(Path.of(
                "C:\\Users\\rahul\\IdeaProjects\\backend_wayfinding\\src\\main\\java\\org\\ecocompass\\core\\data\\consolidated_gtfs.json"));
        this.transitMap = new JSONObject(transitData);

        String roadData = Files.readString(Path.of(
                "C:\\Users\\rahul\\IdeaProjects\\backend_wayfinding\\src\\main\\java\\org\\ecocompass\\core\\data\\road_map.json"));
        this.roadMap = new JSONObject(roadData);

        finderCore = new FinderCore();
        transitRoutesCache = new HashMap<>();
    }

    public TransitionRouteResponse getTransitRecommendations(double[] start, double[] end){

        List<List<List<TransitRoute>>> transitionRoutes = getTransitRoutes(start, end);
        TransitionRouteResponse transitionRouteResponse = new TransitionRouteResponse();
        RecommendationPath recommendation = new RecommendationPath();
        for(List<TransitRoute> busLuasRoute : transitionRoutes.get(0)){
            if(Objects.equals(busLuasRoute.get(0).getMode(), "luas")){
                addPathModeWalkMode(busLuasRoute, 0, recommendation, true);
                addPathModeRoute(busLuasRoute, 0, recommendation, "luas");
            } else {
                addPathModeWalkMode(busLuasRoute, 0, recommendation, true);
                addPathModeRoute(busLuasRoute, 0, recommendation, "bus");
                addPathModeWalkNext(busLuasRoute, recommendation);
                addPathModeRoute(busLuasRoute, 1, recommendation, "luas");
            }
            int lastIndex = busLuasRoute.size()-1;
            if(Objects.equals(busLuasRoute.get(lastIndex).getMode(), "luas")){
                addPathModeWalkMode(busLuasRoute, lastIndex, recommendation, false);
            } else {
                addPathModeWalkMode(busLuasRoute, lastIndex, recommendation, true);
                addPathModeRoute(busLuasRoute, lastIndex, recommendation, "bus");
                addPathModeWalkMode(busLuasRoute,  lastIndex, recommendation, false);
            }
        }

        transitionRouteResponse.addRecommendation(recommendation);

        recommendation = new RecommendationPath();
        for(List<TransitRoute> busRoute: transitionRoutes.get(1)){
            addPathModeWalkMode(busRoute,0, recommendation, true);
            addPathModeRoute(busRoute, 0, recommendation, "bus");
            addPathModeWalkMode(busRoute, 0, recommendation, false);
        }
        transitionRouteResponse.addRecommendation(recommendation);

        recommendation = new RecommendationPath();
        for(List<TransitRoute> busRoute: transitionRoutes.get(2)){
            addPathModeWalkMode(busRoute, 0, recommendation, true);
            addPathModeRoute(busRoute, 0, recommendation, "bus");
            addPathModeWalkNext(busRoute, recommendation);
            addPathModeRoute(busRoute, 1, recommendation, "bus");
            addPathModeWalkMode(busRoute, 1, recommendation, false);
        }
        transitionRouteResponse.addRecommendation(recommendation);
        return transitionRouteResponse;
    }

    private static void addPathModeRoute(List<TransitRoute> route, int lastIndex,
                                     RecommendationPath recommendation, String mode) {
        PathWithMode path = new PathWithMode();
        path.setMode(mode);
        path.setStartStopName(route.get(lastIndex).getFoundSolution().getPossibleSolution().getStartNode().getName());
        path.setEndStopName(route.get(lastIndex).getFoundSolution().getPossibleSolution().getEndNode().getName());
        path.setModeNumber(route.get(lastIndex).getFoundSolution().getModeNumber());
        path.setRouteNumber(route.get(lastIndex).getFoundSolution().getRoute());
        path.setTimeStamp(route.get(lastIndex).getFoundSolution().getWaitTime().get(0));
        path.setPathPointList(swapCoordinates(route.get(lastIndex).getFoundSolution().getTraceCoordinates()));
        path.setDistance(route.get(lastIndex).getFoundSolution().getDistance());
        recommendation.addPath(path);
        recommendation.addTransition(mode);
    }


    private void addPathModeWalkNext(List<TransitRoute> route, RecommendationPath recommendation) {
        KdNode walkStartNextEnd = kdTreeRoad.findNode(
                route.get(0).getFoundSolution().getPossibleSolution().getEndNode().getCoordinates());
        KdNode walkEndNextStart = kdTreeRoad.findNode(
                route.get(1).getFoundSolution().getPossibleSolution().getStartNode().getCoordinates());
        List<double[]> walkPath = finderCore.getShortestPathRoad(walkStartNextEnd, walkEndNextStart, roadMap);
        double walkDistance = finderCore.getRouteDistance(walkPath);

        PathWithMode walkPathNextStart = new PathWithMode();
        walkPathNextStart.setMode("walk");
        walkPathNextStart.setPathPointList(swapCoordinates(walkPath));
        walkPathNextStart.setDistance(walkDistance);
        recommendation.addPath(walkPathNextStart);
        recommendation.addTransition("walk");
    }

    private static void addPathModeWalkMode(List<TransitRoute> route, int lastIndex,
                                     RecommendationPath recommendation, boolean start) {
        PathWithMode walkPath = new PathWithMode();
        walkPath.setMode("walk");
        if(start) {
            walkPath.setPathPointList(swapCoordinates(route.get(lastIndex).getPathListStart()));
            walkPath.setDistance(route.get(lastIndex).getDistanceStart());
        } else {
            walkPath.setPathPointList(swapCoordinates(route.get(lastIndex).getPathListEnd()));
            walkPath.setDistance(route.get(lastIndex).getDistanceEnd());
        }
        recommendation.addPath(walkPath);
        recommendation.addTransition("walk");
    }

    private static List<double[]> swapCoordinates(List<double[]> coordinates) {
        List<double[]> swappedCoordinates = new ArrayList<>();
        for (double[] coordinate : coordinates) {
            double[] swappedCoordinate = new double[]{coordinate[1], coordinate[0]};
            swappedCoordinates.add(swappedCoordinate);
        }
        return swappedCoordinates;
    }

    public List<List<List<TransitRoute>>> getTransitRoutes(double[] start, double[] end) {
        double straightLineDistance = finderCore.haversineDistance(start[0], start[1], end[0], end[1]);
        logger.debug("Straight Line distance: {} ", straightLineDistance);

        KdNode nodeStart = kdTreeRoad.findNode(start);
        KdNode nodeEnd = kdTreeRoad.findNode(end);

        List<double[]> roadRouteStartEnd = finderCore.getShortestPathRoad(nodeStart, nodeEnd, roadMap);
        double directRoadDistance = finderCore.getRouteDistance(roadRouteStartEnd);
        logger.info("A-star distance: {}", directRoadDistance);

        CompletableFuture<List<List<TransitRoute>>> luasSolsFuture =
                CompletableFuture.supplyAsync(() -> getLuasSols(start, end));
        CompletableFuture<List<List<TransitRoute>>> busSolsFuture =
                CompletableFuture.supplyAsync(() -> getBusSols(start, end, directRoadDistance));
        CompletableFuture<List<List<TransitRoute>>> busSplitSolsFuture =
                CompletableFuture.supplyAsync(() -> getBusSplitSols(start, end, roadRouteStartEnd));

        try {
            CompletableFuture.allOf(luasSolsFuture, busSolsFuture, busSplitSolsFuture).join();
        } catch (Exception e) {
            logger.error("Error in getTransitRoutes MultiThreading Launching: " + e.getMessage());
        }

        List<List<TransitRoute>> luasSols = null;
        List<List<TransitRoute>> busSols = null;
        List<List<TransitRoute>> busSplitSols = null;
        try {
            luasSols = luasSolsFuture.get();
            busSols = busSolsFuture.get();
            busSplitSols = busSplitSolsFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in getTransitRoutes MultiThreading Collecting: " + e.getMessage());
        }

        List<List<List<TransitRoute>>> result = new ArrayList<>();
        result.add(luasSols);
        result.add(busSols);
        result.add(busSplitSols);
        return result;
    }

    private List<List<TransitRoute>> getBusSplitSols(double[] start, double[] end, List<double[]> roadRouteStartEnd) {
        List<List<TransitRoute>> busSplitSols = new ArrayList<>();
        double[] midStop = roadRouteStartEnd.get(roadRouteStartEnd.size() / 2);
        int k_fh = Constants.K_NEAREST_MAPPINGS.get("bus");
        List<TransitRoute> firstHalves = new ArrayList<>();
        while(firstHalves.isEmpty() && k_fh <= 30){
            logger.debug("------------------ BUS SPLIT FH -----------------------");
            firstHalves= getTransitRoutes(start, midStop, "bus", k_fh);
            if(!firstHalves.isEmpty()){
                sortSolList(firstHalves);
                firstHalves = firstHalves.subList(0, Math.min(5, firstHalves.size()));

                for(TransitRoute busRoute: firstHalves){
                    double[] endStop = busRoute.getFoundSolution().getPossibleSolution().getEndNode().getCoordinates();
                    List<TransitRoute> secondHalves = new ArrayList<>();
                    int k_sh = Constants.K_NEAREST_MAPPINGS.get("bus");
                    while(secondHalves.isEmpty() && k_sh <=30){
                        logger.debug("------------------ BUS SPLIT SH -----------------------");
                        secondHalves= getTransitRoutes(endStop, end, "bus", k_sh);
                        if(!secondHalves.isEmpty()) {
                            sortSolList(secondHalves);
                            for(TransitRoute route: secondHalves){
                                List<TransitRoute> combinedRoute = new ArrayList<>();
                                combinedRoute.add(busRoute);
                                combinedRoute.add(route);
                                busSplitSols.add(combinedRoute);
                            }

                        } else {
                            k_sh *= 2;
                        }
                    }

                }

            } else {
                k_fh *= 2;
            }
        }

        sortSolsList(busSplitSols);
        return busSplitSols.subList(0, Math.min(1, busSplitSols.size()));
    }

    private List<List<TransitRoute>> getBusSols(double[] start, double[] end, double directRoadDistance) {
        List<List<TransitRoute>> busSols = new ArrayList<>();
        List<TransitRoute> busRoutes;
        int k = Constants.K_NEAREST_MAPPINGS.get("bus");
        while(busSols.isEmpty() && k<=30){
            logger.debug("------------------ BUS SHRINK -----------------------");
            busRoutes = getTransitRoutes(start, end, "bus", k);
            if(!busRoutes.isEmpty()){
                for(TransitRoute busRoute : busRoutes){
                    if(busRoute.getFoundSolution().getDistance() < (1.1 * directRoadDistance)){
                        List<TransitRoute> route = new ArrayList<>();
                        route.add(busRoute);
                        busSols.add(route);
                    }
                }
            } else {
                k *= 2;
            }
        }

        sortSolsList(busSols);
        return busSols.subList(0, Math.min(1, busSols.size()));
    }

    private List<List<TransitRoute>> getLuasSols(double[] start, double[] end) {
        List<List<TransitRoute>> luasSols = new ArrayList<>();
        logger.debug("------------------ LUAS -----------------------");
        List<TransitRoute> luasRoutes = getTransitRoutes(start, end, "luas", 0);

        for(TransitRoute luasRoute: luasRoutes){
            List<TransitRoute> luasSol = new ArrayList<>();
            List<TransitRoute> busSol = new ArrayList<>();
            List<TransitRoute> busRoutes = getTransitRoutes(start,
                    luasRoute.getFoundSolution().getPossibleSolution().getStartNode().getCoordinates(), "bus", 0);
            updateLuasSol(busRoutes, luasRoute.getDistanceStart(), busSol, luasSol);
            luasSol.add(luasRoute);

            busSol = new ArrayList<>();
            busRoutes = getTransitRoutes(luasRoute.getFoundSolution().getPossibleSolution().getEndNode().getCoordinates(),
                    end, "bus",0);
            updateLuasSol(busRoutes, luasRoute.getDistanceEnd(), busSol, luasSol);
            luasSols.add(luasSol);
        }
        sortSolsList(luasSols);
        return luasSols.subList(0, Math.min(1, luasSols.size()));
    }

    private static void updateLuasSol(List<TransitRoute> busRoutes, double luasRoute, List<TransitRoute> busSol,
                                      List<TransitRoute> luasSol) {
        if (!busRoutes.isEmpty()) {
            for (TransitRoute busRoute : busRoutes) {
                if (busRoute.getDistanceStart() + busRoute.getDistanceEnd() < luasRoute - 0.5) {
                    busSol.add(busRoute);
                }
            }
        }
        if (!busSol.isEmpty()) {
            sortSolList(busSol);
            luasSol.add(busSol.get(0));
        }
    }

    private static void sortSolList(List<TransitRoute> busSol) {
        if(!busSol.isEmpty()){
            busSol.sort((route1, route2) -> {
                double distance1 = route1.getFoundSolution().getDistance() +
                        route1.getDistanceStart() +
                        route1.getDistanceEnd();
                double distance2 = route2.getFoundSolution().getDistance() +
                        route2.getDistanceStart() +
                        route2.getDistanceEnd();
                return Double.compare(distance1, distance2);
            });
        }
    }

    private static void sortSolsList(List<List<TransitRoute>> busSols) {
        if(!busSols.isEmpty()){
            busSols.sort(Comparator.comparingDouble(list -> {
                if (list.size() >= 2) {
                    return list.get(0).getFoundSolution().getDistance() + list.get(1).getFoundSolution().getDistance() +
                            list.get(0).getDistanceStart() + list.get(1).getDistanceStart() +
                            list.get(0).getDistanceEnd() + list.get(1).getDistanceEnd();
                } else {
                    return 0;
                }
            }));
        }
    }


    public List<TransitRoute> getTransitRoutes(double[] start, double[] end, String mode, int k) {
        String cacheKey = Arrays.toString(start) + Arrays.toString(end) + mode + k;
        if (transitRoutesCache.containsKey(cacheKey)) {
            return transitRoutesCache.get(cacheKey);
        }

        KdNode NodeStart = kdTreeRoad.findNode(start);
        KdNode NodeEnd = kdTreeRoad.findNode(end);

        if(k == 0) {
            k = Constants.K_NEAREST_MAPPINGS.get(mode);
        }
        KDTree treeRef;

        if ("bus".equals(mode)) {
            treeRef = kdTreeBus;
        } else if ("luas".equals(mode)) {
            treeRef = kdTreeLuas;
        } else if ("dart".equals(mode)) {
            treeRef = kdTreeDart;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        List<KdNode> nearestStopsStart = finderCore.getNearestNodes(treeRef, NodeStart.getCoordinates(), k);
        List<KdNode> nearestStopsEnd = finderCore.getNearestNodes(treeRef, NodeEnd.getCoordinates(), k);
        List<FoundSolution> transitRoutes = finderCore.getTransitRoutes(nearestStopsStart, nearestStopsEnd, transitMap, mode);


        List<TransitRoute> transitroutes = new ArrayList<>();
        for(FoundSolution solution: transitRoutes){
            KdNode roadNodeStart = kdTreeRoad.findNode(solution.getPossibleSolution().getStartNode().getCoordinates());
            KdNode roadNodeEnd = kdTreeRoad.findNode(solution.getPossibleSolution().getEndNode().getCoordinates());

            List<double[]> shortestPathListStart = finderCore.getShortestPathRoad(NodeStart, roadNodeStart, roadMap);
            double pathDistanceStart = finderCore.getRouteDistance(shortestPathListStart);

            List<double[]> shortestPathListEnd = finderCore.getShortestPathRoad(NodeEnd, roadNodeEnd, roadMap);
            double pathDistanceEnd = finderCore.getRouteDistance(shortestPathListEnd);

            TransitRoute transitRoute = new TransitRoute();
            transitRoute.setMode(mode);
            transitRoute.setFoundSolution(solution);
            transitRoute.setPathListStart(shortestPathListStart);
            transitRoute.setPathListEnd(shortestPathListEnd);
            transitRoute.setDistanceStart(pathDistanceStart);
            transitRoute.setDistanceEnd(pathDistanceEnd);
            transitroutes.add(transitRoute);
        }
        transitRoutesCache.put(cacheKey, transitroutes);
        return transitroutes;
    }
}
