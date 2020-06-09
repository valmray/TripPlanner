package com.example.tripplanner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlanPlace implements Serializable {
    //Might just need place id
    public Place place;
    public String position;
    public PlanPlace(){

    }
    public PlanPlace(String position, Place place){
        this.position = position;
        this.place = place;
    }

    public PlanPlace(Map tripPlanMap) {
        this.position = (String) tripPlanMap.get("position");
        this.place = (Place) tripPlanMap.get("place");
    }
}
