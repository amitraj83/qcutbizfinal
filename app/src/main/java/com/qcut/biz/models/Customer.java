package com.qcut.biz.models;

import com.google.firebase.database.Exclude;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    private String key;
    private long timeAdded;
    private int timeToWait;
    private boolean anyBarber;
    private String customerId;
    private String name;
    private int placeInQueue;
    private int skipcount;
    private String status;
    private long timeFirstAddedInQueue;
    private long timeServiceStarted;

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("timeAdded", timeAdded);
        result.put("timeToWait", timeToWait);
        result.put("anyBarber", anyBarber);
        result.put("customerId", customerId);
        result.put("name", name);
        result.put("placeInQueue", placeInQueue);
        result.put("skipcount", skipcount);
        result.put("status", status);
        result.put("timeFirstAddedInQueue", timeFirstAddedInQueue);
        result.put("timeServiceStarted", timeServiceStarted);

        return result;
    }

}
