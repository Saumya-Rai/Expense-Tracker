package com.example.savss.expensetracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent toLoginPage = new Intent(view.getContext(), LoginActivity.class);
                view.getContext().startActivity(toLoginPage);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Button loginButton = (Button) findViewById(R.id.loginButton);

    }
}