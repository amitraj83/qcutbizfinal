package com.qcut.barber.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.qcut.barber.R;
import com.qcut.barber.models.GoOnlineModel;
import com.qcut.barber.presenters.fragments.ShopDetailsPresenter;
import com.qcut.barber.views.ShopDetailsView;

import org.apache.commons.lang3.StringUtils;

public class ShopDetailsFragment extends Fragment implements ShopDetailsView {

    private GoOnlineModel goOnlineModel;
    private TextView emailTV;
    private TextView passwordTV;
    private TextView contactName;
    private TextView shopNameTV;
    private TextView shopAddress1TV;
    private TextView shopAddress2TV;
    private TextView googleMapLinkTV;
    private TextView avgTimeToCutTv;
    private Spinner city;
    //private Spinner area;
    private Spinner country;
    private Button save;

    private ShopDetailsPresenter presenter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        goOnlineModel = ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_shop_details, container, false);
        if (presenter == null) {
            presenter = new ShopDetailsPresenter(this, getContext());
            emailTV = root.findViewById(R.id.shop_details_email);
            passwordTV = root.findViewById(R.id.shop_details_password);
            contactName = root.findViewById(R.id.shop_details_contact_name);
            shopNameTV = root.findViewById(R.id.shop_details_shop_name);
            shopAddress1TV = root.findViewById(R.id.shop_details_address_1);
            shopAddress2TV = root.findViewById(R.id.shop_details_address_2);
            googleMapLinkTV = root.findViewById(R.id.shop_details_gmap_link);
            avgTimeToCutTv = root.findViewById(R.id.shop_details_avg_time_to_cut);
            city = root.findViewById(R.id.shop_details_city);
            //area = (Spinner) root.findViewById(R.id.shop_details_area);
            country = root.findViewById(R.id.shop_details_country);
            save = root.findViewById(R.id.shop_details_save);


            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onSaveClick();
                }
            });

            ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, presenter.getCities());
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            city.setAdapter(cityAdapter);

            ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, presenter.getCountries());
            countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            country.setAdapter(countryAdapter);

        }
        presenter.populateData();
        return root;
    }

    @Override
    public void setEmail(String email) {
        emailTV.setText(email);
    }

    @Override
    public void setPassword(String password) {
        passwordTV.setText(password);
    }

    @Override
    public void setName(String name) {
        contactName.setText(name);

    }

    @Override
    public void setShopName(String shopName) {
        shopNameTV.setText(shopName);
    }

    @Override
    public void setAddress1(String addressLine1) {
        shopAddress1TV.setText(addressLine1);
    }

    @Override
    public void setAddress2(String addressLine2) {
        shopAddress2TV.setText(addressLine2);
    }

    @Override
    public void setAvgTimeToCut(long avgTimeToCut) {
        avgTimeToCutTv.setText(String.valueOf(avgTimeToCut));
    }

    @Override
    public void setGmapLink(String gmapLink) {
        googleMapLinkTV.setText(gmapLink);
    }

    @Override
    public void selectCityIndex(int cityIndex) {
        city.setSelection(cityIndex);
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getAddressLine1() {
        return shopAddress1TV.getText().toString();
    }

    @Override
    public String getPassword() {
        return passwordTV.getText().toString();
    }

    @Override
    public String getName() {
        return contactName.getText().toString();
    }

    @Override
    public String getAddressLine2() {
        return shopAddress2TV.getText().toString();
    }

    @Override
    public String getShopName() {
        return shopNameTV.getText().toString();
    }

    @Override
    public String getGmapLink() {
        return googleMapLinkTV.getText().toString();
    }

    @Override
    public long getAvgTimeToCut() {
        final CharSequence text = avgTimeToCutTv.getText();
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        return Long.valueOf(String.valueOf(text));
    }

    @Override
    public String getSelectedCity() {
        return city.getSelectedItem().toString();
    }

    @Override
    public String getSelectedCountry() {
        return country.getSelectedItem().toString();
    }

    @Override
    public String getEmail() {
        return emailTV.getText().toString();
    }
}