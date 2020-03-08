package com.qcut.biz.models;

import com.google.firebase.database.IgnoreExtraProperties;

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
@IgnoreExtraProperties
public class ServiceAvailable {

    public static final String SERVICES_AVAILABLE = "servicesAvailable";
    private String key;
    private String serviceName;
    private String servicePrice;
}
