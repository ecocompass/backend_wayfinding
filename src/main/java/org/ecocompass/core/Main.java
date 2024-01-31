package org.ecocompass.core;

public class Main {
    public static void main(String[] args) {
        System.out.println("Ecocompass Core Engine");

        Overpass overpass = new Overpass();
        String response = overpass.queryLocation("IE", "Dublin", "Rathmines");
        overpass.saveQueryOutput(response, "query_data.xml");
    }
}
