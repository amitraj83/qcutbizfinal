package com.qcut.barber.views;

import com.qcut.barber.adaptors.BarberSelectionArrayAdapter;
import com.qcut.barber.models.Customer;

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

    void clearEnteredCustomerName();
}
