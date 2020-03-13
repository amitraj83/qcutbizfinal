package com.qcut.biz.models;

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
public class ConfigParams {

    public static final String CONFIG_PARAMETERS = "configParameters";
    public static final String LAST_REALLOCATION_TIME = "lastReallocationTime";
    private long lastReallocationTime;
}
