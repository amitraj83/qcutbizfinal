package com.qcut.biz.views;

import com.qcut.biz.models.ServiceAvailable;

import java.util.List;

public interface ShopAddServicesView {
    void hideDialog();

    void showMessage(String msg);

    String getServiceName();

    String getServicePrice();

    void setServiceAvailable(List<ServiceAvailable> serviceAvailables);
}
