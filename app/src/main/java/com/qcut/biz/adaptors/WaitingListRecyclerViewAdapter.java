package com.qcut.biz.adaptors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qcut.biz.R;
import com.qcut.biz.models.Customer;
import com.qcut.biz.models.CustomerStatus;

import java.util.List;


public class WaitingListRecyclerViewAdapter extends RecyclerView.Adapter<WaitingListRecyclerViewAdapter.MyViewHolder> {

    private List<Customer> dataSet;
    private Context mContext;
    private View.OnClickListener waitingListClickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView custName, custStatus, forWho;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.custName = itemView.findViewById(R.id.cust_name);
            this.custStatus = itemView.findViewById(R.id.cust_status);
            this.forWho = itemView.findViewById(R.id.for_who);
        }
    }

    public WaitingListRecyclerViewAdapter(List<Customer> dataSet, Context mContext,
                                          View.OnClickListener waitingListClickListener) {
        this.dataSet = dataSet;
        this.mContext = mContext;
        this.waitingListClickListener = waitingListClickListener;
    }

    public void setDataSet(List<Customer> dataSet) {
        this.dataSet = dataSet;
    }

    public List<Customer> getDataSet() {
        return this.dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.queue_row, parent, false);
        itemView.setOnClickListener(waitingListClickListener);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Customer customer = this.dataSet.get(position);
        holder.itemView.setTag(customer.getKey());
        holder.custName.setText(customer.getName());
        if (customer.isAnyBarber()) {
            holder.forWho.setText("Any");
        } else {
            holder.forWho.setText("You");
        }
        if (customer.getStatus().equalsIgnoreCase(CustomerStatus.QUEUE.name())) {
            Drawable waitingDrawable = this.mContext.getResources().getDrawable(R.drawable.ic_action_waiting);
            holder.custStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, waitingDrawable, null);
            holder.custStatus.setText(String.valueOf(customer.getExpectedWaitingTime()));
            holder.custStatus.setTag(CustomerStatus.QUEUE);
        } else {
            Drawable progressDrawable = this.mContext.getResources().getDrawable(R.drawable.ic_action_progress);
            holder.custStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, progressDrawable, null);
            holder.custStatus.setText("In Chair");
            holder.custStatus.setTag(CustomerStatus.PROGRESS);
        }
    }

    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }

}
