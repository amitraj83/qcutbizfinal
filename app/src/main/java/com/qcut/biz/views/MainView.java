package com.qcut.biz.views;

public interface MainView {

    void setShopStatusOnline();

    void setShopStatusOffline();

    void showMessage(String msg);

    String getCurrentStatus();

    void navigateToId(int id);

    void startActivity();

    void closeDrawer();
}
