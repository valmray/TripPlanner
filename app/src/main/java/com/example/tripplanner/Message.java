package com.example.tripplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Message implements Serializable {
    public String email, date, message, imageUrl, messageId, tripId, userId;

    public Message()
    {

    }

    public Message (Map messageMap) {

        this.email = (String) messageMap.get("email");
        this.message = (String) messageMap.get("message");
        this.imageUrl = (String) messageMap.get("imageUrl");
        this.messageId = (String) messageMap.get("messageId");
        this.tripId = (String) messageMap.get("tripId");
        this.date = (String) messageMap.get("date");
        this.userId = (String) messageMap.get("userId");
    }
}
