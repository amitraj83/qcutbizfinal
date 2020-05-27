package com.qcut.barber.presenters;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.barber.eventbus.EventBus;
import com.qcut.barber.events.RelocationRequestEvent;
import com.qcut.barber.models.Customer;
import com.qcut.barber.models.CustomerStatus;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.views.CustomerOptionsView;

public class CustomerOptionPresenter {

    FirebaseDatabase database;
    String userid;
    private String barberKey;
    private CustomerOptionsView view;

    public CustomerOptionPresenter(FirebaseDatabase database, String userid, String barberKey) {
        this.database = database;
        this.userid = userid;
        this.barberKey = barberKey;
    }

    public void onAssignToAnyClick(final String customerKey) {
        DBUtils.getCustomer(database, userid, barberKey, customerKey, new OnSuccessListener<Customer>() {
            @Override
            public void onSuccess(Customer customer) {
                customer.setAnyBarber(true);
                DBUtils.getDbRefBarberQueue(database, userid, barberKey).child(customerKey).setValue(customer)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                EventBus.instance().fireEvent(new RelocationRequestEvent());
                            }
                        });
            }
        });
    }

    public void onRemoveCustomerClick(final String customerKey) {
        DBUtils.getCustomer(database, userid, barberKey, customerKey, new OnSuccessListener<Customer>() {
            @Override
            public void onSuccess(Customer customer) {
                if (customer.isInProgress()) {
                    view.showMessage("In progress customer can't be removed.");
                    return;
                }
                customer.setStatus(CustomerStatus.REMOVED.name());
                DBUtils.getDbRefBarberQueue(database, userid, barberKey).child(customerKey)
                        .setValue(customer).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        EventBus.instance().fireEvent(new RelocationRequestEvent());
                    }
                });
            }
        });
    }

    public void setView(CustomerOptionsView view) {
        this.view = view;
    }
}
