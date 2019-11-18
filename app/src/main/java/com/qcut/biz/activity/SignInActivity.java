package com.qcut.biz.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.qcut.biz.R;

public class SignInActivity extends AppCompatActivity {

    Button signIn;
    private TextView emailText;
    private TextView passwordText;
    private FirebaseDatabase database = null;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailText = findViewById(R.id.sign_in_email);
        passwordText = findViewById(R.id.sign_in_password);

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();

        sp = getSharedPreferences("login",MODE_PRIVATE);


        signIn = findViewById(R.id.sign_in_screen);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Query query = database.getReference().child("barbershops").
                        orderByChild("email").equalTo(emailText.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot shopData : dataSnapshot.getChildren()) {
                                String email = shopData.child("email").getValue().toString();
                                String password = shopData.child("password").getValue().toString();
                                if (email.equalsIgnoreCase(emailText.getText().toString()) &&
                                        password.equalsIgnoreCase(passwordText.getText().toString())) {
                                    Toast.makeText(SignInActivity.this, "Login Successful.", Toast.LENGTH_LONG).show();
                                    sp.edit().putBoolean("isLoggedIn",true).apply();
                                    sp.edit().putString("userid", shopData.getKey().toString()).apply();
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(SignInActivity.this, "Login unsuccessful.", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(SignInActivity.this, "Login Does not exist.", Toast.LENGTH_LONG).show();

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
            });
        }});

        LayoutInflater factory = LayoutInflater.from(SignInActivity.this);
        final View forgotPasswordView = factory.inflate(R.layout.forgot_password, null);
        final AlertDialog forgotPasswordDialog = new AlertDialog.Builder(SignInActivity.this).create();
        forgotPasswordDialog.setView(forgotPasswordView);

        TextView forgotPasswordBtn = findViewById(R.id.forgot_btn);
        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog.show();
                forgotPasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                forgotPasswordDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
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
