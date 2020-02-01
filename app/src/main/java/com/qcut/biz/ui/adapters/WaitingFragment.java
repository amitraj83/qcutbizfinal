package com.qcut.biz.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.ui.waiting_list.BarberSelectionArrayAdapter;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class WaitingFragment extends Fragment {

    private FirebaseDatabase database = null;
    private SharedPreferences sp;
    private String userid;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Context mContext;
    Button takeBreakButton = null;
    Button stopQButton = null;
    Button closeQButton = null;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.waiting_queue, container, false);
        final TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        sp = mContext.getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);
        FirebaseApp.initializeApp(mContext);
        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final Button addBarber = root.findViewById(R.id.addTab);
        takeBreakButton = root.findViewById(R.id.tab_index_test);
        stopQButton = root.findViewById(R.id.stop_queue);
//        stopQButton = root.findViewById(R.id.close_queue);


        stopQButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference barberStatusRef = database.getReference().child("barbershops").child(userid);
                barberStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String dialogTitle, dialogText, confirmText;
                            final Status newStatus;
                            String status = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY())
                                    .child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                                    .child("status").getValue().toString();
                            if (status.equalsIgnoreCase(Status.STOP.name())) {
                                stopQButton.setText("STOPPED");
                                stopQButton.setTextColor(Color.RED);
                                dialogTitle = "Resume Queue";
                                dialogText = "Dear Baber ";
                                confirmText = "Want to resume your Queue? After this, customers will be added to your queue";
                                newStatus = Status.OPEN;
                            } else {
                                stopQButton.setText("Stop Q");
                                stopQButton.setTextColor(getResources().getColor(R.color.backgroundItems));
                                dialogTitle = "Stop Queue";
                                dialogText = "Dear Baber ";
                                confirmText = "Want to stop your queue? After this, no customer will be added to your queue";
                                newStatus = Status.STOP;
                            }

                            String photoPath = dataSnapshot.child("barbers").child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                                    .child("imagePath").getValue().toString();
                            String barberName = dataSnapshot.child("barbers").child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                                    .child("name").getValue().toString();
                            queueStatusChangeDialog(dialogTitle, dialogText, confirmText, newStatus, tabLayout, barberName, photoPath);
                            //tabLayout.removeTabAt(tabLayout.getSelectedTabPosition());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        takeBreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference barberStatusRef = database.getReference().child("barbershops").child(userid);
                barberStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String dialogTitle, dialogText, confirmText;
                            final Status newStatus;
                            String status = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY())
                                    .child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                                    .child("status").getValue().toString();
                            if (status.equalsIgnoreCase(Status.BREAK.name())) {
                                takeBreakButton.setText("On Break");
                                takeBreakButton.setTextColor(Color.RED);
                                dialogTitle = "Resume Work";
                                dialogText = "Dear Baber ";
                                confirmText = "Do you want to resume your work?";
                                newStatus = Status.OPEN;
                            } else {
                                takeBreakButton.setText("Break");
                                takeBreakButton.setTextColor(getResources().getColor(R.color.backgroundItems));
                                dialogTitle = "Take A Break";
                                dialogText = "Dear Baber ";
                                confirmText = "Do you want to take a break from work?";
                                newStatus = Status.BREAK;
                            }
                            String photoPath = dataSnapshot.child("barbers").child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                                    .child("imagePath").getValue().toString();
                            String barberName = dataSnapshot.child("barbers").child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                                    .child("name").getValue().toString();

                            queueStatusChangeDialog(dialogTitle, dialogText, confirmText, newStatus, tabLayout, barberName, photoPath);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

        final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid);


        ////////////////////
        barbersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.child("barbers").getChildren().iterator();
                final List<Barber> barberList = new ArrayList<>();
                while (iterator.hasNext()) {
                    final DataSnapshot next = iterator.next();
                    final DataSnapshot name = next.child("name");
                    final DataSnapshot imagePath = next.child("imagePath");
                    boolean bq = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(next.getKey()).exists();
                    if (!bq) {
                        barberList.add(new Barber(next.getKey().toString(), name.getValue().toString(), imagePath.getValue().toString()));
                    } else {
                        boolean tabExists = false;
                        for (int i = 0; i < tabLayout.getTabCount(); i++) {
                            if (tabLayout.getTabAt(i).getTag().toString().equalsIgnoreCase(next.getKey().toString())) {
                                tabExists = true;
                            }
                        }
                        if (!tabExists) {
                            addTab(name.getValue().toString(), tabLayout, root, next.getKey(), imagePath.getValue().toString(), dataSnapshot);
                        }
                    }
                }


                addBarber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final List<Barber> barberList = new ArrayList<>();
                        final DatabaseReference barbersRef = database.getReference().child("barbershops").child(userid);
                        barbersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> iterator = dataSnapshot.child("barbers").getChildren().iterator();
                                final List<Barber> barberList = new ArrayList<>();
                                while (iterator.hasNext()) {
                                    final DataSnapshot next = iterator.next();
                                    final DataSnapshot name = next.child("name");
                                    final DataSnapshot imagePath = next.child("imagePath");
                                    boolean bq = dataSnapshot.child("queues").child(TimeUtil.getTodayDDMMYYYY()).child(next.getKey()).exists();
                                    if (!bq) {
                                        barberList.add(new Barber(next.getKey().toString(), name.getValue().toString(), imagePath.getValue().toString()));
                                    }
                                }


                                if (barberList.size() > 0) {
                                    final LayoutInflater factory = LayoutInflater.from(mContext);
                                    final View selectBarberView = factory.inflate(R.layout.select_barber, null);
                                    final AlertDialog selectBarberDialog = new AlertDialog.Builder(mContext).create();
                                    selectBarberDialog.setView(selectBarberView);


                                    selectBarberDialog.show();
                                    selectBarberDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    selectBarberDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);

                                    final Button yesButton = (Button) selectBarberDialog.findViewById(R.id.yes_add_barber_queue);
                                    final Button noButton = (Button) selectBarberDialog.findViewById(R.id.no_add_barber_queue);

                                    //----------------------------
                                    final Spinner ddSpinner = selectBarberDialog.findViewById(R.id.spinner_select_barber_to_start_queue);

                                    BarberSelectionArrayAdapter customAdapter = new BarberSelectionArrayAdapter(mContext, barberList);
                                    ddSpinner.setAdapter(customAdapter);

                                    ddSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, long l) {

                                            final String selectedKey = ddSpinner.getAdapter().getDropDownView(i, null, null).getTag().toString();

                                            yesButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    String name = dataSnapshot.child("barbers").child(selectedKey).child("name").getValue().toString();
                                                    String imagePath = dataSnapshot.child("barbers").child(selectedKey).child("imagePath").getValue().toString();

                                                    addTab(name, tabLayout, root, selectedKey, imagePath, dataSnapshot);

                                                    selectBarberDialog.dismiss();

                                                }
                                            });
                                            noButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    selectBarberDialog.dismiss();
                                                }
                                            });

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });

                                } else {
                                    Toast.makeText(mContext, "No barber to add", Toast.LENGTH_SHORT).show();
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //----------------------------------
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


////////////////////////////////


        return root;
    }

    private void queueStatusChangeDialog(String dialogTitle, String dialogText, String confirmText,
                                         final Status newStatus, final TabLayout tabLayout, String barberName, String photoPath) {
        final LayoutInflater factory = LayoutInflater.from(mContext);
        final View takeBreakView = factory.inflate(R.layout.take_break_dialog, null);
        final AlertDialog takeBreakDialog = new AlertDialog.Builder(mContext).create();
        takeBreakDialog.setView(takeBreakView);


        takeBreakDialog.show();
        takeBreakDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        takeBreakDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 900);

        ((TextView) takeBreakDialog.findViewById(R.id.take_break_dialog_title)).setText(dialogTitle);
        ((TextView) takeBreakDialog.findViewById(R.id.take_break_text)).setText("Dear " + barberName);
        ((TextView) takeBreakDialog.findViewById(R.id.take_break_confirm_text)).setText(confirmText);

        final ImageView photoView = takeBreakDialog.findViewById(R.id.take_break_photo);

        StorageReference child = storageReference.child(photoPath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Glide.with(mContext)
                            .load(task.getResult())
                            .into(photoView);
                } else {
                    Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button yesButton = (Button) takeBreakDialog.findViewById(R.id.take_break_dialog_yes);
        final Button noButton = (Button) takeBreakDialog.findViewById(R.id.take_break_dialog_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Task<Void> voidTask = database.getReference().child("barbershops").child(userid)
                        .child("queues").child(TimeUtil.getTodayDDMMYYYY())
                        .child(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())
                        .child("status").setValue(newStatus);
                voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        takeBreakDialog.dismiss();
                    }
                });
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeBreakDialog.dismiss();
            }
        });
    }

    private void addTab(String name, TabLayout tabLayout, View root, final String selectedKey, String imagePath, @NonNull DataSnapshot dataSnapshot) {
        final TabLayout.Tab tab = tabLayout.newTab();
        View customView = LayoutInflater.from(mContext).inflate(R.layout.tab_customer_layout, null);
        tab.setCustomView(customView);
        tab.setTag(selectedKey);
        tabLayout.addTab(tab.setText("Loading...").setIcon(R.drawable.photo_barber));

        final ViewPager viewPager = (ViewPager) root.findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount(), tabLayout);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                DatabaseReference barberStatusRef = database.getReference().child("barbershops").child(userid)
                        .child("queues").child(TimeUtil.getTodayDDMMYYYY())
                        .child(tab.getTag().toString())
                        .child("status");
                barberStatusRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.getValue().toString().equalsIgnoreCase(Status.BREAK.name())) {
                                takeBreakButton.setText("On Break");
                                takeBreakButton.setTextColor(Color.RED);
                            } else {
                                takeBreakButton.setText("Break");
                                takeBreakButton.setTextColor(getResources().getColor(R.color.backgroundItems));
                            }

                            if (dataSnapshot.getValue().toString().equalsIgnoreCase(Status.STOP.name())) {
                                stopQButton.setText("STOPPED");
                                stopQButton.setTextColor(Color.RED);
                            } else {
                                stopQButton.setText("Stop Q");
                                stopQButton.setTextColor(getResources().getColor(R.color.backgroundItems));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

//        tab.setText(name);
        ((TextView)customView.findViewById(R.id.tab_name)).setText(name);
        createBarberQueue(selectedKey);

        StorageReference child = storageReference.child(imagePath);
        child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    RequestOptions myOptions = new RequestOptions()
                            .override(100, 100);

                    Glide.with(mContext)
                            .asBitmap()
                            .apply(myOptions)
                            .load(task.getResult())
                            .into((ImageView) tab.getCustomView().findViewById(R.id.tab_image));
                } else {
                    Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createBarberQueue(final String selectedKey) {
        final DatabaseReference queue = database.getReference().child("barbershops").child(userid)
                .child("queues").child(TimeUtil.getTodayDDMMYYYY());
        queue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(selectedKey).exists()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", Status.OPEN);
                    Task<Void> voidTask = queue.child(selectedKey).setValue(map);
                    voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DataSnapshot aBarber = null;
                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                            while (iterator.hasNext()) {
                                aBarber = iterator.next();
                                if(!aBarber.getKey().equalsIgnoreCase(selectedKey)) {
                                    break;
                                }
                            }

                            if(aBarber != null) {
                                Iterator<DataSnapshot> custIterator = aBarber.getChildren().iterator();
                                while (custIterator.hasNext()) {
                                    DataSnapshot aCustomer = custIterator.next();
                                    Object isAnyBarberValue = aCustomer.child(Constants.Customer.IS_ANY_BARBER).getValue();
                                    if (isAnyBarberValue != null) {
                                        Boolean isAnyBarber = Boolean.valueOf(isAnyBarberValue.toString());
                                        if(isAnyBarber) {


                                            Map<String, Object> map = new HashMap<>();
                                            if (aCustomer.child(Constants.Customer.NAME).getValue() != null) {
                                                map.put(Constants.Customer.NAME,
                                                        aCustomer.child(Constants.Customer.NAME).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.PLACE_IN_QUEUE).getValue() != null) {
                                                map.put(Constants.Customer.PLACE_IN_QUEUE, 0);
                                            }
                                            if (aCustomer.child(Constants.Customer.SKIP_COUNT).getValue() != null) {
                                                map.put(Constants.Customer.SKIP_COUNT,
                                                        aCustomer.child(Constants.Customer.SKIP_COUNT).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.TIME_TO_WAIT).getValue() != null) {
                                                map.put(Constants.Customer.TIME_TO_WAIT,
                                                        aCustomer.child(Constants.Customer.TIME_TO_WAIT).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.STATUS).getValue() != null) {
                                                map.put(Constants.Customer.STATUS,
                                                        aCustomer.child(Constants.Customer.STATUS).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.TIME_ADDED).getValue() != null) {
                                                map.put(Constants.Customer.TIME_ADDED,
                                                        aCustomer.child(Constants.Customer.TIME_ADDED).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.TIME_FIRST_ADDED_IN_QUEUE).getValue() != null) {
                                                map.put(Constants.Customer.TIME_FIRST_ADDED_IN_QUEUE,
                                                        aCustomer.child(Constants.Customer.TIME_FIRST_ADDED_IN_QUEUE).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.CUSTOMER_ID).getValue() != null) {
                                                map.put(Constants.Customer.CUSTOMER_ID,
                                                        aCustomer.child(Constants.Customer.CUSTOMER_ID).getValue().toString());
                                            }
                                            if (aCustomer.child(Constants.Customer.IS_ANY_BARBER).getValue() != null) {
                                                map.put(Constants.Customer.IS_ANY_BARBER,
                                                        aCustomer.child(Constants.Customer.IS_ANY_BARBER).getValue().toString());
                                            }


                                            String newCustKey = queue.child(selectedKey).push().getKey();
                                            Task<Void> voidTask = queue.child(selectedKey).child(newCustKey).setValue(map);
                                        }
                                    }
                                }
                            }
                            //Add All Any customer
                            //Find any barber
                            //list all customer with Any
                            //push the same customer to this queue

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
