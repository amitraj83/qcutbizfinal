package com.qcut.biz.views.activities;

import com.qcut.biz.activity.StartActivity;

public interface MainView {

    void setShopStatusOnline();

    void setShopStatusOffline();

    void showMessage(String msg);

    String getCurrentStatus();

    void navigateToId(int id);

    void closeDrawer();

    void startActivity(Class<StartActivity> startActivityClass);
}
