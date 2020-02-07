package com.qcut.biz.ui.waiting_list;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.qcut.biz.R;
import com.qcut.biz.models.Barber;

import java.util.List;

public class BarberSelectionArrayAdapter extends ArrayAdapter<String> {
    private List<Barber> barberList;
    Context mContext;

    public BarberSelectionArrayAdapter(@NonNull Context context, List<Barber> barberList) {
        super(context, R.layout.add_customer_dialog_dropdown);
        this.barberList = barberList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return barberList.size();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private static class ViewHolder {
        ImageView photo;
        TextView name;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.add_customer_dialog_dropdown, parent, false);
        final ImageView photo = (ImageView) convertView.findViewById(R.id.barber_photo_dd);
        TextView name = (TextView) convertView.findViewById(R.id.barber_name_dd);

        if(!barberList.get(position).getName().equalsIgnoreCase("any")) {
            StorageReference child = FirebaseStorage.getInstance().getReference().child(barberList.get(position).getImagePath());

            child.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        Glide.with(getContext())
                                .load(task.getResult())
                                .into(photo);
                    }
                }
            });
        }
        name.setText(barberList.get(position).getName());
        convertView.setTag(barberList.get(position).getKey());
        return convertView;
    }
}
