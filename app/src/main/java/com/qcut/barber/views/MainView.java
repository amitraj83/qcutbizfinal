package com.qcut.barber.views;

import com.qcut.barber.views.activities.StartActivity;

public interface MainView {

    void setShopStatusOnline();

    void setShopStatusOffline();

    void showMessage(String msg);

    String getCurrentStatus();

    void navigateToId(int id);

    void closeDrawer();

    void startActivity(Class<StartActivity> startActivityClass);
}
