package com.qcut.biz.ui.waiting_list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.R;
import com.qcut.biz.models.ShopQueueModel;
import com.qcut.biz.util.Status;

import java.util.ArrayList;


public class WaitingListRecyclerViewAdapter extends RecyclerView.Adapter<WaitingListRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<ShopQueueModel> dataSet;
    Context mContext;
    private String barberTtag;
    private FirebaseDatabase database;
    private String userid;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView custName, custStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.custName = itemView.findViewById(R.id.cust_name);
            this.custStatus = itemView.findViewById(R.id.cust_status);
        }
    }

    public WaitingListRecyclerViewAdapter(ArrayList<ShopQueueModel> dataSet, Context mContext,
                                          String tag, FirebaseDatabase database, String userid) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.barberTtag = tag;
        this.database = database;
        this.userid = userid;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.queue_row, parent, false);
        itemView.setOnClickListener(new WaitingListClickListener(mContext, itemView, this.barberTtag, database, userid));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ShopQueueModel shopQueueModel = this.dataSet.get(position);
        holder.itemView.setTag(shopQueueModel.getId());
        holder.custName.setText(shopQueueModel.getName());
        if (shopQueueModel.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
            Drawable waitingDrawable = this.mContext.getResources().getDrawable( R.drawable.ic_action_waiting );
            holder.custStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, waitingDrawable, null);
            holder.custStatus.setText(shopQueueModel.getDisplayTimeToWait());
            holder.custStatus.setTag(Status.QUEUE);
        } else {
            Drawable progressDrawable = this.mContext.getResources().getDrawable( R.drawable.ic_action_progress );
            holder.custStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, progressDrawable, null);
            holder.custStatus.setText("In Chair");
            holder.custStatus.setTag(Status.PROGRESS);
        }
    }

    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }

}
