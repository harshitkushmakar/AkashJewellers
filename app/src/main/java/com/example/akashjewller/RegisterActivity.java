package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    EditText SignupName, SignupPhoneNumber, SignupEmail, SignupPassword;
    AppCompatButton SignUpButton;
    ImageView back_to_login;
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        SignupName = findViewById(R.id.name_register_text);
        SignupPhoneNumber = findViewById(R.id.phone_number_register);
        SignupEmail = findViewById(R.id.email_text);
        SignupPassword = findViewById(R.id.password_text);
        SignUpButton = findViewById(R.id.SignUp_button);
        back_to_login = findViewById(R.id.btnBack);

        // Hide progress bar initially
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Initialize Firebase Database reference once
        database = FirebaseDatabase.getInstance();

        // --- Sign Up Button Click Listener with Validation ---
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get text from fields
                String name = SignupName.getText().toString().trim();
                String phoneNumber = SignupPhoneNumber.getText().toString().trim();
                String email = SignupEmail.getText().toString().trim();
                String password = SignupPassword.getText().toString().trim();

                // Validate input
                if (!validateInput(name, phoneNumber, email, password)) {
                    return;
                }

                // Show progress indicator
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                // Check if email already exists before proceeding
                checkEmailExists(email, new OnEmailCheckListener() {
                    @Override
                    public void onEmailChecked(boolean isAvailable) {
                        // Hide progress indicator
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (isAvailable) {
                            // Email is available, proceed with registration
                            registerUser(name, phoneNumber, email, password);
                        } else {
                            // Email already exists, show error
                            SignupEmail.setError("Email already registered");
                            SignupEmail.requestFocus();
//                            Toast.makeText(RegisterActivity.this,
//                                    "This email is already registered",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // --- Back Button Click Listener ---
        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to EmailLoginActivity
                Intent intent = new Intent(RegisterActivity.this, EmailLoginActivity.class);
                startActivity(intent);
                finish(); // Finish current activity
            }
        });
    }

    /**
     * Validates all input fields.
     * Sets errors on EditText fields if validation fails.
     *
     * @param name        Name input
     * @param phoneNumber Phone number input
     * @param email       Email input
     * @param password    Password input
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateInput(String name, String phoneNumber, String email, String password) {
        // 1. Validate Name
        if (TextUtils.isEmpty(name)) {
            SignupName.setError("Name cannot be empty");
            SignupName.requestFocus();
            return false;
        } else {
            SignupName.setError(null); // Clear error
        }

        // 2. Validate Phone Number
        if (TextUtils.isEmpty(phoneNumber)) {
            SignupPhoneNumber.setError("Phone number cannot be empty");
            SignupPhoneNumber.requestFocus();
            return false;
        } else if (phoneNumber.length() != 10) {
            SignupPhoneNumber.setError("Phone number must be 10 digits");
            SignupPhoneNumber.requestFocus();
            return false;
        } else if (!TextUtils.isDigitsOnly(phoneNumber)) { // Check if it contains only numbers
            SignupPhoneNumber.setError("Phone number must contain only digits");
            SignupPhoneNumber.requestFocus();
            return false;
        } else {
            SignupPhoneNumber.setError(null); // Clear error
        }

        // 3. Validate Email
        if (TextUtils.isEmpty(email)) {
            SignupEmail.setError("Email cannot be empty");
            SignupEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Use Android's built-in email pattern matcher
            SignupEmail.setError("Enter a valid email address");
            SignupEmail.requestFocus();
            return false;
        } else {
            SignupEmail.setError(null); // Clear error
        }

        // 4. Validate Password
        if (TextUtils.isEmpty(password)) {
            SignupPassword.setError("Password cannot be empty");
            SignupPassword.requestFocus();
            return false;
        } else if (password.length() < 6) {
            SignupPassword.setError("Password must be at least 6 characters");
            SignupPassword.requestFocus();
            return false;
        } else {
            SignupPassword.setError(null); // Clear error
        }

        // All validations passed
        return true;
    }

    /**
     * Checks if an email already exists in the database.
     *
     * @param email Email to check
     * @param listener Callback to handle the result
     */
    private void checkEmailExists(String email, OnEmailCheckListener listener) {
        DatabaseReference usersRef = database.getReference("users");

        // Query the database for any record with the given email
        usersRef.orderByChild("email").equalTo(email).limitToFirst(1)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // If data exists, email is already registered
                        boolean emailExists = task.getResult().exists();
                        listener.onEmailChecked(!emailExists);
                    } else {
                        // Handle error
                        Log.e(TAG, "Error checking email existence", task.getException());
//                        Toast.makeText(RegisterActivity.this,
//                                "Cannot verify email uniqueness: " + task.getException().getMessage(),
//                                Toast.LENGTH_SHORT).show();
                        listener.onEmailChecked(false);
                    }
                });
    }

    /**
     * Interface to handle the async result of email check
     */
    interface OnEmailCheckListener {
        void onEmailChecked(boolean isAvailable);
    }

    /**
     * Registers a new user in the database
     *
     * @param name User's name
     * @param phoneNumber User's phone number
     * @param email User's email
     * @param password User's password
     */
    private void registerUser(String name, String phoneNumber, String email, String password) {
        // Get database reference
        reference = database.getReference("users");

        // Create user object
        UserClass userClass = new UserClass(name, phoneNumber, email, password);

        // Use push() to generate a unique key for each new user
        reference.push().setValue(userClass)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User registration successful for: " + email);
                    Toast.makeText(RegisterActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, EmailLoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to write user data to Firebase", e);
                    Toast.makeText(RegisterActivity.this, "Signup Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}