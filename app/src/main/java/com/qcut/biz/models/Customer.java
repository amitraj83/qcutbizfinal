package com.qcut.biz.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.qcut.biz.util.Status;

import org.apache.commons.lang3.StringUtils;

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
    //    public static final String STATUS = ;
//    public static final String TIME_TO_WAIT = ;
//    public static final String TIME_ADDED = ;
//    public static final String TIME_SERVICE_STARTED = ;
//    public static final String PLACE_IN_QUEUE = ;
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
        return StringUtils.isNotBlank(status) && Status.DONE.name().equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isInProgress() {
        return StringUtils.isNotBlank(status) && Status.PROGRESS.name().equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isInQueue() {
        return StringUtils.isNotBlank(status) && Status.QUEUE.name().equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isRemoved() {
        return StringUtils.isNotBlank(status) && Status.REMOVED.name().equalsIgnoreCase(status);
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
