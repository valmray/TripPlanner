package com.example.tripplanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    public String userId, firstName, lastName, gender, email,storagePath,url;

    public User() {

    }

    public User(Map userMap) {
        this.userId = (String) userMap.get("userId");
        this.firstName = (String) userMap.get("firstName");
        this.lastName = (String) userMap.get("lastName");
        this.gender = (String) userMap.get("gender");
        this.email = (String) userMap.get("email");
        this.storagePath = (String) userMap.get("storagePath");
        this.url = (String) userMap.get("url");
    }

    public User(String userId, String firstName, String lastName, String emailAddress, String gender, String storagePath, String url) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = emailAddress;
        this.gender = gender;
        this.storagePath = storagePath;
        this.url = url;
    }
}
