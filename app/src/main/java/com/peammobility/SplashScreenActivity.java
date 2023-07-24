package com.peammobility;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.peammobility.auth.LoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    ImageView splashScreen;
    String name, userID, loginStatus, authReset;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        splashScreen = findViewById(R.id.splash_image);

        new CountDownTimer((5 * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                loginStatus = sharedPreferences.getString("auth_status", "");
                authReset = sharedPreferences.getString("auth_reset", "null");

//                if (loginStatus.equals("authenticated") && (authReset.equals("no") || authReset.equals("yes"))) {
//                    startActivity(new Intent(SplashScreenActivity.this, PinActivity.class));
//                } else {
//                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
//                }

                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                finish();
            }
        }.start();
    }
}