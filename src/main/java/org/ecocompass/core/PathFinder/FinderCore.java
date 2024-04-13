package org.ecocompass.core.PathFinder;

import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.K_DTree.KdNode;
import org.ecocompass.core.Reroute.TrafficCheck;
import org.ecocompass.core.graph.Node;
import org.ecocompass.core.util.*;
import org.ecocompass.core.util.Cache.CacheEntry;
import org.ecocompass.core.util.Cache.KdNodeCache;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class FinderCore {

    @Autowired
    private KdNodeCache nodeCache;
    private final TrafficCheck trafficCheck;
    private final DistanceUtility distanceUtility;
    private static final Logger logger = LogManager.getLogger(FinderCore.class);
    private final Map<String, CacheEntry<List<double[]>>> shortestPathCache = new ConcurrentHashMap<>();

    @Autowired
    public FinderCore(TrafficCheck trafficCheck){
        this.trafficCheck = trafficCheck;
        this.nodeCache = new KdNodeCache();
        this.distanceUtility = new DistanceUtility();
    }


    public KDTree buildKDTree(JSONObject jsonObject, String mode) {
        List<KdNode> nodes = new ArrayList<>();
        for (String stopId : jsonObject.keySet()) {
            KdNode kdNode = getKdNode(jsonObject, stopId);
            if (Objects.equals(mode, "road") || Objects.equals(mode, "bike")) {
                nodeCache.put(stopId, kdNode);
            }
            nodes.add(kdNode);
        }
        return new KDTree(nodes);
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

    public List<KdNode> getNearestNodes(KDTree tree, double[] point, int k) {
        PriorityQueue<KDTree.NodeWithDistance> pq = tree.kNearestNeighbors(tree.getRoot(), point, 0, k, new PriorityQueue<>());
        List<KdNode> neighbors = new ArrayList<>();
        for (KDTree.NodeWithDistance nwd : pq) {
            neighbors.add(nwd.node);
        }
        return neighbors;
    }

    private KdNode getNodeFromID(String id, JSONObject roadMap) {
        KdNode node = nodeCache.get(id);
        if (node == null) {
            node = getKdNode(roadMap, id);
            nodeCache.put(id, node);
        }
        return node;
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
        fScore.put(start.getNodeID(), distanceUtility.haversineDistance(start.getNode().latitude, start.getNode().longitude,
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

            KdNode startNode = getNodeFromID(current.nodeID, roadMap);
            for (String neighborID : currentNeighborsList) {
                KdNode neighbourNode = getNodeFromID(neighborID, roadMap);
                double tentativeGScore = gScore.getOrDefault(current.nodeID, Double.POSITIVE_INFINITY) +
                        distanceUtility.haversineDistance(startNode.getNode().latitude, startNode.getNode().longitude,
                                neighbourNode.getNode().latitude, neighbourNode.getNode().longitude);
                if (tentativeGScore < gScore.getOrDefault(neighborID, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighborID, current.nodeID);
                    gScore.put(neighborID, tentativeGScore);
                    fScore.put(neighborID, tentativeGScore + distanceUtility.haversineDistance(neighbourNode.getNode().latitude,
                            neighbourNode.getNode().longitude, goal.getNode().latitude, goal.getNode().longitude));

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
            double distance = distanceUtility.haversineDistance(routePoint[0], routePoint[1], point[0], point[1]);
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

    public List<FoundSolution> getTransitRoutes(List<KdNode> nearestStopsStart, List<KdNode> nearestStopsEnd, JSONObject transitMap,
                                                String mode, Long waitTime, KdNode NodeStart, JSONObject roadMap) {
        List<FoundSolution> connectedSolutions = new ArrayList<>();
        JSONObject modeRoutes = transitMap.getJSONObject(mode + "_routes");
        Set<String> modeRoutesSet = modeRoutes.keySet();

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (KdNode startStop : nearestStopsStart) {
                for (KdNode endStop : nearestStopsEnd) {
                    Future<?> future = executorService.submit(() -> addStartStopComboRoutes(transitMap, mode, waitTime,
                            NodeStart, roadMap, startStop, endStop, modeRoutesSet, modeRoutes, connectedSolutions));
                    futures.add(future);
                }
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        return connectedSolutions;
    }

    private void addStartStopComboRoutes(JSONObject transitMap, String mode, Long waitTime, KdNode NodeStart, JSONObject roadMap, KdNode startStop, KdNode endStop, Set<String> modeRoutesSet, JSONObject modeRoutes, List<FoundSolution> connectedSolutions) {
        Duration waitTimeOffset = Duration.ofSeconds(waitTime);
        List<PossibleSolution> possibleSolutions = getPossibleSolutions(transitMap, mode, startStop, endStop);

        ZoneId dublinZone = ZoneId.of("Europe/Dublin");
        ZonedDateTime timeNow = ZonedDateTime.now(dublinZone);
        DayOfWeek dayOfWeek = timeNow.getDayOfWeek();
        int weekdayValue = dayOfWeek.getValue();

        List<Integer> validServiceIds = Constants.SERVICE_ID_MAPPINGS.get(mode).get(weekdayValue-1);
        for (PossibleSolution solution : possibleSolutions) {
            for (String route : solution.getTransitionSet()) {
                processRoute(transitMap, mode, NodeStart, roadMap, startStop, endStop, solution,
                        route, modeRoutesSet, modeRoutes, validServiceIds, connectedSolutions, waitTimeOffset, 0);
                processRoute(transitMap, mode, NodeStart, roadMap, startStop, endStop, solution,
                        route, modeRoutesSet, modeRoutes, validServiceIds, connectedSolutions, waitTimeOffset, 1);
            }
        }
    }

    private void processRoute(JSONObject transitMap, String mode, KdNode NodeStart, JSONObject roadMap, KdNode startStop, KdNode endStop,
                              PossibleSolution solution, String route, Set<String> modeRoutesSet, JSONObject modeRoutes,
                              List<Integer> validServiceIds, List<FoundSolution> connectedSolutions, Duration waitTimeOffset,
                              int i) {
        String routeId = route + "_" + i;
        addAllRouteDetails(transitMap, mode, NodeStart, roadMap, startStop, endStop, solution, route, modeRoutesSet, routeId, modeRoutes, validServiceIds, connectedSolutions, waitTimeOffset);
    }

    private void addAllRouteDetails(JSONObject transitMap, String mode, KdNode NodeStart, JSONObject roadMap,
                                    KdNode startStop, KdNode endStop, PossibleSolution solution, String route,
                                    Set<String> modeRoutesSet, String routeId, JSONObject modeRoutes, List<Integer> validServiceIds,
                                    List<FoundSolution> connectedSolutions, Duration waitTimeOffset) {
        if (modeRoutesSet.contains(routeId)) {
            List<String> stopIds = getStopIds(modeRoutes, routeId);
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
                    addRouteWaitTimes(transitMap, mode, connectedSolutions, startStop, endStop,
                            solution, route, currentServiceId, distance, routeId, trace,
                            modeRouteFromStops, waitTimeOffset, NodeStart, roadMap);
                } else {
                    logger.info(routeId + " : No route found using service id");
                }
            }
        }
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

    public Duration getWaitTimeInSeconds(double distance, String mode) {
        double averageSpeed = Constants.AVERAGE_SPEEDS.get(mode);
        double timeInHours = distance / averageSpeed;
        long timeInSeconds = (long) (timeInHours * 3600);
        return Duration.ofSeconds(timeInSeconds);
    }

    private void addRouteWaitTimes(JSONObject transitMap, String mode, List<FoundSolution> connectedSolutions,
                                   KdNode startStop, KdNode endStop, PossibleSolution solution,
                                   String route, Integer currentServiceId, double distance, String route1,
                                   List<double[]> trace, JSONObject modeRouteFromStops, Duration  waitTimeOffset,
                                   KdNode NodeStart, JSONObject roadMap) {
        List<Long> waitTimes = new ArrayList<>();
        JSONArray service = modeRouteFromStops.getJSONArray(String.valueOf(currentServiceId));
        List<LocalTime> vehicleTimeList = getVehicleLocalTimes(service);
        LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Dublin"));
        List<double[]> shortestPathListStart = getShortestPathRoad(NodeStart, startStop, roadMap);
        if(shortestPathListStart != null) {
            double pathDistanceStart = getRouteDistance(shortestPathListStart);
            waitTimeOffset = waitTimeOffset.plus(getWaitTimeInSeconds(pathDistanceStart, "walk"));
        }
        LocalTime timeAtStop = currentTime.plus(waitTimeOffset);
        for (LocalTime vehicleTime : vehicleTimeList) {
            if (vehicleTime.isBefore(timeAtStop)) {
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

    private synchronized void addConnectedSolutions(JSONObject transitMap, String mode, KdNode startStop, KdNode endStop,
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
            if(trafficCheck.isIncidentOnPath(trace) == null) {
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
                totalDistance += distanceUtility.haversineDistance(coords[0], coords[1], prev[0], prev[1]);
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