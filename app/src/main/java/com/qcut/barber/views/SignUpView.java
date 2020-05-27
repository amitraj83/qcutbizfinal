package com.qcut.barber.views;

public interface SignUpView {

    void showMessage(String msg);

    String getEmail();

    String getPassword();

    void startActivity(Class clazz);

    String getShopContactName();

    String getShopName();
}
