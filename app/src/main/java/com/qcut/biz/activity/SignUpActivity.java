package com.qcut.biz.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qcut.biz.R;
import com.qcut.biz.models.Shop;

public class SignUpActivity extends AppCompatActivity {

    private Button signUp;
    private FirebaseDatabase database = null;
    private EditText shopContactName;
    private EditText shopNameET;
    private EditText shopEmailET;
    private EditText shopPassET;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shopContactName = (EditText) findViewById(R.id.user_name);
        shopNameET = (EditText) findViewById(R.id.shop_name);
        shopEmailET = (EditText) findViewById(R.id.sign_up_email);
        shopPassET = (EditText) findViewById(R.id.sign_up_password);

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();

        sp = getSharedPreferences("login",MODE_PRIVATE);


        signUp = findViewById(R.id.sign_up_screen);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = shopContactName != null ? shopContactName.getText().toString().trim() : "";
                String shopName = shopNameET != null ? shopNameET.getText().toString().trim() : "";
                if(shopEmailET == null || shopEmailET.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(SignUpActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String shopEmail = shopEmailET.getText() != null ? shopEmailET.getText().toString().trim() : "";
                if(shopPassET == null || shopPassET.getText().toString().trim().equalsIgnoreCase("")){
                    Toast.makeText(SignUpActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String shopPassword = shopPassET.getText() != null ? shopPassET.getText().toString().trim() : "";

                DatabaseReference dbRef = database.getReference().child("barbershops");

                if (dbRef != null) {
                    String key = dbRef.push().getKey();
                    Shop shop = new Shop(key, shopEmail, userName, shopName, shopPassword);
                    dbRef.child(key).setValue(shop);
                    sp.edit().putBoolean("isLoggedIn",true).apply();
                    sp.edit().putString("userid", key).apply();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUpActivity.this, "User Already exists.", Toast.LENGTH_SHORT).show();

                }

            }
        });




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
