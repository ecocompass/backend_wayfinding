package org.ecocompass.core;

import org.ecocompass.core.K_DTree.KDTree;
import org.ecocompass.core.graph.Graph;
import org.ecocompass.core.overpass.Overpass;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Overpass overpass() {
        return new Overpass();
    }

    @Bean
    public Graph graph(Overpass overpass) throws Exception {
        String response = overpass.queryLocation(Map.of("country", "IE", "county", "County Dublin"));
        // "neighborhood", "Grosvenor Square"
        overpass.saveQueryOutput(response, "query_data.json");
        //String response = overpass.loadSavedQueryOutput("query_data.json");
        return overpass.createGraphFromOverpassQuery(response);
    }

    @Bean
    public KDTree kdTree(Graph graph, Overpass overpass) {
        return overpass.createTreeFromGraph(graph);
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
