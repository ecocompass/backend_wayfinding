package api.controller;

import org.ecocompass.core.graph.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DesignA2BTest {
    /*
    private PathFinder pathFinder;

    @BeforeEach
    public void setUp() {
        pathFinder = mock(PathFinder.class); // Mock the PathFinder class
    }

    @Test
    public void testFindShortestPathWithPreferences() {
        //Test for only personal vehicle
        UserPreferences userPreferences = new UserPreferences(true, false, false);

        double latitude = 40.7128;
        double longitude = -74.0060;
        double lat = 34.0522;
        double longs = -118.2437;

        Node node1 = new Node(latitude, longitude);
        Node node2 = new Node(lat,longs);

        when(pathFinder.findShortestPath(node1, node2, any(UserPreferences.class)))
                .thenReturn("");

        verify(pathFinder).findShortestPath(node1, node2, eq(userPreferences));

        ReturnedPaths result = pathFinder.findShortestPath(node1, node2, userPreferences);

        assertEquals("", result);
    }

    @Test
    public void testFindShortestPathWithPreferences() {
        // Test for only cycle or walking
        UserPreferences userPreferences = new UserPreferences(false, true, false);

        double latitude = 40.7128;
        double longitude = -74.0060;
        double lat = 34.0522;
        double longs = -118.2437;

        Node node1 = new Node(latitude, longitude);
        Node node2 = new Node(lat,longs);

        when(pathFinder.findShortestPath(node1, node2, any(UserPreferences.class)))
                .thenReturn(new ArrayList<>());

        verify(pathFinder).findShortestPath(node1, node2, eq(userPreferences));

        ReturnedPaths result = pathFinder.findShortestPath(node1, node2, userPreferences);

        assertEquals(new ArrayList<>(), result);
    }


    @Test
    public void testFindShortestPathWithPreferences() {
        //Test for only public transport
        UserPreferences userPreferences = new UserPreferences(false, false, true);

        double latitude = 40.7128;
        double longitude = -74.0060;
        double lat = 34.0522;
        double longs = -118.2437;

        Node node1 = new Node(latitude, longitude);
        Node node2 = new Node(lat,longs);

        when(pathFinder.findShortestPath(node1, node2, any(UserPreferences.class)))
                .thenReturn(new ArrayList<>());

        verify(pathFinder).findShortestPath(node1, node2, eq(userPreferences));

        ReturnedPaths result = pathFinder.findShortestPath(node1, node2, userPreferences);

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    public void testFindShortestPathWithPreferences() {
        //Test for only personal vehicle
        UserPreferences userPreferences = new UserPreferences(true, false);

        double latitude = 40.7128;
        double longitude = -74.0060;
        double lat = 34.0522;
        double longs = -118.2437;

        Node node1 = new Node(latitude, longitude);
        Node node2 = new Node(lat,longs);

        when(pathFinder.findShortestPathReroute(node1, node2, any(UserPreferences.class)))
                .thenReturn(new ArrayList<>());

        verify(pathFinder).findShortestPathReroute(node1, node2, eq(userPreferences));

        ReturnedPaths result = pathFinder.findShortestPathReroute(node1, node2, userPreferences);

        assertEquals(new ArrayList<>(), result);
    }
     */

}
