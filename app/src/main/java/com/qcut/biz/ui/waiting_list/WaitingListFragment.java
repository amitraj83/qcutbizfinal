package com.qcut.biz.ui.waiting_list;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.qcut.biz.models.ShopQueueModel;
import com.qcut.biz.ui.adapters.ShopQueueModelAdaptor;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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

        final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
        final AlertDialog startServiceDialog = new AlertDialog.Builder(getContext()).create();
        startServiceDialog.setView(startServiceView);

        startService = root.findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServiceDialog.show();
                startServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                startServiceDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });

        final View skipCustomerView = factory.inflate(R.layout.skip_customer_dialog, null);
        final AlertDialog skipCustomerDialog = new AlertDialog.Builder(getContext()).create();
        skipCustomerDialog.setView(skipCustomerView);

        skipCostumer = root.findViewById(R.id.skip_customer);
        skipCostumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipCustomerDialog.show();
                skipCustomerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                skipCustomerDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
            }
        });


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

        final View serviceDoneView = factory.inflate(R.layout.service_done_dialog, null);
        final AlertDialog serviceDoneDialog = new AlertDialog.Builder(getContext()).create();
        serviceDoneDialog.setView(serviceDoneView);

        return root;
    }


    private void addCustomer(LayoutInflater factory) {

        final View addCustomerView = factory.inflate(R.layout.add_customer_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(addCustomerView);

        final AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

        Button yesButton = (Button) addCustomerView.findViewById(R.id.add_customer_dialog_yes);
        Button noButton = (Button) addCustomerView.findViewById(R.id.add_customer_dialog_no);

        final EditText input = (EditText) addCustomerView.findViewById(R.id.new_customer_name);

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