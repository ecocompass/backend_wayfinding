package org.ecocompass.api;

import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.PathFinder.Query;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.overpass.Overpass;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(scanBasePackages = {"org.ecocompass.api", "org.ecocompass.config", "org.ecocompass.core"})
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Overpass overpass() {
        return new Overpass();
    }

    @Bean
    public Graph graph(Overpass overpass, @Qualifier("queryDataFile") Resource queryDataResource) throws Exception {
        String response = overpass.loadSavedQueryOutput(queryDataResource);
        return overpass.createGraphFromOverpassQuery(response);
    }

    @Bean(name = "kdTreeRoad")
    public KDTree kdTreeRoad(Graph graph, Overpass overpass) throws IOException {
        return overpass.createTreeFromGraph(graph, "road");
    }

    @Bean(name = "kdTreeBus")
    public KDTree kdTreeBus(Graph graph, Overpass overpass,
                            @Qualifier("gtfsFile") Resource gtfsResource,
                            @Qualifier("roadProcessedDataFile") Resource roadProcessedResource) throws IOException {
        return overpass.createTreeFromGraph("bus", gtfsResource, roadProcessedResource);
    }

    @Bean(name = "kdTreeLuas")
    public KDTree kdTreeLuas(Graph graph, Overpass overpass,
                             @Qualifier("gtfsFile") Resource gtfsResource,
                             @Qualifier("roadProcessedDataFile") Resource roadProcessedResource) throws IOException {
        return overpass.createTreeFromGraph("luas", gtfsResource, roadProcessedResource);
    }

    @Bean(name = "kdTreeDart")
    public KDTree kdTreeDart(Graph graph, Overpass overpass,
                             @Qualifier("gtfsFile") Resource gtfsResource,
                             @Qualifier("roadProcessedDataFile") Resource roadProcessedResource) throws IOException {
        return overpass.createTreeFromGraph("dart", gtfsResource, roadProcessedResource);
    }

    @Bean(name = "kdTreeBike")
    public KDTree kdTreeBike(Graph graph, Overpass overpass) throws IOException {
        return overpass.createTreeFromGraph(graph,"bike");
    }

    @Bean
    public Query query(@Qualifier("kdTreeRoad") KDTree kdTreeRoad,
                       @Qualifier("kdTreeBus") KDTree kdTreeBus,
                       @Qualifier("kdTreeLuas") KDTree kdTreeLuas,
                       @Qualifier("kdTreeDart") KDTree kdTreeDart,
                       @Qualifier("kdTreeBike") KDTree kdTreeBike,
                       @Qualifier("gtfsFile") Resource gtfsResource,
                       @Qualifier("roadProcessedDataFile") Resource roadProcessedResource) throws IOException {
        return new Query(kdTreeRoad, kdTreeBus, kdTreeLuas, kdTreeDart, kdTreeBike, gtfsResource, roadProcessedResource);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            try {
                factory.setAddress(InetAddress.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
