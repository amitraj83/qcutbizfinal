package com.qcut.biz.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.qcut.biz.R;
import com.qcut.biz.models.ShopQueueModel;
import com.qcut.biz.util.Status;

import java.util.ArrayList;

public class ShopQueueModelAdaptor extends ArrayAdapter<ShopQueueModel> {
    private ArrayList<ShopQueueModel> dataSet;
    Context mContext;

    public ShopQueueModelAdaptor(ArrayList<ShopQueueModel> data, Context context) {
        super(context, R.layout.queue_row, data);
        this.dataSet = data;
        this.mContext=context;

    }

    private int lastPosition = -1;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtStatus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ShopQueueModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.queue_row, parent, false);
        viewHolder.txtName = (TextView) convertView.findViewById(R.id.cust_name);
        viewHolder.txtStatus= (TextView) convertView.findViewById(R.id.cust_status);
        convertView.setTag(dataModel.getId());

        viewHolder.txtName.setText(dataModel.getName());
        TextView txtStatus = viewHolder.txtStatus;


        if (dataModel.getStatus().equalsIgnoreCase(Status.QUEUE.name())) {
            Drawable waitingDrawable = parent.getContext().getResources().getDrawable( R.drawable.ic_action_waiting );
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, waitingDrawable, null);
            txtStatus.setText(dataModel.getDisplayTimeToWait());
        } else {
            Drawable progressDrawable = parent.getContext().getResources().getDrawable( R.drawable.ic_action_progress );
            txtStatus.setCompoundDrawablesWithIntrinsicBounds(null, null, progressDrawable, null);
            txtStatus.setText("In Chair");
        }
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        //do your sorting here
        super.notifyDataSetChanged();
    }


}
