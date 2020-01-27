package com.qcut.biz.ui.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;
import com.qcut.biz.ui.go_online.GoOnlineModel;
import com.qcut.biz.util.Status;
import com.qcut.biz.util.TimeUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ShopDetailsFragment extends Fragment {

    private GoOnlineModel goOnlineModel;
    private FirebaseDatabase database = null;
    private TextView emailTV;
    private TextView passwordTV;
    private TextView yourNameTV;
    private TextView shopNameTV;
    private TextView shopAddress1TV;
    private TextView shopAddress2TV;
    private TextView googleMapLinkTV;
    private TextView avgTimeToCut;
    private Spinner city;
    //private Spinner area;
    private Spinner country;
    private Button save;
    private String userid;
    private SharedPreferences sp;
    private String[] cities = {"Dublin1","Roscommon","Roscrea","Mountrath","Dublin","Porterstown","Carrigaline","Cork","Ballina","Manorhamilton","Kells","Listowel","Lisselton","Carrick","Galway","Tipperary","Carlow","Abbeyleix","Mullinavat","Maynooth","Tallaght","Templeogue","Swords","Bray","Cabinteely","Clondalkin","Ballyfermot","Artane","Malahide","Limerick","Saggart","Prosperous","Castleknock","Clonsilla","Kilkenny","Droichead Nua","Kildare","Celbridge","Lucan","Blackrock","Dundrum","Wicklow","Rathgar","Stillorgan","Mullingar","Clonmel","Sandyford","Ashbourne","Athlone","Marino","Dalkey","Finglas","Glasnevin","Naas","Blessington","Waterford","Leixlip","Walkinstown","Mayo","Terenure","Clane","Killarney","Trim","Seafield","Mount Merrion","Glen","Nenagh","Shannon","Kilbride Cross Roads","Killybegs","Sligo","Athboy","Crosshaven","Drogheda","Youghal","Greystones","Ballsbridge","Foxrock","Shankill","Killiney","Abbeyfeale","Dunshaughlin","Navan","Ballincollig","Ardee","Ballyroe","Sandymount","Slieve","Macroom","Wexford","Blanchardstown","Milltown","Dundalk","Athy","Kilrush","Dungarvan","Gorey","Letterkenny","Ballymahon","Island","Stepaside","Ballintober","Firhouse","Cabra","Donabate","Coolock","Rathfarnham","Thurles","Kilcock","Sutton","Newport","Monaghan","Hospital","Bettystown","River","Tralee","Ballybrit","Cobh","Enniskerry","Duleek","Rush","Lusk","Mitchelstown","Kanturk","Balbriggan","Rathmolyon","Crumlin","Clontarf","Kingswood","Kinsale","Dunboyne","Caher","Broadford","Kilmichael","Berrings","Ballineen","Tyrrellspass","Donegal","Bundoran","Glenties","Killygordon","Athenry","Moycullen","Mallow","Castlebar","Ratoath","Bandon","Douglas","Glengarriff","Ballygarvan","Clonakilty","Ballyphilip","Boherbue","Kilfinane","Fermoy","Carbury","Monasterevin","Monkstown","Clogherhead","Enfield","Edenderry","Kildalkey","Castlerea","Stradbally","Skerries","Garristown","Santry","Ballybrack","DÃºn Laoghaire","Leopardstown","Ballyhooly","Claregalway","Dunlavin","Straffan","Summerhill","Ballymount","Courtown","Loughrea","Enniscorthy","Portarlington","Buncrana","Cashel","Carrickmacross","Geevagh","Killala","Carrick on Shannon","Saint Mullins","Carrickmines","Killorglin","Passage West","Ennis","Oysterhaven","Cavan","Virginia","Clones","Bailieborough","Oranmore","Shrule","Callan","Tullow","Carnew","New Ross","Castlebridge","Glenealy","Carrick-on-Suir","Ballymote","Longford","Ballivor","Louth","Castleblayney","Birr","Clarecastle","Tullamore","Ballon","Kingscourt","Coachford","Clonee","Kilmainham","County Wexford","Balgriffin","Kiltamagh","Foxford","Westport","Claremorris","Swinford","Foynes","Sixmilebridge","Newcastle West","Grange","Dunleer","Longwood","Killaloe","Feakle","Baldoyle","Windy Arbour","Doughiska","Thomastown","Raheny","Dundrum","Rathowen","Patrickswell","Delgany","Ballyvaghan","Doolin","Killurin","Midleton","Ringaskiddy","Skibbereen","Ballinadee","Kinvarra","Oughterard","Ballinrobe","Tuam","Headford","Spiddal","Gort","Williamstown","Ballinasloe","Cahersiveen","Bantry","Crookhaven","Portumna","County Galway","Kilmore","Leamlara","Bagenalstown","Ballyragget","Carraroe","Killinick","Crusheen","Glanmire","Arklow","Kilcoole","Kilcullen","Ferbane","Mornington","Naul","Donnybrook","Portmarnock","Ballycullen","Sallins","Irishtown","Ballymun","Jamestown","Bodyke","Rathcoole","Rathmines","Inchicore","Kenmare","Sallynoggin","Carrigtohill","Cross","Castlemaine","Newmarket"};
    private String[] countries = {"Ireland"};
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goOnlineModel =
                ViewModelProviders.of(this).get(GoOnlineModel.class);
        View root = inflater.inflate(R.layout.fragment_shop_details, container, false);

        database = FirebaseDatabase.getInstance();

        emailTV = (TextView) root.findViewById(R.id.shop_details_email);
        passwordTV = (TextView) root.findViewById(R.id.shop_details_password);
        yourNameTV = (TextView) root.findViewById(R.id.shop_details_contact_name);
        shopNameTV = (TextView) root.findViewById(R.id.shop_details_shop_name);
        shopAddress1TV = (TextView) root.findViewById(R.id.shop_details_address_1);
        shopAddress2TV = (TextView) root.findViewById(R.id.shop_details_address_2);
        googleMapLinkTV = (TextView) root.findViewById(R.id.shop_details_gmap_link);
        avgTimeToCut = (TextView) root.findViewById(R.id.shop_details_avg_time_to_cut);
        city = (Spinner) root.findViewById(R.id.shop_details_city);
        //area = (Spinner) root.findViewById(R.id.shop_details_area);
        country = (Spinner) root.findViewById(R.id.shop_details_country);
        save = (Button) root.findViewById(R.id.shop_details_save);

        sp = getContext().getSharedPreferences("login", MODE_PRIVATE);
        userid = sp.getString("userid", null);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = String.valueOf(passwordTV.getText());
                if(password == null || password.trim().length() == 0) {
                    Toast.makeText(getContext(),
                            "Password cannot be empty", Toast.LENGTH_SHORT).show();

                } else if (password.trim().length() < 8) {
                    Toast.makeText(getContext(),
                            "Password must be at least 8 character long", Toast.LENGTH_LONG).show();

                } else {
                    saveShopDetails();
                }
            }
        });

        Arrays.sort(cities);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        city.setAdapter(cityAdapter);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(countryAdapter);

        populateData();


        return root;
    }

    private void populateData() {
        DatabaseReference shopDetailsRef = database.getReference().child("barbershops").child(userid);
        shopDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    DataSnapshot email = dataSnapshot.child("email");
                    if(email != null && email.getValue() !=null) {
                        emailTV.setText(String.valueOf(email.getValue()));
                    }

                    DataSnapshot password = dataSnapshot.child("password");
                    if(password != null && password.getValue() !=null) {
                        passwordTV.setText(String.valueOf(password.getValue()));
                    }

                    DataSnapshot name = dataSnapshot.child("name");
                    if(name != null && name.getValue() !=null) {
                        yourNameTV.setText(String.valueOf(name.getValue()));
                    }

                    DataSnapshot shopName = dataSnapshot.child("shopname");
                    if(shopName != null && shopName.getValue() !=null) {
                        shopNameTV.setText(String.valueOf(shopName.getValue()));
                    }

                    DataSnapshot addressLine1 = dataSnapshot.child("addressLine1");
                    if(addressLine1 != null && addressLine1.getValue() !=null) {
                        shopAddress1TV.setText(String.valueOf(addressLine1.getValue()));
                    }

                    DataSnapshot addressLine2 = dataSnapshot.child("addressLine2");
                    if(addressLine2 != null && addressLine2.getValue() !=null) {
                        shopAddress2TV.setText(String.valueOf(addressLine2.getValue()));
                    }

                    DataSnapshot gmaplink = dataSnapshot.child("gmaplink");
                    if(gmaplink != null && gmaplink.getValue() !=null) {
                        googleMapLinkTV.setText(String.valueOf(gmaplink.getValue()));
                    }

                    DataSnapshot cityValue = dataSnapshot.child("city");
                    if(cityValue != null && cityValue.getValue() !=null) {
                        int selectedIndex = Arrays.asList(cities).indexOf(String.valueOf(cityValue.getValue()));
                        city.setSelection(selectedIndex);
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean notEmpty(TextView textView){
        if(textView != null && textView.getText() != null && textView.getText().toString() != null
        && textView.getText().toString().trim().length() != 0) {
            return true;
        }
        return false;
    }

    private void saveShopDetails() {
        final DatabaseReference queue = database.getReference().child("barbershops").child(userid);

        Map<String, Object> map = new HashMap<>();
        map.put("password", passwordTV.getText().toString().trim());
        if(notEmpty(yourNameTV)) {
            map.put("name", yourNameTV.getText().toString().trim());
        }
        if(notEmpty(shopNameTV)) {
            map.put("shopname", shopNameTV.getText().toString().trim());
        }
        if(notEmpty(shopAddress1TV)) {
            map.put("addressLine1", shopAddress1TV.getText().toString().trim());
        }
        if(notEmpty(shopAddress2TV)) {
            map.put("addressLine2", shopAddress2TV.getText().toString().trim());
        }
        if(notEmpty(googleMapLinkTV)) {
            map.put("gmaplink", googleMapLinkTV.getText().toString().trim());
        }
        if(notEmpty(avgTimeToCut)) {
            map.put("avgTimeToCut", avgTimeToCut.getText().toString().trim());
        }
        if(city != null && city.getSelectedItem() != null) {
            map.put("city", city.getSelectedItem().toString());
        }
        if(country != null && country.getSelectedItem() != null) {
            map.put("country", country.getSelectedItem().toString());
        }
        queue.updateChildren(map);
        Toast.makeText(getContext(),
                "Details saved successfully", Toast.LENGTH_SHORT).show();

    }

}