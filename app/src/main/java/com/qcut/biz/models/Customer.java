package com.qcut.biz.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

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
@IgnoreExtraProperties
public class Customer {
    public static final String BARBER_WAITING_QUEUES = "barberWaitingQueues";
    private String key;
    private String name;
    private long arrivalTime;
    private long expectedWaitingTime;
    private long serviceStartTime;
    private long actualProcessingTime;
    private long lastPositionChangedTime;
    private String status;
    private String preferredBarberKey;
    private int placeInQueue;
    private boolean absent;

    //history
    private long departureTime;
    private long serviceTime;
    private String actualBarberId;

    //old
    private long timeAdded;
    private int timeToWait;
    private boolean anyBarber;
    private String customerId;
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
        result.put("status", status);
        result.put("timeFirstAddedInQueue", timeFirstAddedInQueue);
        result.put("timeServiceStarted", timeServiceStarted);

        return result;
    }

}
