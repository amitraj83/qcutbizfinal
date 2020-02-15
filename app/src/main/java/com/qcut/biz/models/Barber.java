package com.qcut.biz.models;

import com.google.firebase.database.Exclude;
import com.qcut.biz.util.Status;

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

    @Exclude
    public boolean isStopped() {
        return StringUtils.isNotBlank(queueStatus) && Status.STOP.name().equalsIgnoreCase(queueStatus);
    }

    @Exclude
    public boolean isOpen() {
        return StringUtils.isNotBlank(queueStatus) && Status.OPEN.name().equalsIgnoreCase(queueStatus);
    }


    @Exclude
    public boolean isOnBreak() {
        return StringUtils.isNotBlank(queueStatus) && Status.BREAK.name().equalsIgnoreCase(queueStatus);
    }
}
