package com.qcut.biz.models;

import com.google.firebase.database.Exclude;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Barber {
    public static final String BARBERS = "barbers";
    public static final String QUEUE_STATUS = "queueStatus";
    private String key;
    private String name;
    private String imagePath;
    private String queueStatus;
    private long avgTimeToCut;

    @Exclude
    public boolean isStopped() {
        return StringUtils.isNotBlank(queueStatus) && BarberStatus.STOP.name().equalsIgnoreCase(queueStatus);
    }

    @Exclude
    public boolean isOpen() {
        return StringUtils.isNotBlank(queueStatus) && BarberStatus.OPEN.name().equalsIgnoreCase(queueStatus);
    }


    @Exclude
    public boolean isOnBreak() {
        return StringUtils.isNotBlank(queueStatus) && BarberStatus.BREAK.name().equalsIgnoreCase(queueStatus);
    }
}
