package com.example.interviewai;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.interviewai.data.UserSessionManager;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CANDIDATE_NAME = "extra_candidate_name";
    public static final String EXTRA_ROLE_CATEGORY = "extra_role_category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserSessionManager sessionManager = new UserSessionManager(this);

        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        } else {
            intent = new Intent(MainActivity.this, WelcomeActivity.class);
        }

        // Teruskan data intent jika ada
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }

        startActivity(intent);
        finish();
    }
}