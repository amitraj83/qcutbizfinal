package com.qcut.barber.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.qcut.barber.eventbus.EventBus;
import com.qcut.barber.events.BarberQueuesChangeEvent;
import com.qcut.barber.events.BarberStatusChangeEvent;
import com.qcut.barber.events.QueueTabSelectedEvent;
import com.qcut.barber.events.RelocationRequestEvent;
import com.qcut.barber.models.Barber;
import com.qcut.barber.models.BarberQueue;
import com.qcut.barber.models.BarberStatus;
import com.qcut.barber.util.Constants;
import com.qcut.barber.util.DBUtils;
import com.qcut.barber.util.LogUtils;
import com.qcut.barber.views.WaitingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WaitingPresenter implements BarberStatusChangeEvent.BarberStatusChangeEventHandler,
        BarberQueuesChangeEvent.BarberQueuesChangeEventHandler {

    private String userid;
    private WaitingView view;
    private SharedPreferences preferences;

    private Context context;
    private FirebaseDatabase database;
    private StorageReference storageReference;
    private Map<String, BarberQueue> queueMap = new HashMap<>();

    public WaitingPresenter(WaitingView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userid = preferences.getString("userid", null);
        EventBus.instance().registerHandler(BarberQueuesChangeEvent.TYPE, this);
        EventBus.instance().registerHandler(BarberStatusChangeEvent.TYPE, this);
    }

    public void initializeTab() {
        DBUtils.getBarbersQueues(database, userid, new OnSuccessListener<List<BarberQueue>>() {
            @Override
            public void onSuccess(List<BarberQueue> barberQueues) {
                for (BarberQueue bq : barberQueues) {
                    if (bq.getBarber().isStopped()) {
                        continue;
                    }
                    queueMap.put(bq.getBarberKey(), bq);
                    if (!view.isTabExists(bq.getBarberKey())) {
                        //barber queue not exists
                        view.addBarberQueueTab(bq.getBarber());
                    }
                }
            }
        });
    }

    public void onStopButtonClick() {
        final String selectedBarberKey = view.getSelectedTabId();
        final String dialogTitle, confirmText;
        final BarberStatus queueStatus;
        if (Constants.STOPPED.equalsIgnoreCase(view.getStopButtonText())) {
            //is queue is already stopped then new status will be open
            view.updateButtonToStopped();
            dialogTitle = "Resume Queue";
            confirmText = "Want to resume your Queue? After this, customers will be added to your queue";
            queueStatus = BarberStatus.OPEN;
        } else {
            view.updateButtonToStopQ();
            dialogTitle = "Stop Queue";
            confirmText = "Want to stop your queue? After this, no customer will be added to your queue";
            queueStatus = BarberStatus.STOP;
        }
        DBUtils.getBarber(database, userid, selectedBarberKey, new OnSuccessListener<Barber>() {
            @Override
            public void onSuccess(Barber barber) {
                String dialogText = "Dear Barber " + barber.getName();
                view.showBarberStatusConfirmationDialog(dialogTitle, dialogText, confirmText, queueStatus, barber.getImagePath());
            }
        });

    }

    public void getDownloadUrlAndSetInView(final ImageView photo, final String imagePath) {
        StorageReference child = storageReference.child(imagePath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    view.setPhotoUrl(photo, task.getResult());
                } else {
                    String message = task.getException().getMessage();
                    view.showMessage(message);
                    LogUtils.error("Problem while getting image url at path: {0}. {1}", imagePath, message);
                }
            }
        });
    }

    public void onBarberStatusChangeYesClick(final BarberStatus status) {
        Map<String, Object> map = new HashMap<>();
        map.put(Barber.QUEUE_STATUS, status.name());
        DBUtils.getDbRefBarber(database, userid, view.getSelectedTabId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (status == BarberStatus.OPEN || status == BarberStatus.STOP) {
                    //reallocate customers if barber is closing or opening queue
                    EventBus.instance().fireEvent(new RelocationRequestEvent());
                }
            }
        });
        view.hideDialog();
        if (status == BarberStatus.BREAK) {
            view.setButtonToBarberOnBreak();
        } else {
            view.resetBarberBreakButton();
        }
        if (status == BarberStatus.STOP) {
            view.updateButtonToStopped();
        } else {
            view.updateButtonToStopQ();
        }
    }

    public void onTakeBreakButtonClick() {
        final String selectedBarberKey = view.getSelectedTabId();
        DBUtils.getBarber(database, userid, selectedBarberKey, new OnSuccessListener<Barber>() {
            @Override
            public void onSuccess(Barber barber) {
                final String dialogTitle, confirmText;
                final BarberStatus newStatus;
                if (barber.isOnBreak()) {
                    //if already on break, new status will be open
                    dialogTitle = "Resume Work";
                    confirmText = "Do you want to resume your work?";
                    newStatus = BarberStatus.OPEN;
                } else {
                    dialogTitle = "Take A Break";
                    confirmText = "Do you want to take a break from work?";
                    newStatus = BarberStatus.BREAK;
                }
                String dialogText = "Dear Barber " + barber.getName();
                view.showBarberStatusConfirmationDialog(dialogTitle, dialogText, confirmText, newStatus, barber.getImagePath());
            }
        });
    }


    public void onAddBarberQueueTabClick() {
        DBUtils.getBarbers(database, userid, new OnSuccessListener<Map<String, Barber>>() {

            @Override
            public void onSuccess(Map<String, Barber> barbersMap) {
                final List<Barber> remainingBarbers = new ArrayList<>();
                for (Barber b : barbersMap.values()) {
                    if (!view.isTabExists(b.getKey())) {
                        //barber tab not exists
                        remainingBarbers.add(b);
                    }
                }
                if (remainingBarbers.size() > 0) {
                    view.showBarberSelectionDialog(remainingBarbers);
                } else {
                    view.showMessage("No barber to add.");
                    LogUtils.info("No barber to add.");
                }
            }
        });
    }

    public void onBarberQueueTabSelected(final String selectedBarberKey) {
        DBUtils.getBarber(database, userid, selectedBarberKey, new OnSuccessListener<Barber>() {
            @Override
            public void onSuccess(Barber barber) {
                if (barber.getQueueStatus().equalsIgnoreCase(BarberStatus.BREAK.name())) {
                    view.setButtonToBarberOnBreak();
                } else {
                    view.resetBarberBreakButton();
                }

                if (barber.getQueueStatus().equalsIgnoreCase(BarberStatus.STOP.name())) {
                    view.updateButtonToStopped();
                } else {
                    view.updateButtonToStopQ();
                }
                BarberQueue queue = queueMap.get(barber.getKey());
                if (queue == null) {
                    //it means there is no customer, that is the reason queue is not created yet
                    queue = BarberQueue.builder().barber(barber).barberKey(barber.getKey()).build();
                    queueMap.put(queue.getBarberKey(), queue);
                }
                EventBus.instance().fireEvent(new QueueTabSelectedEvent(queue));
            }
        });
    }

    public void onBarberSelectionClick() {
        updateBarberStatus(view.getSelectedBarberKey());
        view.hideBarberSelectDialog();
    }

    public void getDownloadUri(final String imagePath, final OnSuccessListener<Uri> onSuccessListener) {
        StorageReference child = storageReference.child(imagePath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    onSuccessListener.onSuccess(task.getResult());
                } else {
                    onSuccessListener.onSuccess(null);
                    LogUtils.error("Problem while getting image url at path: {0}. {1}",
                            imagePath, task.getException().getMessage());
                }
            }

        });
    }

    public void updateBarberStatus(final String selectedKey) {
        Map<String, Object> map = new HashMap<>();
        map.put(Barber.QUEUE_STATUS, BarberStatus.OPEN.name());
        DBUtils.getDbRefBarber(database, userid, selectedKey).updateChildren(map);
    }

    @Override
    public void onBarberStatusChange(BarberStatusChangeEvent event) {
        Barber barber = event.getBarber();
        if (barber.isOpen()) {
            LogUtils.info("queueStatus: {0}", barber.getQueueStatus());
            if (!view.isTabExists(barber.getKey())) {
                view.addBarberQueueTab(barber);
                final DatabaseReference queueRef = DBUtils.getDbRefBarberQueue(database, userid, barber.getKey());
                queueRef.push().setValue(BarberQueue.builder().build()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        EventBus.instance().fireEvent(new RelocationRequestEvent());
                    }
                });
            }
        }
    }

    @Override
    public void onBarberQueuesChange(BarberQueuesChangeEvent event) {
        queueMap.clear();
        final List<BarberQueue> queues = event.getBarberQueues();
        for (BarberQueue queue : queues) {
            queueMap.put(queue.getBarberKey(), queue);
        }
    }
}
