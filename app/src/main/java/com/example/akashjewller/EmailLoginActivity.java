package com.example.akashjewller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    // UI Elements
    AppCompatButton createNewAccount;
    AppCompatButton Login;
    EditText LoginEmail;
    EditText LoginPassword;
    TextView ForgotPassword;
    CheckBox showPasswordCheckBox;
    CheckBox staySignedInCheckBox; // Added CheckBox
    ImageView back_to_otp;
    // SharedPreferences for "Stay Signed In"
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_STAY_SIGNED_IN = "staySignedIn";
    private static final String PREF_USER_TYPE = "userType"; // "admin" or "user"
    private static final String PREF_USER_IDENTIFIER = "userIdentifier"; // email or admin identifier
    private SharedPreferences sharedPreferences;
    // Define a constant for the admin login identifier
    private static final String ADMIN_EMAIL_IDENTIFIER = "admin"; // Or "admin@yourapp.com", etc.
    private static final String USER_TYPE_ADMIN = "admin";
    private static final String USER_TYPE_REGULAR = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // --- Check if user should be automatically logged in ---
        if (checkStaySignedInPreference()) {
            // If true, the appropriate dashboard was started, so finish this activity
            return; // Exit onCreate early
        }
        // --- End Auto-Login Check ---
        // Set the layout only if not auto-logging in
        setContentView(R.layout.activity_email_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Find Views
        createNewAccount = findViewById(R.id.New_account);
        Login = findViewById(R.id.login);
        LoginEmail = findViewById(R.id.Login_email);
        ForgotPassword = findViewById(R.id.forgot_password);
        LoginPassword = findViewById(R.id.Login_password_text);
        back_to_otp = findViewById(R.id.btnBack);
        showPasswordCheckBox = findViewById(R.id.checkbox_show_password);
        staySignedInCheckBox = findViewById(R.id.checkbox_stay_signed_in); // Find the new CheckBox

        // Listener for Show Password CheckBox
        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    LoginPassword.setTransformationMethod(null); // Show password
                } else {
                    LoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Hide password
                }
                LoginPassword.setSelection(LoginPassword.getText().length()); // Move cursor to the end
            }
        });

        // Listener for Back Button
        back_to_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to the previous login screen (presumably OTP or main login)
                // Consider using finish() if this is the only way to get here
                Intent intent = new Intent(EmailLoginActivity.this, LoginActivity.class);
                startActivity(intent);
                // finish(); // Optional: finish this activity if needed
            }
        });

        // Listener for Login Button
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate both email and password before proceeding
                if (validateEmail() && validatePassword()) {
                    checkUserOrAdmin(); // Check if admin or regular user
                }
            }
        });

        // Listener for Create New Account Button
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmailLoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    /**
     * Checks SharedPreferences to see if the user previously chose to stay signed in.
     * If so, it retrieves the user type and identifier, starts the appropriate
     * dashboard activity, and finishes the current LoginActivity.
     *
     * @return true if the user was automatically logged in, false otherwise.
     */
    private boolean checkStaySignedInPreference() {
        boolean staySignedIn = sharedPreferences.getBoolean(PREF_STAY_SIGNED_IN, false);
        if (staySignedIn) {
            String userType = sharedPreferences.getString(PREF_USER_TYPE, null);
            String userIdentifier = sharedPreferences.getString(PREF_USER_IDENTIFIER, null);

            if (userType != null && userIdentifier != null) {
                Intent intent;
                if (USER_TYPE_ADMIN.equals(userType)) {
                    // Auto-login as Admin
                    Toast.makeText(this, "Welcome back, Admin!", Toast.LENGTH_SHORT).show();
                    intent = new Intent(EmailLoginActivity.this, AdminDashboardActivity.class);
                    // You might want to pass the identifier if needed in the dashboard
                    // intent.putExtra("ADMIN_ID", userIdentifier);
                } else if (USER_TYPE_REGULAR.equals(userType)) {
                    // Auto-login as Regular User
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                    // Pass the user's email to the dashboard
                    intent.putExtra("USER_EMAIL", userIdentifier);
                } else {
                    // Invalid user type stored, clear prefs and proceed to manual login
                    clearStaySignedInPreference();
                    return false;
                }

                startActivity(intent);
                finish(); // Finish LoginActivity so user can't go back to it
                return true; // Indicate that auto-login occurred
            } else {
                // Incomplete preferences, clear them
                clearStaySignedInPreference();
            }
        }
        return false; // Did not auto-login
    }
    /**
     * Saves the user's preference to stay signed in.
     *
     * @param userType       The type of user ("admin" or "user").
     * @param userIdentifier The user's email or admin identifier.
     */
    private void saveStaySignedInPreference(String userType, String userIdentifier) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_STAY_SIGNED_IN, true);
        editor.putString(PREF_USER_TYPE, userType);
        editor.putString(PREF_USER_IDENTIFIER, userIdentifier);
        editor.apply(); // Use apply() for asynchronous saving
    }
    /**
     * Clears the "Stay Signed In" preferences.
     * This should be called on manual logout or if preferences are invalid.
     */
    private void clearStaySignedInPreference() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_STAY_SIGNED_IN);
        editor.remove(PREF_USER_TYPE);
        editor.remove(PREF_USER_IDENTIFIER);
        editor.apply();
    }
    // --- Validation Methods ---
    public boolean validateEmail() {
        String val = LoginEmail.getText().toString().trim();
        if (val.isEmpty()) {
            LoginEmail.setError("Email cannot be Empty");
            return false;
        } else {
            LoginEmail.setError(null); // Clear error if valid
            return true;
        }
    }
    public boolean validatePassword() {
        String val = LoginPassword.getText().toString().trim();
        if (val.isEmpty()) {
            LoginPassword.setError("Password cannot be Empty");
            return false;
        } else {
            LoginPassword.setError(null); // Clear error if valid
            return true;
        }
    }
    // --- Login Logic ---
    public void checkUserOrAdmin() {
        String enteredEmail = LoginEmail.getText().toString().trim();
        String enteredPassword = LoginPassword.getText().toString().trim();

        // Check if the entered email matches the admin identifier (case-insensitive)
        if (enteredEmail.equalsIgnoreCase(ADMIN_EMAIL_IDENTIFIER)) {
            checkAdminCredentials(enteredPassword); // Attempt Admin Login
        } else {
            checkRegularUser(enteredEmail, enteredPassword); // Attempt Regular User Login
        }
    }
    private void checkAdminCredentials(String enteredPassword) {
        // Path to admin password in Firebase
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admin_credentials").child("password");

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedAdminPassword = snapshot.getValue(String.class);
                    if (storedAdminPassword != null && storedAdminPassword.equals(enteredPassword)) {
                        // Admin Password Correct
                        Toast.makeText(EmailLoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();

                        // --- Handle "Stay Signed In" ---
                        if (staySignedInCheckBox.isChecked()) {
                            saveStaySignedInPreference(USER_TYPE_ADMIN, ADMIN_EMAIL_IDENTIFIER);
                        } else {
                            clearStaySignedInPreference(); // Clear if unchecked
                        }
                        // --------------------------------

                        // Navigate to Admin Dashboard
                        Intent intent = new Intent(EmailLoginActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                        finish(); // Close the login activity
                    } else {
                        // Incorrect Admin Password
                        LoginPassword.setError("Incorrect Admin Password");
                        LoginPassword.requestFocus();
                        Toast.makeText(EmailLoginActivity.this, "Admin Login Failed", Toast.LENGTH_SHORT).show();
                        clearStaySignedInPreference(); // Clear prefs on failed login attempt
                    }
                } else {
                    // Admin password node doesn't exist
                    Toast.makeText(EmailLoginActivity.this, "Admin configuration error.", Toast.LENGTH_SHORT).show();
                    LoginPassword.setError("Admin login unavailable");
                    clearStaySignedInPreference(); // Clear prefs if admin login is broken
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmailLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                clearStaySignedInPreference(); // Clear prefs on error
            }
        });
    }
    private void checkRegularUser(String userEmail, String userPassword) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Query users by email
        reference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean loggedIn = false; // Flag to check if login was successful
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        UserClass user = userSnapshot.getValue(UserClass.class);
                        if (user != null && user.getEmail().equals(userEmail)) {
                            // Found the user, now check password
                            if (user.getPassword().equals(userPassword)) {
                                // Password Correct - Login Successful
                                Toast.makeText(EmailLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                loggedIn = true;

                                // --- Handle "Stay Signed In" ---
                                if (staySignedInCheckBox.isChecked()) {
                                    saveStaySignedInPreference(USER_TYPE_REGULAR, user.getEmail());
                                } else {
                                    clearStaySignedInPreference(); // Clear if unchecked
                                }
                                // --------------------------------

                                // Navigate to User Dashboard
                                Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                                intent.putExtra("USER_EMAIL", user.getEmail()); // Pass email to dashboard
                                startActivity(intent);
                                finish(); // Close the login activity
                                return; // Exit after successful login
                            } else {
                                // Incorrect Password
                                LoginPassword.setError("Incorrect Password");
                                LoginPassword.requestFocus();
                                loggedIn = false; // Explicitly set false, although loop might continue
                                // Don't return here, maybe another user has same email (bad practice, but handle)
                            }
                        }
                    }
                    // If loop finishes and login wasn't successful (e.g., only wrong passwords found)
                    if (!loggedIn) {
                        // If we reached here, it means email was found but password was wrong for all matches
                        // (or user object was null, which shouldn't happen with the query)
                        if (LoginPassword.getError() == null) { // Avoid overwriting specific password error
                            LoginPassword.setError("Incorrect Password");
                            LoginPassword.requestFocus();
                        }
                        Toast.makeText(EmailLoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        clearStaySignedInPreference(); // Clear prefs on failed login
                    }

                } else {
                    // No user found with this email
                    LoginEmail.setError("User not found");
                    LoginEmail.requestFocus();
                    clearStaySignedInPreference(); // Clear prefs on failed login
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmailLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                clearStaySignedInPreference(); // Clear prefs on error
            }
        });
    }
    // This method would typically be called from a button in the Dashboard activities
    public static void performLogout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_STAY_SIGNED_IN);
        editor.remove(PREF_USER_TYPE);
        editor.remove(PREF_USER_IDENTIFIER);
        editor.apply();

        // Redirect to Login screen
        Intent intent = new Intent(context, EmailLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        context.startActivity(intent);
        // If called from an Activity, you might want to finish() it after starting the login intent.
        // if (context instanceof Activity) {
        //     ((Activity) context).finish();
        // }
    }
}

