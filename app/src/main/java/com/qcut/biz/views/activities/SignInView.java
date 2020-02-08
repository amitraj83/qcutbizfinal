package com.qcut.biz.views.activities;

public interface SignInView {

    void showMessage(String msg);

    String getEmail();

    String getPassword();

    void startActivity();
}
