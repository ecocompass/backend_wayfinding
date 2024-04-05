package org.ecocompass.core.PathFinder;

import org.apache.commons.math3.util.FastMath;
import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.graph.Node;
import org.ecocompass.core.util.CacheEntry;
import org.ecocompass.core.util.Constants;
import org.ecocompass.core.util.FoundSolution;
import org.ecocompass.core.util.PossibleSolution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class FinderCore {

    private static final Logger logger = LogManager.getLogger(FinderCore.class);

    private Map<String, CacheEntry<KdNode>> nodeFromIdCache = new HashMap<>();
    private Map<String, CacheEntry<List<double[]>>> shortestPathCache = new HashMap<>();

    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;

        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2) +
                FastMath.cos(lat1) * FastMath.cos(lat2) *
                        FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2);
        double c = 2 * FastMath.atan2(FastMath.sqrt(a), FastMath.sqrt(1 - a));
        return R * c;
    }

    public KDTree buildKDTree(JSONObject jsonObject) {
        List<KdNode> nodes = new ArrayList<>();

        for (String stopId : jsonObject.keySet()) {
            KdNode kdNode = getKdNode(jsonObject, stopId);
            nodes.add(kdNode);
        }
        return new KDTree(nodes);
    }

    public List<KdNode> getNearestNodes(KDTree tree, double[] point, int k) {
        PriorityQueue<KDTree.NodeWithDistance> pq = tree.kNearestNeighbors(tree.getRoot(), point, 0, k, new PriorityQueue<>());
        List<KdNode> neighbors = new ArrayList<>();
        for (KDTree.NodeWithDistance nwd : pq) {
            neighbors.add(nwd.node);
        }
        return neighbors;
    }

    private KdNode getNodeFromID(String id, JSONObject roadMap) {
        CacheEntry<KdNode> cachedNode = nodeFromIdCache.get(id);
        if (cachedNode != null && !cachedNode.isExpired()) {
            return cachedNode.getData();
        }
        KdNode newNode = getKdNode(roadMap, id);
        nodeFromIdCache.put(id, new CacheEntry<>(newNode, 5));
        return newNode;
    }

    private static KdNode getKdNode(JSONObject roadMap, String id) {
        JSONObject nodeInfo = roadMap.getJSONObject(id);
        double lat = nodeInfo.getDouble("lat");
        double lon = nodeInfo.getDouble("lon");
        String name;
        if (!nodeInfo.isNull("name")) {
            name = nodeInfo.getString("name");
        } else {
            name = "nodePoint";
        }
        double[] coordinates = {lat, lon};
        Node node = new Node(lat, lon);
        return new KdNode(coordinates, id, node, name);
    }

    public List<double[]> getShortestPathRoad(KdNode start, KdNode goal, JSONObject roadMap) {
        String cacheKey = Arrays.toString(start.getCoordinates()) + Arrays.toString(goal.getCoordinates());
        CacheEntry<List<double[]>> cacheEntry = shortestPathCache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            return cacheEntry.getData();
        }

        PriorityQueue<NodeWrapper> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, String> cameFrom = new HashMap<>();

        Map<String, Double> gScore = new HashMap<>();
        gScore.put(start.getNodeID(), 0.0);

        Map<String, Double> fScore = new HashMap<>();
        fScore.put(start.getNodeID(), haversineDistance(start.getNode().latitude, start.getNode().longitude,
                goal.getNode().latitude, goal.getNode().longitude));

        openSet.add(new NodeWrapper(0.0, start.getNodeID()));

        while (!openSet.isEmpty()) {
            NodeWrapper current = openSet.poll();

            if (current.nodeID.equals(goal.getNodeID())) {
                // Reconstruct path
                List<double[]> path = new ArrayList<>();
                String currentID = goal.getNodeID();
                while (currentID != null) {
                    KdNode currentNode = getNodeFromID(currentID, roadMap);
                    path.add(new double[]{currentNode.getCoordinates()[0], currentNode.getCoordinates()[1]});
                    currentID = cameFrom.get(currentID);
                }
                Collections.reverse(path);
                shortestPathCache.put(cacheKey, new CacheEntry<>(path, 1));
                return path;
            }

            JSONArray currentNeighbors = roadMap.getJSONObject(String.valueOf(current.nodeID)).getJSONArray("neighbors");
            List<String> currentNeighborsList = new ArrayList<>();
            for (int i = 0; i < currentNeighbors.length(); i++) {
                currentNeighborsList.add(currentNeighbors.getString(i));
            }

            for (String neighborID : currentNeighborsList) {
                double tentativeGScore = gScore.getOrDefault(current.nodeID, Double.POSITIVE_INFINITY) +
                        haversineDistance(getNodeFromID(current.nodeID, roadMap).getNode().latitude,
                                getNodeFromID(current.nodeID, roadMap).getNode().longitude,
                                getNodeFromID(neighborID, roadMap).getNode().latitude,
                                getNodeFromID(neighborID, roadMap).getNode().longitude);

                if (tentativeGScore < gScore.getOrDefault(neighborID, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighborID, current.nodeID);
                    gScore.put(neighborID, tentativeGScore);
                    fScore.put(neighborID, tentativeGScore + haversineDistance(getNodeFromID(neighborID, roadMap).getNode().latitude,
                            getNodeFromID(neighborID, roadMap).getNode().longitude, goal.getNode().latitude, goal.getNode().longitude));

                    openSet.add(new NodeWrapper(fScore.get(neighborID), neighborID));
                }
            }
        }

        shortestPathCache.put(cacheKey, new CacheEntry<>(null, 1));
        return null;
    }

    public int findClosestPointIndex(List<double[]> route, double[] point) {
        double minDistance = Double.POSITIVE_INFINITY;
        int closestPointIndex = 0;
        for (int i = 0; i < route.size(); i++) {
            double[] routePoint = route.get(i);
            double distance = haversineDistance(routePoint[0], routePoint[1], point[0], point[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closestPointIndex = i;
            }
        }
        return closestPointIndex;
    }

    public List<double[]> getRouteSection(List<double[]> route, double[] startPoint, double[] endPoint) {
        int startIndex = findClosestPointIndex(route, startPoint);
        int endIndex = findClosestPointIndex(route, endPoint);

        if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }

        return route.subList(startIndex, endIndex + 1);
    }

    public static List<double[]> getModeShape(JSONArray jsonArray) {
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray coordinates = jsonArray.getJSONArray(i);
            double[] point = new double[coordinates.length()];
            for (int j = 0; j < coordinates.length(); j++) {
                point[j] = coordinates.getDouble(j);
            }
            result.add(point);
        }
        return result;
    }

    public List<FoundSolution> getTransitRoutes(List<KdNode> nearestStopsStart, List<KdNode> nearestStopsEnd,
                                                   JSONObject transitMap, String mode) {

        List<FoundSolution> connectedSolutions = new ArrayList<>();
        for (KdNode startStop : nearestStopsStart) {
            for (KdNode endStop : nearestStopsEnd) {
                List<PossibleSolution> possibleSolutions = getPossibleSolutions(transitMap, mode, startStop, endStop);

                ZoneId dublinZone = ZoneId.of("Europe/Dublin");
                ZonedDateTime timeNow = ZonedDateTime.now(dublinZone);
                DayOfWeek dayOfWeek = timeNow.getDayOfWeek();
                int weekdayValue = dayOfWeek.getValue();

                List<Integer> validServiceIds = Constants.SERVICE_ID_MAPPINGS.get(mode).get(weekdayValue-1);

                for (PossibleSolution solution : possibleSolutions) {
                    for (String route : solution.getTransitionSet()) {
                        String route0 = route + "_0";
                        JSONObject modeRoutes = transitMap.getJSONObject(mode + "_routes");
                        Set<String> modeRoutesSet = modeRoutes.keySet();
                        if (modeRoutesSet.contains(route0)) {
                            List<String> stopIds = getStopIds(modeRoutes, route0);
                            int startIndex = stopIds.indexOf(String.valueOf(solution.getStartNode().getNodeID()));
                            int endIndex = stopIds.indexOf(String.valueOf(solution.getEndNode().getNodeID()));
                            Integer currentServiceId = 0;
                            double distance = 0;
                            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                List<double[]> trace = new ArrayList<>();
                                JSONObject modeRouteFromStops = getModeRouteFromStops(transitMap, mode, solution, route);
                                Set<String> modeRouteFromStopsSet = modeRouteFromStops.keySet();
                                currentServiceId = getCurrentServiceId(validServiceIds, modeRouteFromStopsSet, currentServiceId);
                                if (currentServiceId != 0) {
                                    addRouteWaitTimes(transitMap, mode, connectedSolutions, startStop, endStop, timeNow,
                                            solution, route, currentServiceId, distance, route0, trace, modeRouteFromStops);
                                    continue;
                                }
                                else {
                                    logger.info("  {route_0}: No route found using service id");
                                }
                            }

                            String route1 = route + "_1";
                            if (modeRoutesSet.contains(route1)) {
                                stopIds = getStopIds(modeRoutes, route1);
                                startIndex = stopIds.indexOf(String.valueOf(solution.getStartNode().getNodeID()));
                                endIndex = stopIds.indexOf(String.valueOf(solution.getEndNode().getNodeID()));
                                currentServiceId = 0;
                                distance = 0;
                                if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                    List<double[]> trace = new ArrayList<>();
                                    JSONObject modeRouteFromStops = getModeRouteFromStops(transitMap, mode, solution, route);
                                    Set<String> modeRouteFromStopsSet = modeRouteFromStops.keySet();
                                    currentServiceId = getCurrentServiceId(validServiceIds, modeRouteFromStopsSet, currentServiceId);
                                    if (currentServiceId != 0) {
                                        addRouteWaitTimes(transitMap, mode, connectedSolutions, startStop, endStop, timeNow,
                                                solution, route, currentServiceId, distance, route1, trace, modeRouteFromStops);
                                    } else {
                                        logger.info("  {route_1}: No route found using service id");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return connectedSolutions;
    }

    private static Integer getCurrentServiceId(List<Integer> validServiceIds, Set<String> modeRouteFromStopsSet,
                                               Integer currentServiceId) {
        for (Integer id : validServiceIds) {
            if (modeRouteFromStopsSet.contains(String.valueOf(id))) {
                currentServiceId = id;
                break;
            }
        }
        return currentServiceId;
    }

    private static JSONObject getModeRouteFromStops(JSONObject transitMap, String mode, PossibleSolution solution, String route) {
        return transitMap.getJSONObject(mode + "_stops").
                getJSONObject(String.valueOf(solution.getStartNode().getNodeID())).
                getJSONObject("routes").getJSONObject(route);
    }

    private static List<String> getStopIds(JSONObject modeRoutes, String route0) {
        List<String> stopIds = new ArrayList<>();
        JSONArray jsonArray = modeRoutes.getJSONArray(route0);
        for (int i = 0; i < jsonArray.length(); i++) {
            stopIds.add(jsonArray.getString(i));
        }
        return stopIds;
    }

    private static List<PossibleSolution> getPossibleSolutions(JSONObject transitMap, String mode, KdNode startStop, KdNode endStop) {
        JSONObject startStopNode = transitMap.getJSONObject(mode + "_stops").getJSONObject(String.valueOf(startStop.getNodeID()));
        JSONObject endStopNode = transitMap.getJSONObject(mode + "_stops").getJSONObject(String.valueOf(endStop.getNodeID()));

        List<PossibleSolution> possibleSolutions = new ArrayList<>();
        if (startStopNode != null && endStopNode != null) {
            JSONObject startStopRoutes = startStopNode.getJSONObject("routes");
            JSONObject endStopRoutes = endStopNode.getJSONObject("routes");

            Set<String> startRouteNumbers = startStopRoutes.keySet();
            Set<String> endRouteNumbers = endStopRoutes.keySet();

            Set<String> transitionRouteNumbers = new HashSet<>(startRouteNumbers);
            transitionRouteNumbers.retainAll(endRouteNumbers);

            if (!transitionRouteNumbers.isEmpty()) {
                PossibleSolution foundSolution = new PossibleSolution(startStop, endStop, transitionRouteNumbers);
                possibleSolutions.add(foundSolution);
            }

        }
        return possibleSolutions;
    }

    private void addRouteWaitTimes(JSONObject transitMap, String mode, List<FoundSolution> connectedSolutions,
                                   KdNode startStop, KdNode endStop, ZonedDateTime timeNow, PossibleSolution solution,
                                   String route, Integer currentServiceId, double distance, String route1,
                                   List<double[]> trace, JSONObject modeRouteFromStops) {
        List<Long> waitTimes = new ArrayList<>();
        JSONArray service = modeRouteFromStops.getJSONArray(String.valueOf(currentServiceId));
        List<LocalTime> vehicleTimeList = getVehicleLocalTimes(service);
        LocalTime currentTime = timeNow.toLocalTime();
        for (LocalTime vehicleTime : vehicleTimeList) {
            if (vehicleTime.isBefore(currentTime)) {
                continue;
            }

            long waitTime = vehicleTime.toSecondOfDay() - currentTime.toSecondOfDay();
            waitTimes.add(waitTime/60);
        }
        Collections.sort(waitTimes);
        List<Long> nextThreeWaitTimes;
        if (waitTimes.size() >= 3) {
            nextThreeWaitTimes = waitTimes.subList(0, 3);
        } else {
            nextThreeWaitTimes = new ArrayList<>(waitTimes);
        }
        addConnectedSolutions(transitMap, mode, startStop, endStop, solution, route, nextThreeWaitTimes, currentServiceId,
                trace, route1, distance, connectedSolutions);
    }

    private void addConnectedSolutions(JSONObject transitMap, String mode, KdNode startStop, KdNode endStop,
                                       PossibleSolution solution, String route, List<Long> nextThreeWaitTimes,
                                       Integer currentServiceId, List<double[]> trace, String route1, double distance,
                                       List<FoundSolution> connectedSolutions) {
        if (!nextThreeWaitTimes.isEmpty()) {
            trace.add(startStop.getCoordinates());
            JSONArray modeShapes = transitMap.getJSONObject(mode + "_shapes").getJSONArray(route1);
            List<double[]> modeShape = getModeShape(modeShapes);
            trace.addAll(getRouteSection(modeShape, startStop.getCoordinates(), endStop.getCoordinates()));
            trace.add(endStop.getCoordinates());

            distance+= getRouteDistance(trace);

            FoundSolution foundSolution = new FoundSolution();
            foundSolution.setPossibleSolution(solution);
            foundSolution.setRoute(route);
            foundSolution.setModeNumber(String.valueOf(currentServiceId));
            foundSolution.setDistance(distance);
            foundSolution.setTraceCoordinates(trace);
            foundSolution.setWaitTime(nextThreeWaitTimes);
            connectedSolutions.add(foundSolution);
        }
    }

    private List<LocalTime> getVehicleLocalTimes(JSONArray service) {
        return service.toList().stream()
                .map(Object::toString)
                .map(timeString -> {
                    String[] timeSplit = timeString.split(":");
                    int hour = Integer.parseInt(timeSplit[0]);
                    if (hour >= 24) {
                        hour -= 24;
                    }
                    return LocalTime.of(hour, Integer.parseInt(timeSplit[1]), Integer.parseInt(timeSplit[2]));
                })
                .collect(Collectors.toList());
    }

    public double getRouteDistance(List<double[]> route) {
        double totalDistance = 0;
        double[] prev = null;
        for (double[] coords : route) {
            if (prev != null) {
                totalDistance += haversineDistance(coords[0], coords[1], prev[0], prev[1]);
            }
            prev = coords;
        }
        return totalDistance;
    }

    private static class NodeWrapper {
        double fScore;
        String nodeID;

        NodeWrapper(double fScore, String nodeID) {
            this.fScore = fScore;
            this.nodeID = nodeID;
        }
    }
}