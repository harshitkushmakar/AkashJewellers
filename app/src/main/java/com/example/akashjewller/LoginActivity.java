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
// Removed: DataSnapshot, DatabaseError, DatabaseReference, FirebaseDatabase, Query, ValueEventListener

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity"; // Tag for logging
    EditText EnterName;
    EditText EnterNumber;
    AppCompatButton GetOtp;
    ProgressBar progressBar;
    ImageView emailimage;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    ImageView googleimage;

    // --- Variable to store the resend token ---
    PhoneAuthProvider.ForceResendingToken resendToken;
    // --- Variable to store the verification ID (needed if auto-verify happens here) ---
    String verificationId;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

// EdgeToEdge.enable(this); // Consider if EdgeToEdge is causing issues, test without if needed.

        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;

        });

        // Set status bar icon color based on theme
// This part looks fine, but ensure it works as expected across devices/versions.
// Consider using Material Components themes for simpler status bar handling.
// Initialize Firebase (ensure this is done only once, usually in Application class)
// If you have an Application class, move initialization there.
// FirebaseApp.initializeApp(this); // Can sometimes cause issues if called multiple times
// Initialize App Check
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(

                PlayIntegrityAppCheckProviderFactory.getInstance());

        // Initialize Firebase Auth

        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Initialize Views
        EnterName = findViewById(R.id.enter_name);

        EnterNumber = findViewById(R.id.phoneNumber);

        GetOtp = findViewById(R.id.getOtpButton);

        progressBar = findViewById(R.id.progressbarGetOtp);

        emailimage = findViewById(R.id.email_btn);

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
                    EnterName.setError("Name cannot be empty"); // Provide feedback
                    EnterName.requestFocus();
                    // Toast.makeText(LoginActivity.this, "Please enter your Name", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution

                } else {

                    EnterName.setError(null); // Clear error if previously set

                }

                if (mobileNumber.isEmpty()) {

                    EnterNumber.setError("Mobile number cannot be empty");

                    EnterNumber.requestFocus();

             // Toast.makeText(LoginActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();

                    return; // Stop further execution

                } else if (mobileNumber.length() != 10) {

                    EnterNumber.setError("Enter a valid 10-digit number");

                    EnterNumber.requestFocus();
                    // Toast.makeText(LoginActivity.this, "Please enter a valid 10-digit number", Toast.LENGTH_SHORT).show();

                    return; // Stop further execution

                } else {

                    EnterNumber.setError(null); // Clear error if previously set

                }


// If inputs are valid, proceed

                progressBar.setVisibility(View.VISIBLE);

                GetOtp.setVisibility(View.INVISIBLE);

                // Check if the phone number exists in the database

               // Using "Phone" node as per your saveNewUserData method

                Query query = mDatabase.child("Phone").orderByChild("phone").equalTo(mobileNumber);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override

                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Log.d(TAG, "Phone number exists, updating name.");

                            // User with this phone number exists, update the name

                            boolean updated = false;

                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                                userSnapshot.getRef().child("name").setValue(name)

                                        .addOnSuccessListener(aVoid -> {

                                            Log.d(TAG, "Name updated successfully for existing user.");

                                          // Toast.makeText(LoginActivity.this, "Name updated.", Toast.LENGTH_SHORT).show();
                                            initiateOtpVerification(mobileNumber); // Proceed to OTP verification

                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            GetOtp.setVisibility(View.VISIBLE);
                                            Log.e(TAG, "Failed to update name", e);
                                            Toast.makeText(LoginActivity.this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        });

                                updated = true;
                                break; // Exit the loop after finding and attempting update on the first match

                            }
                            if (!updated) { // Should not happen if dataSnapshot.exists() is true, but good practice

                                Log.w(TAG, "DataSnapshot existed but no child found to update.");

                                saveNewUserData(name, mobileNumber); // Fallback to save if update failed unexpectedly

                            }

                        } else {

                            Log.d(TAG, "Phone number does not exist, creating new user.");

// User with this phone number does not exist, create a new entry

                            saveNewUserData(name, mobileNumber);

                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Database error checking phone number", databaseError.toException());
                        Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    } // --- End of onCreate ---

    private void initiateOtpVerification(String mobileNumber) {

        Log.d(TAG, "Initiating OTP verification for: " + mobileNumber);

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =

                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override

                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // This callback is triggered in two situations:
                        // 1. Instant verification without needing code (rare on non-Google Play devices)
                        // 2. Auto-retrieval of the SMS code on Google Play services devices

                        Log.d(TAG, "onVerificationCompleted: Auto-verification or auto-retrieval complete.");

                        progressBar.setVisibility(View.GONE);

                        GetOtp.setVisibility(View.VISIBLE);

                      // Store verification ID if needed, although credential is provided
                        // verificationId = credential.getSmsCode(); // This might be null depending on flow

                        signInWithPhoneAuthCredential(credential); // Sign in directly

                    }
                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "onVerificationFailed", e);

                        progressBar.setVisibility(View.GONE);

                        GetOtp.setVisibility(View.VISIBLE);

                        Toast.makeText(LoginActivity.this,

                                "Verification failed: " + e.getMessage(),

                                Toast.LENGTH_LONG).show();

                            // Log common errors: e.g., quota exceeded, invalid phone number format, network error
                        // Consider showing more specific error messages based on e.getErrorCode()

                    }
                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {

                        // SMS sent successfully, user needs to enter the code
                        Log.d(TAG, "onCodeSent: Verification ID = " + verificationId); // Log the ID

                        progressBar.setVisibility(View.GONE);

                        GetOtp.setVisibility(View.VISIBLE);

                        // --- STORE VERIFICATION ID AND RESEND TOKEN ---
                        LoginActivity.this.verificationId = verificationId;
                        LoginActivity.this.resendToken = token; // Store the token
                        // --- START OTP ACTIVITY AND PASS DATA ---
                        Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                        intent.putExtra("mobile", mobileNumber);
                        intent.putExtra("backendOtp", verificationId); // Pass the verification ID
                        intent.putExtra("resendToken", token); // <-- PASS THE TOKEN
                        startActivity(intent);
                    }
        };
        // Start the phone number verification process
        try {

            String phoneNumberE164 = "+91" + mobileNumber; // Ensure E.164 format

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)

                    .setPhoneNumber(phoneNumberE164) // Phone number to verify

                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit

                    .setActivity(this) // Activity (for callback binding)

                    .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks

                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

            Log.d(TAG, "PhoneAuthProvider.verifyPhoneNumber called for " + phoneNumberE164);

        } catch (Exception e) {

// Catch potential exceptions during option building or verification start

            progressBar.setVisibility(View.GONE);

            GetOtp.setVisibility(View.VISIBLE);

//            Log.e(TAG, "Error initiating OTP verification", e);

            Toast.makeText(LoginActivity.this,

                    "Too Many Attempt! try After sometime " ,

                    Toast.LENGTH_LONG).show();

        }

    }

// Method to initialize reCAPTCHA / App Check configuration (called it in onCreate)

// Note: initializeRecaptchaConfig name might be outdated if only using Play Integrity
    private void initializeRecaptchaConfig() {

// This is actually initializing App Check with Play Integrity,

// which is the modern approach and often replaces SafetyNet/reCAPTCHA for phone auth.

// The method name could be updated for clarity, e.g., initializeAppCheck()

        Log.d(TAG, "Initializing App Check with Play Integrity.");


// FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance(); // Already done in onCreate

// firebaseAppCheck.installAppCheckProviderFactory(

// PlayIntegrityAppCheckProviderFactory.getInstance());

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "Attempting to sign in with credential.");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential successful.");

                  // Sign in success, update UI with the signed-in user's information
                        // FirebaseUser user = task.getResult().getUser(); // Get user info if needed

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);

                        finish(); // Finish LoginActivity

                    } else {

// Sign in failed, display a message and potentially log the error

                        Log.w(TAG, "signInWithCredential failed", task.getException());

                        Toast.makeText(LoginActivity.this,

                                "Authentication failed: " + Objects.toString(task.getException().getMessage(), "Unknown error"), // Handle null exception message

                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveNewUserData(String name, String phone) {
        // Saving under "Phone/{uniqueId}" node
        DatabaseReference usersRef = mDatabase.child("Phone");
        String userId = usersRef.push().getKey(); // Generate a unique random ID
        long timestampMillis = System.currentTimeMillis(); // Raw timestamp
        String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(timestampMillis)); // Formatted date/time

        if (userId != null) {
            Log.d(TAG, "Saving new user data for ID: " + userId);
            DatabaseReference newUserRef = usersRef.child(userId); // Reference to the new user node

            // 1. Create a Map to hold all user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("phone", phone);
            userData.put("createdTimestamp", timestampMillis); // <-- Add timestamp here
            userData.put("createdReadable", readableTime);

            // 2. Save the entire map in one operation
            newUserRef.setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "New user data saved successfully (including timestamp).");
                        // Toast.makeText(LoginActivity.this, "User data saved.", Toast.LENGTH_SHORT).show();
                        initiateOtpVerification(phone); // Proceed to OTP verification
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        GetOtp.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Failed to save new user data", e);
                        Toast.makeText(LoginActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            // Handle the unlikely case where push().getKey() returns null
            Toast.makeText(LoginActivity.this, "Failed to generate user ID.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Firebase push().getKey() returned null for new user.");
            progressBar.setVisibility(View.GONE);
            GetOtp.setVisibility(View.VISIBLE);
        }
    }

}