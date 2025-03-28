package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class EmailLoginActivity extends AppCompatActivity {

    TextView createNewAccount;
    AppCompatButton Login;
    EditText LoginEmail;
    EditText LoginPassword;
    TextView ForgotPassword;

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

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use logical OR (||) instead of bitwise OR (|)
                if (validateEmail() && validatePassword()) {
                    checkUser();
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
        String val = LoginEmail.getText().toString();
        if (val.isEmpty()) {
            LoginEmail.setError("Email cannot be Empty");
            return false;
        } else {
            LoginEmail.setError(null);
            return true;
        }
    }

    public boolean validatePassword() {
        String val = LoginPassword.getText().toString();
        if (val.isEmpty()) {
            LoginPassword.setError("Password cannot be Empty");
            return false;
        } else {
            LoginPassword.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String userEmail = LoginEmail.getText().toString().trim();
        String userPassword = LoginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Query to find users matching the email
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isUserFound = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserClass user = userSnapshot.getValue(UserClass.class);

                    if (user != null && user.getEmail().equals(userEmail)) {
                        isUserFound = true;
                        // Correct password comparison
                        if (user.getPassword().equals(userPassword)) {
                            Toast.makeText(EmailLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EmailLoginActivity.this, UserDashboardActivity.class);
                            startActivity(intent);
                            finish(); // Close the login activity
                            return;
                        } else {
                            LoginPassword.setError("Incorrect Password");
                            LoginPassword.requestFocus();
                            return;
                        }
                    }
                }

                if (!isUserFound) {
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