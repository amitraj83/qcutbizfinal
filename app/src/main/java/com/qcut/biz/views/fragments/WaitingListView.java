package com.qcut.biz.views.fragments;

import com.qcut.biz.models.Customer;
import com.qcut.biz.ui.waiting_list.BarberSelectionArrayAdapter;

import java.util.List;

public interface WaitingListView {

    void showMessage(String msg);

    void showAddCustomerDialog();

    String getSelectedBarberKey();

    void setYesButtonEnable(boolean b);

    String getEnteredCustomerName();

    void hideAddCustomerDialog();

    void startDoorBell();

    void setBarberList(BarberSelectionArrayAdapter barberSelectionAdapter);

    void updateNextCustomerView(String customerName, String customerKey);

    void updateAndRefreshQueue(List<Customer> customers);

    void updateBarberStatus(boolean onBreak);
}
