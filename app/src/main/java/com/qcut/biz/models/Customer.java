package com.qcut.biz.models;

import java.util.Comparator;

public class Customer  {
    String key;
    long timeAdded;
    int timeToWait;

    public Customer(String key, long timeAdded, int timeToWait) {
        this.key = key;
        this.timeAdded = timeAdded;
        this.timeToWait = timeToWait;
    }

    public int getTimeToWait() {
        return timeToWait;
    }

    public void setTimeToWait(int timeToWait) {
        this.timeToWait = timeToWait;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }


}
