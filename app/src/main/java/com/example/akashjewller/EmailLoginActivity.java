package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmailLoginActivity extends AppCompatActivity {

    AppCompatButton createNewAccount;
    AppCompatButton Login;
    EditText LoginEmail;
    EditText LoginPassword;
    TextView ForgotPassword;
    CheckBox showPasswordCheckBox;
    ImageView back_to_otp;




    // Define a constant for the admin login identifier (use a specific email or username)
    private static final String ADMIN_EMAIL_IDENTIFIER = "admin"; // Or "admin@yourapp.com", etc.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNewAccount = findViewById(R.id.New_account);
        Login = findViewById(R.id.login);
        LoginEmail = findViewById(R.id.Login_email);
        ForgotPassword = findViewById(R.id.forgot_password);
        LoginPassword = findViewById(R.id.Login_password_text);
        back_to_otp = findViewById(R.id.btnBack);
        showPasswordCheckBox = findViewById(R.id.checkbox_show_password);


        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Show password
                    LoginPassword.setTransformationMethod(null);
                } else {
                    // Hide password
                    LoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                // Move cursor to the end
                LoginPassword.setSelection(LoginPassword.getText().length());
            }
        });

        back_to_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmailLoginActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use logical AND (&&) - both must be valid
                if (validateEmail() && validatePassword()) {
                    checkUserOrAdmin(); // Renamed the method for clarity
                }
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmailLoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean validateEmail() {
        String val = LoginEmail.getText().toString().trim(); // Trim whitespace
        if (val.isEmpty()) {
            LoginEmail.setError("Email cannot be Empty");
            return false;
        } else {
            LoginEmail.setError(null);
            return true;
        }
    }

    public boolean validatePassword() {
        String val = LoginPassword.getText().toString().trim(); // Trim whitespace
        if (val.isEmpty()) {
            LoginPassword.setError("Password cannot be Empty");
            return false;
        } else {
            LoginPassword.setError(null);
            return true;
        }
    }

    public void checkUserOrAdmin() {
        String enteredEmail = LoginEmail.getText().toString().trim();
        String enteredPassword = LoginPassword.getText().toString().trim();

        // Check if the entered email matches the admin identifier
        if (enteredEmail.equalsIgnoreCase(ADMIN_EMAIL_IDENTIFIER)) {
            // Attempt Admin Login
            checkAdminCredentials(enteredPassword);
        } else {
            // Attempt Regular User Login
            checkRegularUser(enteredEmail, enteredPassword);
        }
    }

    private void checkAdminCredentials(String enteredPassword) {
        // Path to your admin password in Firebase Realtime Database
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admin_credentials").child("password");

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedAdminPassword = snapshot.getValue(String.class);
                    if (storedAdminPassword != null && storedAdminPassword.equals(enteredPassword)) {
                        // Admin Password Correct
                        Toast.makeText(EmailLoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                        // --- IMPORTANT: Replace with your actual Admin Dashboard Activity ---
                        Intent intent = new Intent(EmailLoginActivity.this, AdminDashboardActivity.class);
                        // --------------------------------------------------------------------
                        startActivity(intent);
                        finish(); // Close the login activity
                    } else {
                        // Incorrect Admin Password
                        LoginPassword.setError("Incorrect Admin Password");
                        LoginPassword.requestFocus();
                        Toast.makeText(EmailLoginActivity.this, "Admin Login Failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Admin password node doesn't exist in DB
                    Toast.makeText(EmailLoginActivity.this, "Admin configuration error.", Toast.LENGTH_SHORT).show();
                    LoginPassword.setError("Admin login unavailable"); // Inform user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmailLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // --- This is your original user checking logic, now in its own method ---
    private void checkRegularUser(String userEmail, String userPassword) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Query to find users matching the email
        // Using orderByChild("email").equalTo(userEmail) is more efficient if you have many users
        reference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Since email should be unique, we expect at most one result.
                    // Iterate just in case, but typically the first child is the one.
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        UserClass user = userSnapshot.getValue(UserClass.class);

                        // Double check user and email just to be safe, though query should ensure it
                        if (user != null && user.getEmail().equals(userEmail)) {
                            // Correct password comparison
                            // Exit after finding user but wrong password
                            if (user.getPassword().equals(userPassword)) {
                                Toast.makeText(EmailLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                // --- IMPORTANT: Replace with your actual User Dashboard Activity ---
                                Intent intent = new Intent(EmailLoginActivity.this, UserDashboardActivity.class);
                                // -----------------------------------------------------------------
                                startActivity(intent);
                                finish(); // Close the login activity
                            } else {
                                LoginPassword.setError("Incorrect Password");
                                LoginPassword.requestFocus();
                            }
                            return; // Exit after successful login
                        }
                    }
                    // If loop finishes without finding a matching user object (shouldn't happen with query)
                    LoginEmail.setError("Error retrieving user data.");

                } else {
                    // No user found with this email
                    LoginEmail.setError("User not found");
                    LoginEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmailLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}