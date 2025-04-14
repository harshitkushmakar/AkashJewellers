package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils for isDigitsOnly
import android.util.Log;
import android.util.Patterns; // Import Patterns for email validation
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
// import android.widget.TextView; // TextView not used directly here
import android.widget.Toast;

// import androidx.activity.EdgeToEdge; // Uncomment if using EdgeToEdge
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity"; // For logging

    EditText SignupName, SignupPhoneNumber, SignupEmail, SignupPassword;
    AppCompatButton SignUpButton;
    ImageView back_to_login;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Enable if using EdgeToEdge
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

        // Initialize Firebase Database reference once
        database = FirebaseDatabase.getInstance();
        // You might want to point to a specific node like "users" here
        // reference = database.getReference("users"); // Initialize reference here

        // --- Sign Up Button Click Listener with Validation ---
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get text from fields
                String name = SignupName.getText().toString().trim();
                String phoneNumber = SignupPhoneNumber.getText().toString().trim();
                String email = SignupEmail.getText().toString().trim();
                String password = SignupPassword.getText().toString().trim(); // Don't trim password usually

                // --- Start Validation ---
                if (!validateInput(name, phoneNumber, email, password)) {
                    // If validation fails, stop here. Errors are set within validateInput.
                    return;
                }
                // --- Validation Passed ---

                // Get database reference (can be initialized here or in onCreate)
                reference = database.getReference("users"); // Reference to the "users" node

                // Create user object and save to Firebase
                UserClass userClass = new UserClass(name, phoneNumber, email, password);

                // Use push() to generate a unique key for each new user
                reference.push().setValue(userClass)
                        .addOnSuccessListener(aVoid -> {
                            // Data successfully written
                            Log.d(TAG, "User registration successful for: " + email);
                            Toast.makeText(RegisterActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, EmailLoginActivity.class);
                            // Optional: Clear task stack if you don't want user going back to Register
                            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Close registration activity
                        })
                        .addOnFailureListener(e -> {
                            // Failed to write data
                            Log.e(TAG, "Failed to write user data to Firebase", e);
                            Toast.makeText(RegisterActivity.this, "Signup Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // --- Back Button Click Listener ---
        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to EmailLoginActivity
                // Consider using finish() instead if EmailLoginActivity is always the previous screen
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
}