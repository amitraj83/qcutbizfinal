package com.qcut.barber.views.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.qcut.barber.R;

public class StartActivity extends AppCompatActivity {

    private Button signIn, signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
        if(sp.getBoolean("isLoggedIn",false) && (sp.getString("userid",null) != null)){
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_start);
        getSupportActionBar().hide();

        signIn = findViewById(R.id.sign_up_text);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(StartActivity.this, SignInActivity.class);
                startActivity(signInIntent);
            }
        });

        signUp = findViewById(R.id.sign_up);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(StartActivity.this, SignUpActivity.class);
                startActivity(signInIntent);
            }
        });

    }

}
