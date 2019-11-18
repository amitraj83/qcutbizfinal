package com.qcut.biz.models;

public class ShopQueueModel {
    private String id;
    private String name;
    private String displayTimeToWait;
    private String status;
    private long timeToWait; //in min

    public ShopQueueModel(String id, String name, long timeToWait, String displayTimeToWait, String status) {
        this.id = id;
        this.name = name;
        this.displayTimeToWait = displayTimeToWait;
        this.status = status;
        this.timeToWait = timeToWait;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeToWait() {
        return timeToWait;
    }

    public void setTimeToWait(long timeToWait) {
        this.timeToWait = timeToWait;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayTimeToWait() {
        return displayTimeToWait;
    }

    public void setDisplayTimeToWait(String displayTimeToWait) {
        this.displayTimeToWait = displayTimeToWait;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
