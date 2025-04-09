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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText EnterName;
    EditText EnterNumber;
    AppCompatButton GetOtp;
    ProgressBar progressBar;
    ImageView emailimage;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

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

        // Initialize Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
                String name = EnterName.getText().toString().trim();
                String mobileNumber = EnterNumber.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter your Name", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if name is empty
                }

                if (!mobileNumber.isEmpty()) {
                    if (mobileNumber.length() == 10) {
                        progressBar.setVisibility(View.VISIBLE);
                        GetOtp.setVisibility(View.INVISIBLE);

                        // Check if the phone number exists in the database
                        Query query = mDatabase.child("users").orderByChild("phone").equalTo(mobileNumber);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // User with this phone number exists, update the name
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        userSnapshot.getRef().child("name").setValue(name)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(LoginActivity.this, "Name updated.", Toast.LENGTH_SHORT).show();
                                                    initiateOtpVerification(mobileNumber);
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    GetOtp.setVisibility(View.VISIBLE);
                                                    Toast.makeText(LoginActivity.this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                        return; // Exit the loop after updating the first matching user
                                    }
                                } else {
                                    // User with this phone number does not exist, create a new entry
                                    saveNewUserData(name, mobileNumber);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressBar.setVisibility(View.GONE);
                                GetOtp.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Please enter a valid 10-digit number", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    GetOtp.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initiateOtpVerification(String mobileNumber) {
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
                        intent.putExtra("mobile", mobileNumber);
                        intent.putExtra("backendOtp", backendOtp);
                        startActivity(intent);
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);
                    }
                };

        try {
            String phoneNumber = "+91" + mobileNumber;
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
                    "Error initiating OTP: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
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

    private void saveNewUserData(String name, String phone) {
        DatabaseReference usersRef = mDatabase.child("users");
        String userId = usersRef.push().getKey(); // Generate a unique ID for the user

        if (userId != null) {
            usersRef.child(userId).child("name").setValue(name);
            usersRef.child(userId).child("phone").setValue(phone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(LoginActivity.this, "Name and phone number saved.", Toast.LENGTH_SHORT).show();
                        initiateOtpVerification(phone);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Failed to generate user ID.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            GetOtp.setVisibility(View.VISIBLE);
        }
    }
}