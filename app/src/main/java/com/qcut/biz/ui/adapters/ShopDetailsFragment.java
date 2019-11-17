package com.qcut.biz.ui.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.qcut.biz.R;
import com.qcut.biz.ui.go_online.GoOnlineModel;

public class ShopDetailsFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    ListView mListView;
    String[] mHairCuts, mPricesHairCuts;
    Button addService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goOnlineModel =
                ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_shop_details, container, false);


        return root;
    }

    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mHairCuts.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return view;
        }
    }
}