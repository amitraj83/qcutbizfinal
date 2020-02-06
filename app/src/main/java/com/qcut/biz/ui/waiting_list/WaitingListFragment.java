package com.qcut.biz.ui.waiting_list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerComparator;
import com.qcut.biz.models.ShopQueueModel;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;
import com.qcut.biz.util.TimerService;
import com.qcut.biz.util.ViewUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class WaitingListFragment extends Fragment {

    private WaitingListModel waitingListModel;
    Button startService, finishService;
    FloatingActionButton addCustomer;
    private FirebaseDatabase database = null;
    private SharedPreferences sp;
    private String userid;
    private long timePerCut = 15;
    private WaitingListRecyclerViewAdapter adapter = null;
    private RecyclerView dynamicListView = null;
    private TextView nextCustomerTV;
    private String tag;
    private Context mContext;
    public View rootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public WaitingListFragment(String tag) {
        this.tag = tag;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        waitingListModel =
                ViewModelProviders.of(this).get(WaitingListModel.class);
        final View root = inflater.inflate(R.layout.fragment_waiting_list, container, false);
        this.rootView = root;
//        root.setVisibility(30);
//        root.setBackgroundColor(Color.RED);

        sp = mContext.getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        FirebaseApp.initializeApp(mContext);
        database = FirebaseDatabase.getInstance();

        nextCustomerTV = root.findViewById(R.id.next_customer);

        final LayoutInflater factory = LayoutInflater.from(mContext);

        cardViewStartSkipService(root, factory);

        DatabaseReference barberStatusRef = database.getReference().child("barbershops").child(userid)
                .child("queues").child(TimeUtil.getTodayDDMMYYYY())
                .child(tag.toString())
                .child("status");
        barberStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getValue().toString().equalsIgnoreCase(Status.BREAK.name())) {
                        root.findViewById(R.id.barber_on_break_message).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.next_customer_card).setVisibility(View.INVISIBLE);
                        root.findViewById(R.id.cardView).setBackgroundColor(Color.YELLOW);

                    } else {
                        root.findViewById(R.id.cardView).setBackgroundColor(Color.WHITE);
                        root.findViewById(R.id.next_customer_card).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.barber_on_break_message).setVisibility(View.INVISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        TextView viewById = root.findViewById(R.id.textView);
//        viewById.setText(tag);

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
                                Toast.makeText(mContext, "Cannot add customer. First get online.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(mContext, "Cannot add customer. First get online.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        dynamicListView = root.findViewById(R.id.today_queue);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        dynamicListView.setLayoutManager(mLayoutManager);
        dynamicListView.setItemAnimator(new DefaultItemAnimator());
        final ArrayList<ShopQueueModel> models = new ArrayList<ShopQueueModel>();
        adapter = new WaitingListRecyclerViewAdapter(models, mContext, tag, database, userid);
        dynamicListView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            int dragFrom = -1;
            int dragTo = -1;

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.ViewHolder dragged,
                                  @NonNull final RecyclerView.ViewHolder target) {
                Query barberRef = database.getReference().child("barbershops").child(userid)
                        .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag).
                                orderByChild(Constants.Customer.STATUS).equalTo(Status.PROGRESS.name());
                barberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(mContext, "A customer in chair. Cannot drag or drop others.", Toast.LENGTH_SHORT).show();
                        } else {

                            final int position_dragged = dragged.getAdapterPosition();
                            final String status = ((WaitingListRecyclerViewAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(position_dragged))
                                    .custStatus.getTag().toString();
                            if (status.equalsIgnoreCase(Status.QUEUE.name())) {
                                final int position_target = target.getAdapterPosition();
                                Collections.swap(adapter.getDataSet(), position_dragged, position_target);
                                if (dragFrom == -1) {
                                    dragFrom = position_dragged;
                                }
                                dragTo = position_target;
                                adapter.notifyItemMoved(position_dragged, position_target);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public void clearView(final RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                final DatabaseReference barberRef = database.getReference().child("barbershops").child(userid)
                        .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag);

                barberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Customer> customers = new ArrayList<Customer>();
                        Iterator<DataSnapshot> childIterator = dataSnapshot.getChildren().iterator();
                        while (childIterator.hasNext()) {
                            DataSnapshot aCustomer = childIterator.next();
                            //TODO customers can be added to customers array
                            if (!aCustomer.getKey().equalsIgnoreCase("status")) {
//                                     && aCustomer.child("status").getValue().toString().equalsIgnoreCase(Status.QUEUE.name())) {
                                //if not a status then it is a customer
                                Customer customer = aCustomer.getValue(Customer.class);
                                if (Status.QUEUE.name().equalsIgnoreCase(customer.getStatus())) {
                                    customers.add(Customer.builder().key(aCustomer.getKey())
                                            .timeAdded(customer.getTimeAdded())
                                            .timeToWait(customer.getTimeToWait()).build());
                                }
                            }
                        }
                        Collections.sort(customers, new CustomerComparator());

                        int count = 0;
                        int itemCount = recyclerView.getAdapter().getItemCount();
                        Map<String, Object> timeToUpdate = new HashMap<>();
                        for (int i = 0; i < itemCount; i++) {
                            final String sourceTag = recyclerView.findViewHolderForAdapterPosition(i)
                                    .itemView.getTag().toString();
                            final String status = ((WaitingListRecyclerViewAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i))
                                    .custStatus.getTag().toString();

                            if (status.equalsIgnoreCase(Status.QUEUE.name())) {
                                timeToUpdate.put(sourceTag + "/timeAdded", i);
                                timeToUpdate.put(sourceTag + "/timeToWait", customers.get(count++).getTimeToWait());
                            }

                        }
                        barberRef.updateChildren(timeToUpdate);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }

        });

        helper.attachToRecyclerView(dynamicListView);
        showQueue();
        return root;
    }

    private void cardViewStartSkipService(View root, final LayoutInflater factory) {

        startService = root.findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference barberRef = database.getReference().child("barbershops").child(userid)
                        .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag);
                barberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            if(dataSnapshot.child(Constants.Barber.STATUS).exists()) {
                                if(dataSnapshot.child(Constants.Barber.STATUS).getValue().toString().equalsIgnoreCase(Status.OPEN.name())) {
                                    showStartServiceDialog(factory);
                                } else {
                                    Toast.makeText(mContext, "Cannot start services. May be barber is on break or his queue is stopped.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        finishService = root.findViewById(R.id.skip_customer);
        finishService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showSkipServiceDialog(factory);

            }
        });

    }

    private void showSkipServiceDialog(LayoutInflater factory) {
        final String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
        if (queuedCustomerId != null &&
                queuedCustomerId.trim().equalsIgnoreCase("none") ||
                queuedCustomerId.trim().equalsIgnoreCase("")) {
            Toast toast = Toast.makeText(mContext,
                    "No Customer in the queue", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        } else {
            final View skipCustomerView = factory.inflate(R.layout.skip_customer_dialog, null);
            TextView skipCustName = (TextView) skipCustomerView.findViewById(R.id.skip_customer_name);
            skipCustName.setText(nextCustomerTV.getText());
            final AlertDialog skipCustomerDialog = new AlertDialog.Builder(mContext).create();
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
                if (dataSnapshot.exists()) {
                    final ArrayList<ShopQueueModel> models = new ArrayList<ShopQueueModel>();
                    Iterator<DataSnapshot> snapshotIterator = dataSnapshot.getChildren().iterator();
                    while (snapshotIterator.hasNext()) {
                        DataSnapshot aCustomer = snapshotIterator.next();
                        String key = aCustomer.getKey();
                        if (!key.equalsIgnoreCase("online")) {
                            //if key is not online then it will be a barber queue
                            //TODO verify: this is barber queue customer will be not at this level, it will be furthur nested
                            if (aCustomer.child("name").getValue() != null) {
                                String name = aCustomer.child("name").getValue().toString();
                                long timeToWait = aCustomer.child("timeToWait").exists() ?
                                        Long.valueOf(aCustomer.child("timeToWait").getValue().toString()) : 180L;
                                long timeAdded = Long.valueOf(aCustomer.child("timeAdded").getValue().toString());
                                String status = aCustomer.child("status").exists() ?
                                        aCustomer.child("status").getValue().toString() : Status.QUEUE.name();
                                if (status.equalsIgnoreCase(Status.QUEUE.name())) {
                                }
                                models.add(new ShopQueueModel(key, name, timeAdded, timeToWait,
                                        TimeUtil.getDisplayWaitingTime(timeToWait), status,
                                        Boolean.valueOf(aCustomer.child(Constants.Customer.IS_ANY_BARBER).exists() ?
                                                aCustomer.child(Constants.Customer.IS_ANY_BARBER).getValue().toString() : "true"))
                                );
                            }
                        }
                    }
                    Collections.sort(models, new DataComparator());
                    ShopQueueModel prev = null;
                    ShopQueueModel next = null;
                    for (ShopQueueModel model : models) {
                        if (model.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
                            if (model.getId().equalsIgnoreCase(nextCustomerId)) {
                                prev = model;
                                continue;
                            }
                            if (prev != null) {
                                next = model;
                                break;
                            }

                        }
                    }
                    if (prev != null && next != null) {
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
        if (queuedCustomerId != null &&
                queuedCustomerId.trim().equalsIgnoreCase("none") ||
                queuedCustomerId.trim().equalsIgnoreCase("")) {
            Toast toast = Toast.makeText(mContext,
                    "No Customer in the queue", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        } else {
            final View startServiceView = factory.inflate(R.layout.start_service_dialog, null);
            TextView startServiceCustName = (TextView) startServiceView.findViewById(R.id.start_service_cust_name);
            startServiceCustName.setText(nextCustomerTV.getText());
            final AlertDialog startServiceDialog = new AlertDialog.Builder(mContext).create();
            startServiceDialog.setView(startServiceView);

            startServiceDialog.show();
            startServiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 4, getResources().getDisplayMetrics());
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());

            startServiceDialog.getWindow().setLayout(width, height);

            final Button yesButton = (Button) startServiceDialog.findViewById(R.id.yes_start_service);
            final Button noButton = (Button) startServiceDialog.findViewById(R.id.no_start_service);

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String queuedCustomerId = String.valueOf(nextCustomerTV.getTag());
                    setCustomerInProgress(queuedCustomerId);
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

    private void setCustomerInProgress(final String queuedCustomerId) {

        final DatabaseReference queryRef = database.getReference().child("barbershops").child(userid)
                .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                boolean isSomeoneInProgress = false;
                String customerId = "";
                while (iterator.hasNext()) {
                    DataSnapshot customer = iterator.next();

                    if (!customer.getKey().equalsIgnoreCase("status")) {
                        //its a customer
                        Customer c = customer.getValue(Customer.class);

                        if (customer.getKey().equalsIgnoreCase(queuedCustomerId)) {
                            customerId = c.getCustomerId();
                        }

                        if (Status.PROGRESS.name().equalsIgnoreCase(c.getStatus())) {
                            isSomeoneInProgress = true;
                        }
                    }
                }
                if (!isSomeoneInProgress) {

                    final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                            .child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag).child(queuedCustomerId);


                    Map<String, Object> map = new HashMap<>();
                    map.put(Constants.Customer.STATUS, Status.PROGRESS);
                    map.put(Constants.Customer.TIME_TO_WAIT, 0);
                    map.put(Constants.Customer.TIME_ADDED, -1);
                    map.put(Constants.Customer.TIME_SERVICE_STARTED, new Date().getTime());
                    map.put(Constants.Customer.PLACE_IN_QUEUE, -1);
                    Task<Void> voidTask = queue.updateChildren(map);
                    final String customerIdForAnyBarber = customerId;
                    voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DatabaseReference dateRef = database.getReference().child("barbershops").child(userid)
                                    .child("queues").child(TimeUtil.getTodayDDMMYYYY());
                            dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterator<DataSnapshot> queueIt = dataSnapshot.getChildren().iterator();
                                    while (queueIt.hasNext()) {
                                        DataSnapshot queueSnapshot = queueIt.next();
                                        if (!queueSnapshot.getKey().equalsIgnoreCase(tag) && !queueSnapshot.getKey().equalsIgnoreCase("online")) {
                                            Iterator<DataSnapshot> customersIt = queueSnapshot.getChildren().iterator();
                                            while (customersIt.hasNext()) {
                                                DataSnapshot customerDataSnapshot = customersIt.next();
                                                if (!customerDataSnapshot.getKey().equalsIgnoreCase("status")) {
                                                    //it is a customer
                                                    if (customerDataSnapshot.getValue(Customer.class).getCustomerId()
                                                            .equalsIgnoreCase(customerIdForAnyBarber)) {
                                                        queueSnapshot.getRef().child(customerDataSnapshot.getKey()).removeValue();
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(mContext, "Cannot start services. A customer is already in progress.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addCustomer(LayoutInflater factory) {

        final View addCustomerView = factory.inflate(R.layout.add_customer_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(addCustomerView);

        final AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayHeight(getActivity().getWindowManager()) / 3, getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ViewUtils.getDisplayWidth(getActivity().getWindowManager()) / 2, getResources().getDisplayMetrics());

        dialog.getWindow().setLayout(width, height);

        final Button yesButton = (Button) addCustomerView.findViewById(R.id.add_customer_dialog_yes);
        final Button noButton = (Button) addCustomerView.findViewById(R.id.add_customer_dialog_no);

        final EditText input = (EditText) addCustomerView.findViewById(R.id.new_customer_name);

        final Spinner ddSpinner = addCustomerView.findViewById(R.id.spinner_barber_selection);


        final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid);
        barbersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                final List<String> queueBarberIdList = new ArrayList<String>();
                if (dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY()).exists()) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY()).getChildren().iterator();
                    List<Barber> barberList = new ArrayList<>();
                    barberList.add(Barber.builder().id(Constants.ANY).name(Constants.ANY).imagePath("").build());

                    while (iterator.hasNext()) {
                        DataSnapshot next = iterator.next();
                        if (next != null && !next.getKey().equalsIgnoreCase("online")) {
                            String barberKey = next.getKey();
                            Barber barber = dataSnapshot.child("barbers").child(barberKey).getValue(Barber.class);
                            String barberStatus = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY())
                                    .child(barberKey).child("status").getValue().toString();
                            if (StringUtils.isNotBlank(barberKey) && barber != null && !barberStatus.equalsIgnoreCase(Status.STOP.name())) {
                                queueBarberIdList.add(next.getKey());
                                barberList.add(barber);
                            }
                        }
                    }

                    BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(mContext, barberList);
                    ddSpinner.setAdapter(customAdapter);
                }
                ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        final String selectedKey = ddSpinner.getAdapter().getDropDownView(i, null, null).getTag().toString();

                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                yesButton.setEnabled(false);
                                if (input != null && !input.getText().toString().trim().equalsIgnoreCase("")) {
                                    String customerId = UUID.randomUUID().toString();
                                    if (selectedKey.equalsIgnoreCase(Constants.ANY)) {
                                        for (String barberKey : queueBarberIdList) {
                                            pushCustomerToDB(input, dataSnapshot, barberKey, dialog, customerId, true);
                                        }
                                    } else {
                                        pushCustomerToDB(input, dataSnapshot, selectedKey, dialog, customerId, false);
                                    }
                                    yesButton.playSoundEffect(0);
                                } else {
                                    Toast.makeText(mContext, "Cannot add customer. No name provided", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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


        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void pushCustomerToDB(EditText input, @NonNull DataSnapshot dataSnapshot, String selectedKey,
                                  final AlertDialog dialog, String customerId, boolean isAny) {
        final String name = input.getText().toString();
        dialog.dismiss();
        Task<Void> voidTask = DBUtils.pushCustomerToDB(mContext, dataSnapshot, selectedKey, name, customerId, isAny);
        if(voidTask != null) {
            voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    TimerService.updateWaitingTimes(database, userid);
                    Toast.makeText(mContext, name + " added to queue", Toast.LENGTH_SHORT).show();
                    MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.door_bell);
                    mediaPlayer.start();

                }
            });
        }
    }


    private void showQueue() {

        DatabaseReference dbRef = database.getReference().child("barbershops")
                .child(userid).child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(tag);
        if (dbRef != null) {
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final ArrayList<ShopQueueModel> models = new ArrayList<ShopQueueModel>();
                    if (dataSnapshot.exists() && dataSnapshot.getKey().equalsIgnoreCase(tag)) {
                        Iterator<DataSnapshot> snapshotIterator = dataSnapshot.getChildren().iterator();
                        boolean isSomeOneInQueue = false;
                        while (snapshotIterator.hasNext()) {
                            DataSnapshot aCustomer = snapshotIterator.next();
                            String key = aCustomer.getKey();
                            if (!key.equalsIgnoreCase("status")) {
                                Customer customer = aCustomer.getValue(Customer.class);
                                boolean isCustomerNameNotNull = StringUtils.isNotBlank(customer.getName());
                                boolean isCustomerNotDone = !Status.DONE.name().equalsIgnoreCase(customer.getStatus());
                                boolean isCustomerNotRemoved = !Status.REMOVED.name().equalsIgnoreCase(customer.getStatus());

                                if (isCustomerNameNotNull && (isCustomerNotDone && isCustomerNotRemoved)) {
                                    //TODO DEFAULT VALUE
//                                    long timeToWait = aCustomer.child("timeToWait").exists() ?
//                                            Long.valueOf(aCustomer.child("timeToWait").getValue().toString()) : 180L;
                                    //TODO DEFAULT VALUE
//                                    long timeAdded = Long.valueOf(aCustomer.child("timeAdded").exists() ?
//                                            aCustomer.child("timeAdded").getValue().toString() : "3");
                                    //TODO DEFAULT VALUE
//                                    String status = aCustomer.child("status").exists() ?
//                                            aCustomer.child("status").getValue().toString() : Status.QUEUE.name();
                                    if (Status.QUEUE.name().equalsIgnoreCase(customer.getStatus())) {
                                        isSomeOneInQueue = true;
                                    }
                                    models.add(new ShopQueueModel(key, customer.getName(), customer.getTimeAdded(), customer.getTimeToWait(),
                                            TimeUtil.getDisplayWaitingTime(customer.getTimeToWait()), customer.getStatus(),
                                            Boolean.valueOf(aCustomer.child(Constants.Customer.IS_ANY_BARBER).getValue().toString())));
                                }
                            }
                        }
                        if (isSomeOneInQueue && models.size() > 0) {
                            Collections.sort(models, new DataComparator());
                            for (ShopQueueModel model : models) {
                                if (model.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
                                    nextCustomerTV.setText(model.getName());
                                    nextCustomerTV.setTag(model.getId());
                                    break;
                                }
                            }
                        } else {
                            nextCustomerTV.setText("No customer.");
                            nextCustomerTV.setTag("NONE");
                        }
                        adapter.setDataSet(models);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void queueChangeListener() {
        //showQueue();
//        DatabaseReference dbRef = database.getReference().child("barbershops")
//                .child(userid).child("queues").child(TimeUtil.getTodayDDMMYYYY());
//        dbRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                showQueue();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private class DataComparator implements Comparator<ShopQueueModel> {

        @Override
        public int compare(ShopQueueModel o1, ShopQueueModel o2) {
            if (o1.getTimeAdded() > o2.getTimeAdded()) {
                return 1;
            } else if (o1.getTimeAdded() == o2.getTimeAdded()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

}