package com.qcut.barber.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.qcut.barber.models.GoOnlineModel;

public class GoOnlineFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    ListView mListView;
    String[] mHairCuts, mPricesHairCuts;
    Button addService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        return null;
    }

}