package com.example.tripplanner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TripPlan implements Serializable {
    public String planDescription;
    public HashMap<String, PlanPlace> visitOrder;

    public TripPlan(){

    }

    public TripPlan(Map tripPlanMap) {
        this.planDescription = (String) tripPlanMap.get("planDescription");
        this.visitOrder = (HashMap<String, PlanPlace>) tripPlanMap.get("visitOrder");
    }
}
