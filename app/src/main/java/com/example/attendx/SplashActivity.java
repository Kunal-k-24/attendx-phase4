package com.example.attendx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {
    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view to show the logo
        setContentView(R.layout.activity_splash);

        // Delay and start main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginSelectionActivity.class); // Change if needed
            startActivity(intent);
            finish(); // Close splash screen
        }, SPLASH_TIME_OUT);
    }
}
