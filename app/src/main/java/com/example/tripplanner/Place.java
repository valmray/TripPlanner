package com.example.tripplanner;

import java.io.Serializable;

public class Place implements Serializable {
    public String name;
    public String type;
    public String id;
    public double latitude, longitude;

    public Place() {

    }

    public Place(String name, String type, String id, double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
