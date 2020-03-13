package com.qcut.biz.adaptors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.vipulasri.timelineview.TimelineView;
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

public class CustomerViewAdapter extends RecyclerView.Adapter<CustomerViewAdapter.TimeLineViewHolder> {
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
    public TimeLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline, parent, false);
        return new TimeLineViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final TimeLineViewHolder holder, int position) {
        Customer customer = this.dataSet.get(position);
        holder.custName.setText(customer.getName());
        if (customer.isInProgress()) {
            Drawable tlProgress = this.mContext.getResources().getDrawable(R.drawable.ic_action_progress);
            holder.waitingTime.setBackground(this.mContext.getResources().getDrawable(R.drawable.rounded_text_view_grey));
            holder.waitingTime.setText("In Chair");
//            holder.mTimelineView.setMarker(tlProgress);
        } else if (customer.getExpectedWaitingTime() == 0) {
            holder.waitingTime.setText("Ready");
            holder.waitingTime.setBackground(this.mContext.getResources().getDrawable(R.drawable.rounded_text_view_green));
            Drawable tlReady = this.mContext.getResources().getDrawable(R.drawable.green_dot);
//            holder.mTimelineView.setMarker(tlReady);
            final Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            animation.cancel();
            animation.reset();
            holder.waitingTime.startAnimation(animation);

//            holder.mTimelineView.startAnimation(animation);
        } else {
            holder.waitingTime.setBackground(this.mContext.getResources().getDrawable(R.drawable.rounded_text_view));
            holder.waitingTime.setText(TimeUtil.getDisplayWaitingTime(customer.getExpectedWaitingTime()));
        }
        //TODO remove db logic, create presenter
        DBUtils.getBarber(database, userid, customer.getActualBarberId(), new OnSuccessListener<Barber>() {
            @Override
            public void onSuccess(Barber barber) {
                if (barber != null) {
                    StorageReference child = storageReference.child(barber.getImagePath());
                    /*child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
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
                    });*/
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }


    public class TimeLineViewHolder extends RecyclerView.ViewHolder {

//        public TimelineView mTimelineView;
        public TextView custName;
        TextView waitingTime;
//        public ImageView custBarber;

        public TimeLineViewHolder(View itemView, int viewType) {
            super(itemView);
//            mTimelineView = (TimelineView) itemView.findViewById(R.id.timeline);
//            mTimelineView.initLine(viewType);
            this.custName = itemView.findViewById(R.id.cust_view_name);
//            this.custBarber = itemView.findViewById(R.id.cus_view_barber);
            this.waitingTime = itemView.findViewById(R.id.cust_view_waiting_time);
        }
    }
}
