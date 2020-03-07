package com.qcut.biz.views;

import android.net.Uri;
import android.widget.ImageView;

import com.qcut.biz.adaptors.CustomerViewAdapter;
import com.qcut.biz.models.Barber;

import java.util.List;

public interface CustomerView {

    void setCustomerViewAdaptor(CustomerViewAdapter adapter);

    void showMessage(String s);

    void startDoorBell();
}
