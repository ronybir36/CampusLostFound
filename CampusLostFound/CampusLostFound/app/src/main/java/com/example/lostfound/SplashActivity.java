package com.example.lostfound;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfound.util.PrefsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * First screen. Firebase keeps the login token on the phone, so we check it
 * here. If the user is signed in and "remember me" was on, go straight to Home.
 * Otherwise, sign out any leftover session and show Login.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeUser, SPLASH_DELAY_MS);
    }

    private void routeUser() {
        if (isFinishing()) {
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        boolean rememberMe = new PrefsManager(this).isRememberMe();

        Intent next;
        if (user != null && rememberMe) {
            next = new Intent(this, HomeActivity.class);
        } else {
            if (user != null) {
                auth.signOut(); // user did not ask to be remembered
            }
            next = new Intent(this, LoginActivity.class);
        }

        startActivity(next);
        finish();
    }
}
