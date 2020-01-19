package com.qcut.biz.ui.waiting_list;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.ShopQueueModel;
import com.qcut.biz.ui.adapters.ShopQueueModelAdaptor;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListFragment extends Fragment {

    private WaitingListModel waitingListModel;
    Button startService, skipCostumer;
    FloatingActionButton addCustomer;
    private FirebaseDatabase database = null;
    private SharedPreferences sp;
    private String userid;
    private int timePerCut = 15;
    private ShopQueueModelAdaptor adapter = null;
    private ListView dynamicListView = null;
    private TextView nextCustomerTV;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        waitingListModel =
                ViewModelProviders.of(this).get(WaitingListModel.class);
        View root = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        FirebaseApp.initializeApp(getContext());
        database = FirebaseDatabase.getInstance();

        nextCustomerTV = root.findViewById(R.id.next_customer);

        final LayoutInflater factory = LayoutInflater.from(getContext());

        cardViewStartSkipService(root, factory);



        addCustomer = root.findViewById(R.id.add_customer_fab);
        addCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DatabaseReference onlineRef = database.getReference().child("barbershops").child(userid)
                        .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child("online");
                onlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String onlineValue = dataSnapshot.getValue().toString();
                            if (onlineValue.equalsIgnoreCase("true")) {
                                addCustomer(factory);
                            } else {
                                Toast.makeText(getContext(), "Cannot add customer. First get online.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getContext(), "Cannot add customer. First get online.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        dynamicListView = root.findViewById(R.id.today_queue);
        showQueue();
        queueChangeListener();




        dynamicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeCustomerStatus(factory, position);
            }
        });


        return root;
    }

    private void cardViewStartSkipService(View root, final LayoutInflater factory) {

        startService = root.findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartServiceDialog(factory);
            }
        });


        skipCostumer = root.findViewById(R.id.skip_customer);
        skipCostumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSkipServiceDialog(factory);

            }
        });

    }

    private void showSkipServiceDialog(LayoutInflater factory) {
        final String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
        if(queuedCustomerId != null &&
                queuedCustomerId.trim().equalsIgnoreCase("none") ||
                queuedCustomerId.trim().equalsIgnoreCase("")) {
            Toast toast= Toast.makeText(getContext(),
                    "No Customer in the queue", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        } else {
            final View skipCustomerView = factory.inflate(R.layout.skip_customer_dialog, null);
            TextView skipCustName = (TextView) skipCustomerView.findViewById(R.id.skip_customer_name);
            skipCustName.setText(nextCustomerTV.getText());
            final AlertDialog skipCustomerDialog = new AlertDialog.Builder(getContext()).create();
            skipCustomerDialog.setView(skipCustomerView);

            skipCustomerDialog.show();
            skipCustomerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            skipCustomerDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

            Button yesButton = (Button) skipCustomerDialog.findViewById(R.id.yes_skip_service);
            Button noButton = (Button) skipCustomerDialog.findViewById(R.id.no_skip_service);

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    yesSkipCustomer(queuedCustomerId);
                    skipCustomerDialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipCustomerDialog.dismiss();
                }
            });

        }
    }

    private void yesSkipCustomer(final String nextCustomerId) {
        final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                .child("queues").child(TimeUtil.getTodayDDMMYYYY());
        queue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    final ArrayList<ShopQueueModel> models = new ArrayList<ShopQueueModel>();
                    Iterator<DataSnapshot> snapshotIterator = dataSnapshot.getChildren().iterator();
                    while (snapshotIterator.hasNext()) {
                        DataSnapshot aCustomer = snapshotIterator.next();
                        String key = aCustomer.getKey();
                        if(!key.equalsIgnoreCase("online")) {
                            if (aCustomer.child("name").getValue() != null) {
                                String name = aCustomer.child("name").getValue().toString();
                                long timeToWait = aCustomer.child("timeToWait").getValue() != null ?
                                        Long.valueOf(aCustomer.child("timeToWait").getValue().toString()) : 180L;
                                String status = aCustomer.child("status").getValue() != null ?
                                        aCustomer.child("status").getValue().toString() : Status.QUEUE.name();
                                if(status.equalsIgnoreCase(Status.QUEUE.name())) {
                                }
                                models.add(new ShopQueueModel(key, name, timeToWait, TimeUtil.getDisplayWaitingTime(timeToWait), status));
                            }
                        }
                    }
                    Collections.sort(models, new DataComparator());
                    ShopQueueModel prev = null;
                    ShopQueueModel next = null;
                    for (ShopQueueModel model : models) {
                        if(model.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
                            if(model.getId().equalsIgnoreCase(nextCustomerId)) {
                                prev = model;
                                continue;
                            }
                            if(prev != null) {
                                next = model;
                                break;
                            }

                        }
                    }
                    if(prev != null && next != null) {
                        Map<String, Object> prevData = new HashMap<>();
                        prevData.put("timeToWait", next.getTimeToWait());
                        Map<String, Object> nextData = new HashMap<>();
                        nextData.put("timeToWait", prev.getTimeToWait());

                        final DatabaseReference prevRef = database.getReference().child("barbershops").child(userid)
                                .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(prev.getId());
                        prevRef.updateChildren(prevData);

                        final DatabaseReference nextRef = database.getReference().child("barbershops").child(userid)
                                .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(next.getId());
                        nextRef.updateChildren(nextData);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showStartServiceDialog(LayoutInflater factory) {
        String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
        if(queuedCustomerId != null &&
                queuedCustomerId.trim().equalsIgnoreCase("none") ||
                queuedCustomerId.trim().equalsIgnoreCase("")) {
            Toast toast= Toast.makeText(getContext(),
                    "No Customer in the queue", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        } else {
            final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
            TextView startServiceCustName = (TextView) startServiceView.findViewById(R.id.start_service_cust_name);
            startServiceCustName.setText(nextCustomerTV.getText());
            final AlertDialog startServiceDialog = new AlertDialog.Builder(getContext()).create();
            startServiceDialog.setView(startServiceView);

            startServiceDialog.show();
            startServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            startServiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

            Button yesButton = (Button) startServiceDialog.findViewById(R.id.yes_start_service);
            Button noButton = (Button) startServiceDialog.findViewById(R.id.no_start_service);

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(queuedCustomerId);

                    Map<String, Object> map = new HashMap<>();
                    map.put("status", Status.PROGRESS);
                    map.put("timeToWait", 0);
                    map.put("placeInQueue", 0);
                    queue.updateChildren(map);

                    if(adapter != null) {
                        showQueue();
                    }
                    startServiceDialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startServiceDialog.dismiss();
                }
            });

        }
    }

    private void changeCustomerStatus(LayoutInflater factory, int position) {
        final ShopQueueModel queueItem = (ShopQueueModel) dynamicListView.getItemAtPosition(position);
        if(queueItem.getStatus().equalsIgnoreCase(Status.PROGRESS.name())) {
            final View serviceDoneView = factory.inflate(R.layout.service_done_dialog, null);
            TextView custNameTV = (TextView) serviceDoneView.findViewById(R.id.service_done_customer_name);
            custNameTV.setText(queueItem.getName());
            final AlertDialog serviceDoneDialog = new AlertDialog.Builder(getContext()).create();
            serviceDoneDialog.setView(serviceDoneView);


            serviceDoneDialog.show();
            serviceDoneDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            serviceDoneDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

            addServiceDoneButtonsClickListener(serviceDoneDialog, queueItem);

        } else if (queueItem.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
            final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
            TextView custNameTV = (TextView) startServiceView.findViewById(R.id.start_service_cust_name);
            custNameTV.setText(queueItem.getName());
            final AlertDialog serviceStartDialog = new AlertDialog.Builder(getContext()).create();
            serviceStartDialog.setView(startServiceView);

            serviceStartDialog.show();
            serviceStartDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            serviceStartDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            addServiceStartButtonsClickListener(serviceStartDialog, queueItem);
        }

    }

    private void addServiceStartButtonsClickListener(final AlertDialog serviceStartDialog, final ShopQueueModel queueItem) {

        Button yesButton = (Button) serviceStartDialog.findViewById(R.id.yes_start_service);
        Button noButton = (Button) serviceStartDialog.findViewById(R.id.no_start_service);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String queuedCustomerId = String.valueOf(queueItem.getId());
                if (queuedCustomerId != null) {
                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(queuedCustomerId);

                    Map<String, Object> map = new HashMap<>();
                    map.put("status", Status.PROGRESS);
                    map.put("timeToWait", 0);
                    map.put("placeInQueue", 0);
                    queue.updateChildren(map);
                }
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

    private void addServiceDoneButtonsClickListener(final AlertDialog serviceDoneDialog, final ShopQueueModel queueItem) {

        Button yesButton = (Button) serviceDoneDialog.findViewById(R.id.yes_done_service);
        Button noButton = (Button) serviceDoneDialog.findViewById(R.id.no_done_service);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String queuedCustomerId = String.valueOf(queueItem.getId());
                if (queuedCustomerId != null) {
                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(queuedCustomerId);

                    Map<String, Object> map = new HashMap<>();
                    map.put("status", Status.DONE);
                    map.put("timeToWait", 0);
                    map.put("placeInQueue", 0);
                    queue.updateChildren(map);
                }
                serviceDoneDialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceDoneDialog.dismiss();
            }
        });
    }


    private void addCustomer(LayoutInflater factory) {

        final View addCustomerView = factory.inflate(R.layout.add_customer_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(addCustomerView);

        final AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 950);

        Button yesButton = (Button) addCustomerView.findViewById(R.id.add_customer_dialog_yes);
        Button noButton = (Button) addCustomerView.findViewById(R.id.add_customer_dialog_no);

        final EditText input = (EditText) addCustomerView.findViewById(R.id.new_customer_name);

        final Spinner ddSpinner = addCustomerView.findViewById(R.id.spinner_barber_selection);

        final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid)
                .child("barbers");
        barbersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                List<Barber> barberList = new ArrayList<>();
                while (iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
                    DataSnapshot name = next.child("name");
                    DataSnapshot imagePath = next.child("imagePath");

                    barberList.add(new Barber(name.getValue().toString(), imagePath.getValue().toString()));
                }
                BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(getContext(), barberList);
                ddSpinner.setAdapter(customAdapter);

                ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input != null && !input.getText().toString().trim().equalsIgnoreCase("")) {
                    final String name = input.getText().toString();
                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY());
                    queue.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int count = 0;
                            if (dataSnapshot.exists()) {
                                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                while (iterator.hasNext()) {
                                    DataSnapshot next = iterator.next();
                                    if (next != null && !next.getKey().equalsIgnoreCase("online")) {
                                        DataSnapshot statusChild = next.child("status");
                                        String status = String.valueOf(statusChild.getValue());
                                        if (status.equalsIgnoreCase(Status.QUEUE.name())) {
                                            count++;
                                        }
                                    }
                                }
                            }
                            String key = queue.push().getKey();

                            Map<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("placeInQueue", ++count);
                            map.put("skipcount", 0);
                            map.put("timeToWait", timePerCut * count);
                            map.put("status", Status.QUEUE);
                            Task<Void> voidTask = queue.child(key).setValue(map);
                            voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), name+" added to queue", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Cannot add customer. No name provided", Toast.LENGTH_SHORT).show();

                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }



    private void showQueue() {
        final ArrayList<ShopQueueModel> models = new ArrayList<ShopQueueModel>();
        DatabaseReference dbRef = database.getReference().child("barbershops")
                .child(userid).child("queues").child(TimeUtil.getTodayDDMMYYYY());
        if(dbRef != null) {
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Iterator<DataSnapshot> snapshotIterator = dataSnapshot.getChildren().iterator();
                        boolean isSomeOneInQueue = false;
                        while (snapshotIterator.hasNext()) {
                            DataSnapshot aCustomer = snapshotIterator.next();
                            String key = aCustomer.getKey();
                            if(!key.equalsIgnoreCase("online")) {
                                boolean isCustomerNameNotNull = aCustomer.child("name").getValue() != null;
                                boolean isCustomerNotDone = !aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.DONE.name());
                                boolean isCustomerNotRemoved = !aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.REMOVED.name());

                                if (isCustomerNameNotNull && ( isCustomerNotDone && isCustomerNotRemoved) ) {
                                    String name = aCustomer.child("name").getValue().toString();
                                    long timeToWait = aCustomer.child("timeToWait").getValue() != null ?
                                            Long.valueOf(aCustomer.child("timeToWait").getValue().toString()) : 180L;
                                    String status = aCustomer.child("status").getValue() != null ?
                                            aCustomer.child("status").getValue().toString() : Status.QUEUE.name();
                                    if(status.equalsIgnoreCase(Status.QUEUE.name())) {
                                        isSomeOneInQueue = true;
                                    }
                                    models.add(new ShopQueueModel(key, name, timeToWait, TimeUtil.getDisplayWaitingTime(timeToWait), status));
                                }
                            }
                        }
                        if(isSomeOneInQueue && models.size() > 0) {
                            Collections.sort(models, new DataComparator());
                            for (ShopQueueModel model : models) {
                                if(model.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
                                    nextCustomerTV.setText(model.getName());
                                    nextCustomerTV.setTag(model.getId());
                                    break;
                                }
                            }
                        } else {
                            nextCustomerTV.setText("No next customer.");
                            nextCustomerTV.setTag("NONE");
                        }
                        adapter= new ShopQueueModelAdaptor(models, getContext());
                        dynamicListView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void queueChangeListener(){
        DatabaseReference dbRef = database.getReference().child("barbershops")
                .child(userid).child("queues").child(TimeUtil.getTodayDDMMYYYY());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showQueue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class DataComparator implements Comparator<ShopQueueModel> {

        @Override
        public int compare(ShopQueueModel o1, ShopQueueModel o2) {
            if (o1.getTimeToWait() > o2.getTimeToWait()) {
                return 1;
            } else if (o1.getTimeToWait() == o2.getTimeToWait()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

}