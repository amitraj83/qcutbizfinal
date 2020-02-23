package com.qcut.biz.listeners;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.BarberQueue;
import com.qcut.biz.models.BarberStatus;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerStatus;
import com.qcut.biz.util.Constants;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.MappingUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class CustomerOptionsListener implements View.OnClickListener {
    private Context context;
    private RecyclerView.ViewHolder holder;
    String userid;

    public CustomerOptionsListener(Context context, RecyclerView.ViewHolder holder) {
        this.context = context;
        this.holder = holder;
        this.userid = context.getSharedPreferences("login", MODE_PRIVATE).getString("userid", null);
    }
    @Override
    public void onClick(View v) {
//        Context wrapper = new ContextThemeWrapper(context, R.style.cust_popup_menu);
//        final PopupMenu popup = new PopupMenu(wrapper, holder.itemView.findViewById(R.id.customer_options));
//        popup.inflate(R.menu.customer_menu);
//        popup.getMenu().add(-1, 100, 100, "Assign to Any");
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.remove_customer: removeCustomer(); break;
//                    case 100: assignToAny(); break;
//                }
//                return true;
//            }
//        });
//        popup.show();

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupWindowView = inflater.inflate(R.layout.popup_window, null);



        final PopupWindow popup = new PopupWindow(popupWindowView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popup.showAsDropDown(holder.itemView.findViewById(R.id.customer_options));
        View removeCustomer = popupWindowView.findViewById(R.id.remove_customer_row);
        removeCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCustomer();
                popup.dismiss();
            }
        });
        View shuffleCustomer = popupWindowView.findViewById(R.id.shuffle_customer_row);
        shuffleCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignToAny();
                popup.dismiss();
            }
        });
//        popup.

    }

    private void assignToAny() {
        final String customerKey = holder.itemView.getTag().toString();
        DatabaseReference dbRefBarberQueues = DBUtils.getDbRefBarberQueues(FirebaseDatabase.getInstance(), userid);
        dbRefBarberQueues.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot next = iterator.next();
                        Iterator<DataSnapshot> customerIT = next.getChildren().iterator();
                        while (customerIT.hasNext()) {
                            DataSnapshot custSnapshot = customerIT.next();
                            Customer customer = MappingUtils.mapToCustomer(custSnapshot);
                            if (customer.getKey().equalsIgnoreCase(customerKey)) {
                                custSnapshot.child(Constants.Customer.IS_ANY_BARBER).getRef().setValue(true);
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

    public void removeCustomer () {
        final String customerKey = holder.itemView.getTag().toString();
        DatabaseReference dbRefBarberQueues = DBUtils.getDbRefBarberQueues(FirebaseDatabase.getInstance(), userid);
        dbRefBarberQueues.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot next = iterator.next();
                        Iterator<DataSnapshot> customerIT = next.getChildren().iterator();
                        while (customerIT.hasNext()) {
                            DataSnapshot custSnapshot = customerIT.next();
                            Customer customer = MappingUtils.mapToCustomer(custSnapshot);
                            if (customer.getKey().equalsIgnoreCase(customerKey)) {
                                if (customer.getStatus().equalsIgnoreCase(CustomerStatus.QUEUE.name())) {
                                    custSnapshot.getRef().child(Constants.Customer.STATUS).setValue(CustomerStatus.REMOVED);
                                } else {
                                    Toast.makeText(context, "Failed - Customer not in Queue", Toast.LENGTH_SHORT).show();
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
}
