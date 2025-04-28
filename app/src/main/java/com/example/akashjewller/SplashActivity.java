package com.example.akashjewller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Re-added Firebase Auth imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT_FIRST_LOGIN = 1500; // 1.5 seconds delay for first login
    private static final int SPLASH_TIMEOUT_RETURNING_USER = 500; // 0.5 seconds for returning users

    // --- SharedPreferences Constants (Should match EmailLoginActivity) ---
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_STAY_SIGNED_IN = "staySignedIn";
    private static final String PREF_USER_TYPE = "userType"; // "admin" or "user"
    private static final String PREF_USER_IDENTIFIER = "userIdentifier"; // email or admin identifier
    private static final String USER_TYPE_ADMIN = "admin";
    private static final String USER_TYPE_REGULAR = "user";
    // --------------------------------------------------------------------

    private SharedPreferences sharedPreferences;
    // Firebase Auth instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Determine splash timeout based on login status
        int splashTimeout = determineSplashTimeout();

        // Handler to redirect after delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                // --- Step 1: Check Firebase Auth (for OTP Login primarily) ---
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    // User is logged in via Firebase Auth (likely OTP)
                    // Go directly to User Dashboard
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                    // Optionally pass Firebase User ID if needed
                    intent.putExtra("FIREBASE_UID", currentUser.getUid());

                } else {
                    // --- Step 2: If no Firebase user, check SharedPreferences (for Email "Stay Signed In") ---
                    boolean staySignedInPref = sharedPreferences.getBoolean(PREF_STAY_SIGNED_IN, false);

                    if (staySignedInPref) {
                        // User previously chose to stay signed in via Email/Admin login
                        String userType = sharedPreferences.getString(PREF_USER_TYPE, null);
                        String userIdentifier = sharedPreferences.getString(PREF_USER_IDENTIFIER, null);

                        if (USER_TYPE_ADMIN.equals(userType)) {
                            // Go to Admin Dashboard
                            intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                            // intent.putExtra("ADMIN_ID", userIdentifier); // Optional
                        } else if (USER_TYPE_REGULAR.equals(userType) && userIdentifier != null) {
                            // Go to User Dashboard
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.putExtra("USER_EMAIL", userIdentifier); // Pass email
                        } else {
                            // Invalid state in SharedPreferences, clear it and go to default Login
                            clearStaySignedInPreference();
                            // Decide which login screen is the primary entry point
                            intent = new Intent(SplashActivity.this, LoginActivity.class); // Default to OTP Login?
                            // Or maybe: intent = new Intent(SplashActivity.this, EmailLoginActivity.class);
                        }
                    } else {
                        // --- Step 3: No Firebase user AND "Stay Signed In" is false ---
                        // Go to the primary Login screen (assuming LoginActivity for OTP)
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                        // Or maybe: intent = new Intent(SplashActivity.this, EmailLoginActivity.class);
                    }
                }

                // Ensure previous activities are cleared when navigating away from splash
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish(); // Close the splash activity
            }
        }, splashTimeout); // Use the dynamically determined delay
    }

    /**
     * Determines the appropriate splash timeout based on whether the user is returning or new.
     * @return The timeout in milliseconds (500ms for returning users, 1500ms for new users)
     */
    private int determineSplashTimeout() {
        // Check if user is logged in via Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User logged in with Firebase Auth - they're a returning user
            return SPLASH_TIMEOUT_RETURNING_USER;
        }

        // Check if logged in via "Stay Signed In" preference
        boolean staySignedInPref = sharedPreferences.getBoolean(PREF_STAY_SIGNED_IN, false);
        if (staySignedInPref) {
            String userType = sharedPreferences.getString(PREF_USER_TYPE, null);
            String userIdentifier = sharedPreferences.getString(PREF_USER_IDENTIFIER, null);

            // Verify this is a valid saved login
            if ((USER_TYPE_ADMIN.equals(userType) ||
                    (USER_TYPE_REGULAR.equals(userType) && userIdentifier != null))) {
                // Valid saved login - they're a returning user
                return SPLASH_TIMEOUT_RETURNING_USER;
            }
        }

        // No valid saved login found - treat as a new user
        return SPLASH_TIMEOUT_FIRST_LOGIN;
    }

    /**
     * Clears the "Stay Signed In" preferences.
     * Helper method in case preferences are invalid or need clearing.
     */
    private void clearStaySignedInPreference() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_STAY_SIGNED_IN);
        editor.remove(PREF_USER_TYPE);
        editor.remove(PREF_USER_IDENTIFIER);
        editor.apply();
    }
}