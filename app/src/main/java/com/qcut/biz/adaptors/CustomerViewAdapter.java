package com.qcut.biz.adaptors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;
import com.qcut.biz.models.Customer;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.util.TimeUtil;

import java.util.ArrayList;

public class CustomerViewAdapter extends RecyclerView.Adapter<CustomerViewAdapter.MyViewHolder> {
    private ArrayList<Customer> dataSet;
    private Context mContext;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private String userid;

    public CustomerViewAdapter(Context context, FirebaseDatabase database, String userid,
                               StorageReference storageReference, ArrayList<Customer> dataSet) {
        this.mContext = context;
        this.dataSet = dataSet;
        this.storageReference = storageReference;
        this.database = database;
        this.userid = userid;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.global_queue_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Customer customer = this.dataSet.get(position);
        holder.custName.setText(customer.getName());
        if (customer.getExpectedWaitingTime() == 0) {
            holder.waitingTime.setText("Ready");
        } else {
            holder.waitingTime.setText(TimeUtil.getDisplayWaitingTime(customer.getExpectedWaitingTime()));
        }
        Drawable waitingDrawable = this.mContext.getResources().getDrawable(R.drawable.ic_action_waiting);
        holder.waitingTime.setCompoundDrawablesWithIntrinsicBounds(null, null, waitingDrawable, null);
        DBUtils.getBarber(database, userid, customer.getActualBarberId(), new OnSuccessListener<Barber>() {
            @Override
            public void onSuccess(Barber barber) {
                if (barber != null) {
                    StorageReference child = storageReference.child(barber.getImagePath());
                    child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                RequestOptions myOptions = new RequestOptions()
                                        .override(125, 125);
                                Glide.with(mContext)
                                        .asBitmap()
                                        .apply(myOptions)
                                        .load(task.getResult())
                                        .into(holder.custBarber);

                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView custName, waitingTime;
        public ImageView custBarber;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.custName = itemView.findViewById(R.id.cust_name);
            this.custBarber = itemView.findViewById(R.id.cust_barber);
            this.waitingTime = itemView.findViewById(R.id.cust_waiting_time);
        }
    }
}
