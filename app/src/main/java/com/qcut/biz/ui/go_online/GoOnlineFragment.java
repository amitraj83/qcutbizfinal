package com.qcut.biz.ui.go_online;

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

public class GoOnlineFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    ListView mListView;
    String[] mHairCuts, mPricesHairCuts;
    Button addService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goOnlineModel =
                ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_go_online, container, false);

        LayoutInflater factory = LayoutInflater.from(getContext());


        //Initialize ListView
        mListView = root.findViewById(R.id.listViewOnline);
        GoOnlineFragment.CustomAdapter customAdapter = new GoOnlineFragment.CustomAdapter();
        mListView.setAdapter(customAdapter);

        String[] areaItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner areaSpinner = (Spinner) root.findViewById(R.id.area_spinner_left);
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, areaItems);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);

        String[] cityItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner citySpinner = (Spinner) root.findViewById(R.id.city_spinner_left);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, cityItems);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        String[] countryItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner countrySpinner = (Spinner) root.findViewById(R.id.country_spinner_left);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, countryItems);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        String[] expensiveItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner expensiveSpinner = (Spinner) root.findViewById(R.id.expensive_spinner_left);
        ArrayAdapter<String> expensiveAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, expensiveItems);
        expensiveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expensiveSpinner.setAdapter(expensiveAdapter);

        String[] mondayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner mondayLeftSpinner = (Spinner) root.findViewById(R.id.monday_spinner_left);
        ArrayAdapter<String> mondayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, mondayLeftItems);
        mondayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mondayLeftSpinner.setAdapter(mondayLeftAdapter);

        String[] mondayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner mondayRightSpinner = (Spinner) root.findViewById(R.id.monday_spinner_right);
        ArrayAdapter<String> mondayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, mondayRightItems);
        mondayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mondayRightSpinner.setAdapter(mondayRightAdapter);

        String[] tuesdayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner tuesdayLeftSpinner = (Spinner) root.findViewById(R.id.tuesday_spinner_left);
        ArrayAdapter<String> tuesdayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, tuesdayLeftItems);
        tuesdayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tuesdayLeftSpinner.setAdapter(tuesdayLeftAdapter);

        String[] tuesdayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner tuesdayRightSpinner = (Spinner) root.findViewById(R.id.tuesday_spinner_right);
        ArrayAdapter<String> tuesdayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, tuesdayRightItems);
        tuesdayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tuesdayRightSpinner.setAdapter(tuesdayRightAdapter);

        String[] wednesdayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner wednesdayLeftSpinner = (Spinner) root.findViewById(R.id.wednesday_spinner_left);
        ArrayAdapter<String> wednesdayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, wednesdayLeftItems);
        wednesdayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wednesdayLeftSpinner.setAdapter(wednesdayLeftAdapter);

        String[] wednesdayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner wednesdayRightSpinner = (Spinner) root.findViewById(R.id.wednesday_spinner_right);
        ArrayAdapter<String> wednesdayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, wednesdayRightItems);
        wednesdayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wednesdayRightSpinner.setAdapter(wednesdayRightAdapter);

        String[] thursdayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner thursdayLeftSpinner = (Spinner) root.findViewById(R.id.thurday_spinner_left);
        ArrayAdapter<String> thursdayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, thursdayLeftItems);
        thursdayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        thursdayLeftSpinner.setAdapter(thursdayLeftAdapter);

        String[] thursdayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner thursdayRightSpinner = (Spinner) root.findViewById(R.id.thurday_spinner_right);
        ArrayAdapter<String> thurdayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, thursdayRightItems);
        thurdayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        thursdayRightSpinner.setAdapter(thurdayRightAdapter);

        String[] fridayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner fridayLeftSpinner = (Spinner) root.findViewById(R.id.friday_spinner_left);
        ArrayAdapter<String> fridayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, fridayLeftItems);
        fridayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fridayLeftSpinner.setAdapter(fridayLeftAdapter);

        String[] fridayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner fridayRightSpinner = (Spinner) root.findViewById(R.id.friday_spinner_right);
        ArrayAdapter<String> fridayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, fridayRightItems);
        fridayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fridayRightSpinner.setAdapter(fridayRightAdapter);

        String[] saturdayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner saturdayLeftSpinner = (Spinner) root.findViewById(R.id.saturday_spinner_left);
        ArrayAdapter<String> saturdayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, saturdayLeftItems);
        saturdayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        saturdayLeftSpinner.setAdapter(saturdayLeftAdapter);

        String[] saturdayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner saturdayRightSpinner = (Spinner) root.findViewById(R.id.saturday_spinner_right);
        ArrayAdapter<String> saturdayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, saturdayRightItems);
        saturdayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        saturdayRightSpinner.setAdapter(saturdayRightAdapter);

        String[] sundayLeftItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner sundayLeftSpinner = (Spinner) root.findViewById(R.id.sunday_spinner_left);
        ArrayAdapter<String> sundayLeftAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, sundayLeftItems);
        sundayLeftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sundayLeftSpinner.setAdapter(sundayLeftAdapter);

        String[] sundayRightItems = new String[] {
                "Arial", "Arial", "Arial",
        };
        Spinner sundayRightSpinner = (Spinner) root.findViewById(R.id.sunday_spinner_right);
        ArrayAdapter<String> sundayRightAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, sundayRightItems);
        sundayRightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sundayRightSpinner.setAdapter(sundayRightAdapter);


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

            @SuppressLint("ViewHolder") View listView_layout = getLayoutInflater().inflate(R.layout.online_list_item, null);

            TextView mHairCut = listView_layout.findViewById(R.id.hair_cuts);
            TextView mHairCutPrice = listView_layout.findViewById(R.id.hair_cuts_price);

            mHairCut.setText(mHairCuts[i]);
            mHairCutPrice.setText(mPricesHairCuts[i]);

            return listView_layout;
        }
    }
}