package org.ecocompass.core.util;

import java.util.List;
import java.util.Map;

public class Constants {
    public static final List<String> GTFS_TYPES = List.of("bus", "dart", "luas");

    public static final Map<String, Integer> AVERAGE_SPEEDS = Map.of(
            "walk", 4,
            "bus", 15,
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
}