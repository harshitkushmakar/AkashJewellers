package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText EnterName;
    EditText EnterNumber;
    AppCompatButton GetOtp;
    ProgressBar progressBar;
    TextView emailText;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        emailText = findViewById(R.id.EmailActivity_text);


        emailText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the next activity
                Intent intent = new Intent(LoginActivity.this, EmailLoginActivity.class);
                startActivity(intent);
            }
        });
        GetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!EnterNumber.getText().toString().trim().isEmpty()) {
                    if(EnterNumber.getText().toString().trim().length() == 10) {
                        // Show progress and hide button
                        progressBar.setVisibility(View.VISIBLE);
                        GetOtp.setVisibility(View.INVISIBLE);

                        // Create callbacks
                        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        // This is called when verification is done automatically
                                        progressBar.setVisibility(View.GONE);
                                        GetOtp.setVisibility(View.VISIBLE);
                                        // Typically used for instant verification or auto-retrieval
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        // Reset UI
                                        progressBar.setVisibility(View.GONE);
                                        GetOtp.setVisibility(View.VISIBLE);
                                        // Show error message
                                        Toast.makeText(LoginActivity.this,
                                                "Verification failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String backendOtp,
                                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        super.onCodeSent(backendOtp, forceResendingToken);

                                        // Navigate to OTP activity with the verification ID
                                        Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                                        intent.putExtra("mobile", EnterNumber.getText().toString());
                                        intent.putExtra("backendOtp", backendOtp);
                                        startActivity(intent);

                                        // Reset UI after navigation
                                        progressBar.setVisibility(View.GONE);
                                        GetOtp.setVisibility(View.VISIBLE);
                                    }
                                };

                        // Build auth options
                        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber("+91" + EnterNumber.getText().toString())
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(LoginActivity.this)
                                .setCallbacks(mCallbacks)
                                .build();

                        // Start phone authentication
                        PhoneAuthProvider.verifyPhoneNumber(options);

                    } else {
                        Toast.makeText(LoginActivity.this, "Please enter valid 10-digit number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}