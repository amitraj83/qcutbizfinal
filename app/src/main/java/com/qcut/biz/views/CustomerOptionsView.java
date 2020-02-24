package com.qcut.biz.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.qcut.biz.R;
import com.qcut.biz.presenters.CustomerOptionPresenter;

public class CustomerOptionsView {

    Context context;
    PopupWindow popupWindow;
    View removeCustomer;
    View shuffleCustomer;
    String selectedCustomerKey;

    public CustomerOptionsView (Context context, final CustomerOptionPresenter presenter) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupWindowView = inflater.inflate(R.layout.popup_window, null);



        popupWindow = new PopupWindow(popupWindowView,
                (int)(context.getResources().getDimension(R.dimen.popupwindow_size)/ context.getResources().getDisplayMetrics().density),
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        removeCustomer = popupWindowView.findViewById(R.id.remove_customer_row);
        shuffleCustomer = popupWindowView.findViewById(R.id.shuffle_customer_row);
        removeCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onRemoveCustomerClick(selectedCustomerKey);
                popupWindow.dismiss();
            }
        });
        shuffleCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAssignToAnyClick(selectedCustomerKey);
                popupWindow.dismiss();
            }
        });

    }


    public void show(RecyclerView.ViewHolder holder) {
        selectedCustomerKey = holder.itemView.getTag().toString();
        popupWindow.showAsDropDown(holder.itemView.findViewById(R.id.customer_options));

    }
}
