package com.example.akashjewller;

import android.content.Intent;
// import android.content.res.Configuration; // Import if using status bar styling
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

// import androidx.activity.EdgeToEdge; // Uncomment if using EdgeToEdge
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Firebase Imports (Removed Database related ones)
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException; // Keep for error checking
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
// Removed: DataSnapshot, DatabaseError, DatabaseReference, FirebaseDatabase, Query, ValueEventListener

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // Tag for logging

    // UI Elements
    EditText EnterName;
    EditText EnterNumber;
    AppCompatButton GetOtp;
    ProgressBar progressBar;
    ImageView emailimage;

    // Firebase Auth
    FirebaseAuth mAuth;
    // Removed: DatabaseReference mDatabase;

    // Variables for Phone Auth
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private String verificationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Enable if using EdgeToEdge
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase App and Auth (Keep these)
        try {
            FirebaseApp.initializeApp(this); // Initialize default FirebaseApp
            Log.d(TAG, "FirebaseApp initialized successfully.");

            FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance());
            Log.d(TAG, "Firebase App Check installed with Play Integrity.");

        } catch (IllegalStateException e) {
            Log.w(TAG, "FirebaseApp already initialized: " + e.getMessage());
            FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance());
        }

        mAuth = FirebaseAuth.getInstance();
        // Removed: mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        EnterName = findViewById(R.id.enter_name);
        EnterNumber = findViewById(R.id.phoneNumber);
        GetOtp = findViewById(R.id.getOtpButton);
        progressBar = findViewById(R.id.progressbarGetOtp);
        emailimage = findViewById(R.id.email_image);


        if (emailimage != null) {
            emailimage.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, EmailLoginActivity.class);
                startActivity(intent);
            });
        } else {
            Log.w(TAG, "ImageView with ID 'email_image' not found in layout.");
        }


        // Get OTP Button Listener
        GetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // We still get the name, but won't save it to DB
                String name = EnterName.getText().toString().trim();
                String mobileNumber = EnterNumber.getText().toString().trim();

                // --- Input Validation (Keep this) ---
                if (name.isEmpty()) {
                    EnterName.setError("Name cannot be empty");
                    EnterName.requestFocus();
                    return;
                } else {
                    EnterName.setError(null);
                }

                if (mobileNumber.isEmpty()) {
                    EnterNumber.setError("Mobile number cannot be empty");
                    EnterNumber.requestFocus();
                    return;
                } else if (mobileNumber.length() != 10) {
                    EnterNumber.setError("Enter a valid 10-digit number");
                    EnterNumber.requestFocus();
                    return;
                } else {
                    EnterNumber.setError(null);
                }

                // --- Proceed directly to OTP verification if inputs are valid ---
                Log.d(TAG, "Input valid, proceeding to OTP verification for " + mobileNumber);
                progressBar.setVisibility(View.VISIBLE);
                GetOtp.setVisibility(View.INVISIBLE);
                initiateOtpVerification(mobileNumber); // Call verification directly
            }
        });
    } // --- End of onCreate ---


    private void initiateOtpVerification(String mobileNumber) {
        // This method remains largely the same as it handles AUTHENTICATION, not database saving
        Log.d(TAG, "Initiating OTP verification for: " + mobileNumber);
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "onVerificationCompleted: Auto-verification/retrieval complete.");
                        // Reset UI potentially
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "onVerificationFailed", e);
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);

                        String errorMessage;
                        if (e instanceof FirebaseAuthException) {
                            FirebaseAuthException authEx = (FirebaseAuthException) e;
                            String errorCode = authEx.getErrorCode();
                            Log.w(TAG, "FirebaseAuthException Error Code: " + errorCode);
                            if ("ERROR_TOO_MANY_REQUESTS".equals(errorCode)) {
                                errorMessage = "SMS limit exceeded, Please try after 1 hr";
                            } else {
                                errorMessage = "Verification failed [" + errorCode + "]: " + e.getLocalizedMessage();
                            }
                        } else {
                            errorMessage = "Verification failed: " + e.getLocalizedMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: Verification ID = " + verificationId);
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);

                        // Store verification ID and resend token (Needed for OtpActivity)
                        LoginActivity.this.verificationId = verificationId;
                        LoginActivity.this.resendToken = token;

                        // Start OtpActivity and pass necessary data
                        Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                        intent.putExtra("mobile", mobileNumber);
                        intent.putExtra("backendOtp", verificationId);
                        intent.putExtra("resendToken", token);
                        startActivity(intent);
                    }
                };

        // Start the phone number verification process
        try {
            String phoneNumberE164 = "+91" + mobileNumber;
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNumberE164)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(mCallbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
            Log.d(TAG, "PhoneAuthProvider.verifyPhoneNumber called for " + phoneNumberE164);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            GetOtp.setVisibility(View.VISIBLE);
            Log.e(TAG, "Error initiating OTP verification", e);
            Toast.makeText(LoginActivity.this,
                    "Error initiating OTP: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // This method remains the same - handles sign-in after verification
        Log.d(TAG, "Attempting to sign in with credential.");
        progressBar.setVisibility(View.VISIBLE);
        GetOtp.setVisibility(View.INVISIBLE);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    GetOtp.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential successful.");
                        Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "signInWithCredential failed", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + Objects.toString(task.getException().getMessage(), "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

} // --- End of LoginActivity Class ---