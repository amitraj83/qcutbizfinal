package com.qcut.barber.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.apache.commons.lang3.StringUtils;

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
    public static final String DRAG_ADJUSTED_TIME = "dragAdjustedTime";
    private String key;
    private String name;
    private long arrivalTime;
    private long dragAdjustedTime;
    private long expectedWaitingTime;
    private long serviceStartTime;
    private long actualProcessingTime;
    private long lastPositionChangedTime;
    private String status;
    private String preferredBarberKey;
    private int placeInQueue;
    private boolean absent;
    private String channel = "DIRECT";
    private String addedBy ;


    //history
    private long departureTime;
    private long serviceTime;
    private String actualBarberId;

    //old
    private long timeAdded;
    private boolean anyBarber;

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
}
