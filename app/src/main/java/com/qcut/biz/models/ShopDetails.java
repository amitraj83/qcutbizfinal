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
public class ShopDetails {

    public static final String SHOP_DETAILS="shopDetails";
    public static final String STATUS="status";
    private String key;
    private String email;
    private String name;
    private String password;
    private String shopName;
    private String phone;
    private String address;
    private String addressLine1;
    private String addressLine2;
    private String avgTimeToCut;
    private String city;
    private String country;
    private String status;
}
