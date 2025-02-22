package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "AttendX_Prefs";
    private static final String KEY_USER_ROLE = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUserRole = sharedPreferences.getString(KEY_USER_ROLE, null);

        new Handler().postDelayed(() -> {
            if ("teacher".equals(savedUserRole)) {
                startActivity(new Intent(SplashActivity.this, TeacherDashboardActivity.class));
            } else if ("student".equals(savedUserRole)) {
                startActivity(new Intent(SplashActivity.this, StudentDashboardActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginSelectionActivity.class));
            }
            finish();
        }, SPLASH_TIME_OUT);
    }
}
