package com.qcut.biz.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

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
@IgnoreExtraProperties
public class ShopDetails {

    public static final String SHOP_DETAILS = "shopDetails";
    public static final String STATUS = "status";
    public static final String EMAIL = "email";
    private String key;
    private String email;
    private String name;
    private String password;
    private String shopName;
    private String phone;
    private String address;
    private String addressLine1;
    private String addressLine2;
    private long avgTimeToCut;
    private String gmapLink;
    private String city;
    private String country;
    private String status;

    @Exclude
    public boolean isOnline() {
        return StringUtils.isNotBlank(status) && ShopStatus.ONLINE.name().equalsIgnoreCase(status);
    }
}
