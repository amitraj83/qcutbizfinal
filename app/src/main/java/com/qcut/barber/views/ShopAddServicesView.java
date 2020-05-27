package com.qcut.barber.views;

import com.qcut.barber.models.ServiceAvailable;

import java.util.List;

public interface ShopAddServicesView {
    void hideDialog();

    void showMessage(String msg);

    String getServiceName();

    String getServicePrice();

    void setServiceAvailable(List<ServiceAvailable> serviceAvailables);
}
