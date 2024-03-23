package org.ecocompass.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class ResourceConfig {

    @Bean
    @Qualifier("gtfsFile")
    public Resource gtfsResource() {
        return new ClassPathResource("data/consolidated_gtfs.json");
    }

    @Bean
    @Qualifier("queryDataFile")
    public Resource queryDataResource() {
        return new ClassPathResource("data/query_data.json");
    }

    @Bean
    @Qualifier("roadProcessedDataFile")
    public Resource roadProcessedDataResource() {
        return new ClassPathResource("data/road_map.json");
    }
}
