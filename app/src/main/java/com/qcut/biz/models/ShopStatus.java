package com.qcut.biz.models;

import org.apache.commons.lang3.StringUtils;

public enum ShopStatus {
    ONLINE, OFFLINE;

    public static ShopStatus fromValue(String status) {
        if (StringUtils.isNotBlank(status)) {
            for (ShopStatus s : values()) {
                if (s.name().equalsIgnoreCase(status)) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Invalid status: " + String.valueOf(status));
    }
}
