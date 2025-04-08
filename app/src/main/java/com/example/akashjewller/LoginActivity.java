package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText EnterName;
    EditText EnterNumber;
    AppCompatButton GetOtp;
    ProgressBar progressBar;
    ImageView emailimage;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase properly
        FirebaseApp.initializeApp(this);

        // Initialize App Check for proper app verification
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        // Initialize Firebase Auth after Firebase initialization
        mAuth = FirebaseAuth.getInstance();

        // Initialize reCAPTCHA configuration
        initializeRecaptchaConfig();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EnterName = findViewById(R.id.enter_name);
        EnterNumber = findViewById(R.id.phoneNumber);
        GetOtp = findViewById(R.id.getOtpButton);
        progressBar = findViewById(R.id.progressbarGetOtp);
        emailimage = findViewById(R.id.email_image);

        emailimage.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, EmailLoginActivity.class);
            startActivity(intent);
        });

        GetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!EnterNumber.getText().toString().trim().isEmpty()) {
                    if (EnterNumber.getText().toString().trim().length() == 10) {
                        progressBar.setVisibility(View.VISIBLE);
                        GetOtp.setVisibility(View.INVISIBLE);

                        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        progressBar.setVisibility(View.GONE);
                                        GetOtp.setVisibility(View.VISIBLE);
                                        signInWithPhoneAuthCredential(phoneAuthCredential);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        progressBar.setVisibility(View.GONE);
                                        GetOtp.setVisibility(View.VISIBLE);
                                        Toast.makeText(LoginActivity.this,
                                                "Verification failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String backendOtp,
                                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        super.onCodeSent(backendOtp, forceResendingToken);
                                        Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                                        intent.putExtra("mobile", EnterNumber.getText().toString());
                                        intent.putExtra("backendOtp", backendOtp);
                                        startActivity(intent);
                                        progressBar.setVisibility(View.GONE);
                                        GetOtp.setVisibility(View.VISIBLE);
                                    }
                                };

                        try {
                            String phoneNumber = "+91" + EnterNumber.getText().toString().trim();
                            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(LoginActivity.this)
                                    .setCallbacks(mCallbacks)
                                    .build();

                            PhoneAuthProvider.verifyPhoneNumber(options);
                        } catch (Exception e) {
                            progressBar.setVisibility(View.GONE);
                            GetOtp.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Please enter a valid 10-digit number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to initialize reCAPTCHA configuration
    private void initializeRecaptchaConfig() {
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
