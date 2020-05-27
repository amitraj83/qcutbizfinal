package com.qcut.barber.views;

public interface SignInView {

    void showMessage(String msg);

    String getEmail();

    String getPassword();

    void startActivity();
}
