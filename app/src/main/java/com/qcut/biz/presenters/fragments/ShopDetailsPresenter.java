package com.qcut.biz.presenters.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.models.ShopDetails;
import com.qcut.biz.util.DBUtils;
import com.qcut.biz.views.ShopDetailsView;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class ShopDetailsPresenter {

    private FirebaseDatabase database;
    private String userid;
    private ShopDetailsView view;
    private SharedPreferences preferences;
    private Context context;
    private static String[] CITIES = {"Dublin1", "Roscommon", "Roscrea", "Mountrath", "Dublin", "Porterstown", "Carrigaline", "Cork", "Ballina", "Manorhamilton", "Kells", "Listowel", "Lisselton", "Carrick", "Galway", "Tipperary", "Carlow", "Abbeyleix", "Mullinavat", "Maynooth", "Tallaght", "Templeogue", "Swords", "Bray", "Cabinteely", "Clondalkin", "Ballyfermot", "Artane", "Malahide", "Limerick", "Saggart", "Prosperous", "Castleknock", "Clonsilla", "Kilkenny", "Droichead Nua", "Kildare", "Celbridge", "Lucan", "Blackrock", "Dundrum", "Wicklow", "Rathgar", "Stillorgan", "Mullingar", "Clonmel", "Sandyford", "Ashbourne", "Athlone", "Marino", "Dalkey", "Finglas", "Glasnevin", "Naas", "Blessington", "Waterford", "Leixlip", "Walkinstown", "Mayo", "Terenure", "Clane", "Killarney", "Trim", "Seafield", "Mount Merrion", "Glen", "Nenagh", "Shannon", "Kilbride Cross Roads", "Killybegs", "Sligo", "Athboy", "Crosshaven", "Drogheda", "Youghal", "Greystones", "Ballsbridge", "Foxrock", "Shankill", "Killiney", "Abbeyfeale", "Dunshaughlin", "Navan", "Ballincollig", "Ardee", "Ballyroe", "Sandymount", "Slieve", "Macroom", "Wexford", "Blanchardstown", "Milltown", "Dundalk", "Athy", "Kilrush", "Dungarvan", "Gorey", "Letterkenny", "Ballymahon", "Island", "Stepaside", "Ballintober", "Firhouse", "Cabra", "Donabate", "Coolock", "Rathfarnham", "Thurles", "Kilcock", "Sutton", "Newport", "Monaghan", "Hospital", "Bettystown", "River", "Tralee", "Ballybrit", "Cobh", "Enniskerry", "Duleek", "Rush", "Lusk", "Mitchelstown", "Kanturk", "Balbriggan", "Rathmolyon", "Crumlin", "Clontarf", "Kingswood", "Kinsale", "Dunboyne", "Caher", "Broadford", "Kilmichael", "Berrings", "Ballineen", "Tyrrellspass", "Donegal", "Bundoran", "Glenties", "Killygordon", "Athenry", "Moycullen", "Mallow", "Castlebar", "Ratoath", "Bandon", "Douglas", "Glengarriff", "Ballygarvan", "Clonakilty", "Ballyphilip", "Boherbue", "Kilfinane", "Fermoy", "Carbury", "Monasterevin", "Monkstown", "Clogherhead", "Enfield", "Edenderry", "Kildalkey", "Castlerea", "Stradbally", "Skerries", "Garristown", "Santry", "Ballybrack", "DÃºn Laoghaire", "Leopardstown", "Ballyhooly", "Claregalway", "Dunlavin", "Straffan", "Summerhill", "Ballymount", "Courtown", "Loughrea", "Enniscorthy", "Portarlington", "Buncrana", "Cashel", "Carrickmacross", "Geevagh", "Killala", "Carrick on Shannon", "Saint Mullins", "Carrickmines", "Killorglin", "Passage West", "Ennis", "Oysterhaven", "Cavan", "Virginia", "Clones", "Bailieborough", "Oranmore", "Shrule", "Callan", "Tullow", "Carnew", "New Ross", "Castlebridge", "Glenealy", "Carrick-on-Suir", "Ballymote", "Longford", "Ballivor", "Louth", "Castleblayney", "Birr", "Clarecastle", "Tullamore", "Ballon", "Kingscourt", "Coachford", "Clonee", "Kilmainham", "County Wexford", "Balgriffin", "Kiltamagh", "Foxford", "Westport", "Claremorris", "Swinford", "Foynes", "Sixmilebridge", "Newcastle West", "Grange", "Dunleer", "Longwood", "Killaloe", "Feakle", "Baldoyle", "Windy Arbour", "Doughiska", "Thomastown", "Raheny", "Dundrum", "Rathowen", "Patrickswell", "Delgany", "Ballyvaghan", "Doolin", "Killurin", "Midleton", "Ringaskiddy", "Skibbereen", "Ballinadee", "Kinvarra", "Oughterard", "Ballinrobe", "Tuam", "Headford", "Spiddal", "Gort", "Williamstown", "Ballinasloe", "Cahersiveen", "Bantry", "Crookhaven", "Portumna", "County Galway", "Kilmore", "Leamlara", "Bagenalstown", "Ballyragget", "Carraroe", "Killinick", "Crusheen", "Glanmire", "Arklow", "Kilcoole", "Kilcullen", "Ferbane", "Mornington", "Naul", "Donnybrook", "Portmarnock", "Ballycullen", "Sallins", "Irishtown", "Ballymun", "Jamestown", "Bodyke", "Rathcoole", "Rathmines", "Inchicore", "Kenmare", "Sallynoggin", "Carrigtohill", "Cross", "Castlemaine", "Newmarket"};
    private static String[] COUNTRIES = {"Ireland"};
    private ShopDetails shopDetails;

    public ShopDetailsPresenter(ShopDetailsView view, Context context) {
        this.view = view;
        this.context = context;
        this.preferences = context.getSharedPreferences("login", MODE_PRIVATE);
        FirebaseApp.initializeApp(context);
        database = FirebaseDatabase.getInstance();
        userid = preferences.getString("userid", null);
        Arrays.sort(CITIES);
    }

    public String[] getCities() {
        return CITIES;
    }

    public String[] getCountries() {
        return COUNTRIES;
    }

    public void populateData() {
        DBUtils.getShopDetails(database, userid, new OnSuccessListener<ShopDetails>() {

            @Override
            public void onSuccess(ShopDetails shopDetails) {
                ShopDetailsPresenter.this.shopDetails = shopDetails;
                view.setEmail(shopDetails.getEmail());
                view.setPassword(shopDetails.getPassword());
                view.setName(shopDetails.getName());
                view.setShopName(shopDetails.getShopName());
                view.setAddress1(shopDetails.getAddressLine1());
                view.setAddress2(shopDetails.getAddressLine2());
                view.setAvgTimeToCut(shopDetails.getAvgTimeToCut());
                view.setGmapLink(shopDetails.getGmapLink());
                int cityIndex = Arrays.asList(CITIES).indexOf(String.valueOf(shopDetails.getCity()));
                view.selectCityIndex(cityIndex);
            }
        });
    }

    public void onSaveClick() {
        String password = view.getPassword();
        if (password == null || password.trim().length() == 0) {
            view.showMessage("Password cannot be empty");

        } else if (password.trim().length() < 8) {
            view.showMessage("Password must be at least 8 character long");
        } else {
            saveShopDetails();
        }
    }

    private void saveShopDetails() {
        final ShopDetails.ShopDetailsBuilder builder = ShopDetails.builder();
        builder.email(view.getEmail()).name(view.getName()).password(view.getPassword())
                .addressLine1(view.getAddressLine1()).status(shopDetails.getStatus())
                .addressLine2(view.getAddressLine2()).shopName(view.getShopName()).gmapLink(view.getGmapLink())
                .avgTimeToCut(view.getAvgTimeToCut()).city(view.getSelectedCity()).country(view.getSelectedCountry());
        if (shopDetails != null && StringUtils.isNotBlank(shopDetails.getKey())) {
            builder.key(shopDetails.getKey());
        }
        DBUtils.saveShopDetails(database, userid, builder.build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                view.showMessage("Details saved successfully");
            }
        });
    }
}
