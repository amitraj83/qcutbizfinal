package com.qcut.barber.views;

import com.qcut.barber.adaptors.CustomerViewAdapter;

public interface CustomerView {

    void setCustomerViewAdaptor(CustomerViewAdapter adapter);

    void showMessage(String s);

    void startDoorBell();
}
