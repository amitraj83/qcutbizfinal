package com.qcut.biz.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.apache.commons.lang3.StringUtils;

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
    public static final String EXPECTED_WAITING_TIME = "expectedWaitingTime";
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
    private long timeToWait;
    private boolean anyBarber;
    private String customerId;
    private long timeFirstAddedInQueue;
    private long timeServiceStarted;

    @Exclude
    public boolean isDone() {
        return StringUtils.isNotBlank(status) && CustomerStatus.DONE.name().equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isInProgress() {
        return StringUtils.isNotBlank(status) && CustomerStatus.PROGRESS.name().equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isInQueue() {
        return StringUtils.isNotBlank(status) && CustomerStatus.QUEUE.name().equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isRemoved() {
        return StringUtils.isNotBlank(status) && CustomerStatus.REMOVED.name().equalsIgnoreCase(status);
    }


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
