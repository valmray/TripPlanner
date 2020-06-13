package com.example.tripplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Trip implements Serializable {
    //user id for the user who created the trip
    public String title, city, trip_id, user_id, description, date, url, privacy;
    public Double latitude, longitude;
    public ArrayList<User> people;
    public ArrayList<Message> chat;

    public Trip(String userId,
                String tripId,
                String title,
                String description,
                ArrayList<User> people,
                String date,
                String city,
                Double latitude,
                Double longitude,
                String url,
                ArrayList<Message> chat,
                String privacy) {
        this.user_id = userId;
        this.trip_id = tripId;
        this.title = title;
        this.description = description;
        this.people = people;
        this.date = date;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.chat = chat;
        this.privacy = privacy;

    }

    public Trip (Map tripMap) {

        this.user_id = (String) tripMap.get("user_id");
        this.trip_id = (String) tripMap.get("trip_id");
        this.title = (String) tripMap.get("title");
        this.description = (String) tripMap.get("description");
        this.people = (ArrayList<User>) tripMap.get("people");
        this.date = (String) tripMap.get("date");
        this.city = (String) tripMap.get("city");
        this.latitude =  (Double) tripMap.get("latitude");
        this.longitude = (Double) tripMap.get("longitude");
        this.url = (String) tripMap.get("url");
        this.chat = (ArrayList<Message>) tripMap.get("chat");
        this.privacy = (String) tripMap.get("privacy");

    }
}
