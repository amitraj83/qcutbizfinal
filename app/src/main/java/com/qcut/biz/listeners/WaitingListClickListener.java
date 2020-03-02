package com.qcut.biz.listeners;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.R;
import com.qcut.biz.eventbus.EventBus;
import com.qcut.biz.events.RelocationRequestEvent;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberStatus;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerStatus;
import com.qcut.biz.util.DBUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WaitingListClickListener implements View.OnClickListener {

    private Context mContext;
    private String tag;
    private FirebaseDatabase database = null;
    private String userid;
    private Customer selectedCustomer;
    private AlertDialog serviceDoneDialog;
    private TextView custNameTVServiceDone;
    private TextView custNameTVServiceStart;
    private AlertDialog serviceStartDialog;

    //TODO remove ui element from this class
    public WaitingListClickListener(Context mContext, String tag, FirebaseDatabase database, String userid) {
        this.mContext = mContext;
        this.tag = tag;
        this.database = database;
        this.userid = userid;
        final LayoutInflater factory = LayoutInflater.from(mContext);
        if (serviceDoneDialog == null) {
            final View serviceDoneView = factory.inflate(R.layout.service_done_dialog, null);
            custNameTVServiceDone = serviceDoneView.findViewById(R.id.service_done_customer_name);
            serviceDoneDialog = new AlertDialog.Builder(mContext).create();
            serviceDoneDialog.setView(serviceDoneView);
            serviceDoneDialog.show();
            addServiceDoneButtonsClickListener();
            serviceDoneDialog.hide();
        }
        if (serviceStartDialog == null) {
            final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
            custNameTVServiceStart = startServiceView.findViewById(R.id.start_service_cust_name);
            serviceStartDialog = new AlertDialog.Builder(mContext).create();
            serviceStartDialog.setView(startServiceView);
            serviceStartDialog.show();
            addServiceStartButtonsClickListener();
            serviceStartDialog.hide();
        }
    }

    @Override
    public void onClick(View v) {
        changeCustomerStatus(v);
    }

    private void changeCustomerStatus(final View v) {
        final String queueItemStatus = v.findViewById(R.id.cust_status).getTag().toString();
        final String queueItemName = ((TextView) v.findViewById(R.id.cust_name)).getText().toString();
        selectedCustomer = Customer.builder().status(queueItemStatus).name(queueItemName)
                .key(v.getTag().toString()).actualBarberId(tag).build();
        if (queueItemStatus.equalsIgnoreCase(CustomerStatus.PROGRESS.name())) {
            custNameTVServiceDone.setText(queueItemName);
            serviceDoneDialog.show();
            serviceDoneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        } else if (queueItemStatus.equalsIgnoreCase(CustomerStatus.QUEUE.name())) {
            //TODO not need to get barbers if we disable button when barber status is on break or close
            DBUtils.getBarber(database, userid, tag, new OnSuccessListener<Barber>() {
                @Override
                public void onSuccess(Barber barber) {
                    if (BarberStatus.OPEN.name().equalsIgnoreCase(barber.getQueueStatus())) {
                        custNameTVServiceStart.setText(selectedCustomer.getName());
                        serviceStartDialog.show();
                        serviceStartDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    } else {
                        Toast.makeText(mContext, "Cannot start services. May be barber is on break or his queue is stopped.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void addServiceDoneButtonsClickListener() {
        Button yesButton = serviceDoneDialog.findViewById(R.id.yes_done_service);
        Button noButton = serviceDoneDialog.findViewById(R.id.no_done_service);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceDoneDialog.dismiss();
                if (selectedCustomer != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", CustomerStatus.DONE);
                    map.put("expectedWaitingTime", 0);
                    map.put("placeInQueue", -1);
                    Task<Void> voidTask = DBUtils.getDbRefCustomer(database, userid, tag, selectedCustomer.getKey()).updateChildren(map);
                    voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            EventBus.instance().fireEvent(new RelocationRequestEvent());
                        }
                    });
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceDoneDialog.dismiss();
            }
        });
    }

    private void addServiceStartButtonsClickListener() {
        final Button yesButton = serviceStartDialog.findViewById(R.id.yes_start_service);
        Button noButton = serviceStartDialog.findViewById(R.id.no_start_service);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCustomerInProgress();
                serviceStartDialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceStartDialog.dismiss();
            }
        });
    }


    private void setCustomerInProgress() {
        DBUtils.getBarberQueue(database, userid, tag, new OnSuccessListener<BarberQueue>() {
            @Override
            public void onSuccess(BarberQueue barberQueue) {
                boolean isSomeoneInProgress = false;
                Customer c = null;
                for (Customer customer : barberQueue.getCustomers()) {
                    if (CustomerStatus.PROGRESS.name().equalsIgnoreCase(customer.getStatus())) {
                        isSomeoneInProgress = true;
                    }
                    if (customer.getKey().equalsIgnoreCase(selectedCustomer.getKey())) {
                        c = customer;
                    }
                }
                if (!isSomeoneInProgress) {
                    Map<String, Object> map = new HashMap<>();
                    c.setStatus(CustomerStatus.PROGRESS.name());
                    c.setTimeAdded(-1);
                    c.setExpectedWaitingTime(0);
                    c.setServiceStartTime(new Date().getTime());
                    c.setPlaceInQueue(-1);
                    final Task<Void> voidTask = DBUtils.getDbRefCustomer(database, userid, tag, selectedCustomer.getKey()).setValue(c);
                    voidTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            EventBus.instance().fireEvent(new RelocationRequestEvent());
                        }
                    });
                } else {
                    Toast.makeText(mContext, "Cannot start services. A customer is already in progress.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}