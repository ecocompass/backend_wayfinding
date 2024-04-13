package org.ecocompass.core.util;

import java.util.List;
import java.util.Map;

public class Constants {
    public static final List<String> GTFS_TYPES = List.of("bus", "dart", "luas");

    public static final Map<String, Integer> AVERAGE_SPEEDS = Map.of(
            "walk", 4,
            "bike", 12,
            "bus", 15,
            "car", 16,
            "luas", 25,
            "dart", 60
    );

    public static final Map<String, Map<Integer, List<Integer>>> SERVICE_ID_MAPPINGS = Map.of(
            "luas", Map.of(
                    0, List.of(129, 132),
                    1, List.of(129, 132),
                    2, List.of(129, 132),
                    3, List.of(129, 132),
                    4, List.of(129, 131, 132),
                    5, List.of(130),
                    6, List.of(132)
            ),
            "bus", Map.of(
                    0, List.of(270, 360),
                    1, List.of(270, 360),
                    2, List.of(270, 360),
                    3, List.of(270, 360),
                    4, List.of(270, 360),
                    5, List.of(61, 361),
                    6, List.of(362)
            ),
            "dart", Map.of(
                    0, List.of(319, 320, 332, 339, 342, 343, 345, 346, 348, 350, 351, 352, 356, 359, 360, 361, 363, 364, 365, 366, 367, 370, 371),
                    1, List.of(58, 320, 332, 339, 342, 343, 345, 346, 348, 350, 351, 352, 356, 359, 360, 361, 363, 364, 365, 366, 368, 370, 371),
                    2, List.of(320, 332, 339, 342, 343, 345, 346, 348, 350, 351, 352, 356, 359, 360, 361, 363, 364, 365, 366, 368, 370, 371),
                    3, List.of(57, 320, 332, 339, 342, 343, 345, 346, 348, 350, 351, 352, 356, 359, 360, 361, 363, 364, 365, 366, 368, 370, 371),
                    4, List.of(56, 320, 332, 339, 342, 343, 345, 346, 348, 349, 359, 360, 362, 363, 364, 365, 366, 368, 369, 371),
                    5, List.of(20, 59, 321, 333, 334, 336, 337, 338, 343, 346, 347, 348, 350, 351, 359, 360, 363, 364, 365, 366, 369, 370, 371),
                    6, List.of(81, 162, 318, 325, 344, 358)
            )
    );

    public static final Map<String, Integer> K_NEAREST_MAPPINGS = Map.of(
            "bus", 15,
            "dart", 5,
            "luas", 5
    );

    public static final Map<String, Double> EMISSION_MAPPINGS = Map.of(
        "walk", 0.0,   // Walking has negligible emissions
        "bike", 0.0,   // Cycling has negligible emissions
        "bus", 0.103,   // kg CO2 per km (estimated based on buses in other European cities)
        "luas", 0.092, // kg CO2 per km (estimated based on trams in other European cities)
        "dart", 0.045, // kg CO2 per km (estimated based on electric trains in other countries)
        "car", 0.153   // kg CO2 per km (average car emission)
    );

    public static final Map<String, Double> CALORIES_MAPPINGS = Map.of(
        "walk", 3.5,
        "bike", 8.0,
        "bus", 0.0,
        "luas", 0.0,
        "dart", 0.0,
        "car", 0.0
    );

}