package com.qcut.biz.views;

public interface ShopDetailsView {
    void setEmail(String email);

    void setPassword(String password);

    void setName(String name);

    void setShopName(String shopName);

    void setAddress1(String addressLine1);

    void setAddress2(String addressLine2);

    void setAvgTimeToCut(long avgTimeToCut);

    void setGmapLink(String gmapLink);

    void selectCityIndex(int cityIndex);

    void showMessage(String msg);

    String getAddressLine1();

    String getPassword();

    String getName();

    String getAddressLine2();

    String getShopName();

    String getGmapLink();

    long getAvgTimeToCut();

    String getSelectedCity();

    String getSelectedCountry();

    String getEmail();
}
