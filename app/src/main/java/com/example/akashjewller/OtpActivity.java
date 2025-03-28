package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    AppCompatButton verifyButtonOnclick;
    EditText inputNumber1, inputNumber2, inputNumber3, inputNumber4, inputNumber5, inputNumber6;
    String getOtpBackend;
    ProgressBar progressBarVerify;
    TextView resendOtpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize input fields
        inputNumber1 = findViewById(R.id.otp_number_1);
        inputNumber2 = findViewById(R.id.otp_number_2);
        inputNumber3 = findViewById(R.id.otp_number_3);
        inputNumber4 = findViewById(R.id.otp_number_4);
        inputNumber5 = findViewById(R.id.otp_number_5);
        inputNumber6 = findViewById(R.id.otp_number_6);

        // Initialize other UI elements
        verifyButtonOnclick = findViewById(R.id.btnVerify);
        progressBarVerify = findViewById(R.id.progressbar_verify);
        resendOtpText = findViewById(R.id.resend_otp);

        // Get data from intent
        getOtpBackend = getIntent().getStringExtra("backendOtp");
        String mobileNumber = getIntent().getStringExtra("mobile");

        // Set mobile number text
        TextView textView = findViewById(R.id.text_mobile_number);
        textView.setText(String.format("+91 | %s", mobileNumber));

        // Verify button click listener
        verifyButtonOnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputNumber1.getText().toString().trim().isEmpty() &&
                        !inputNumber2.getText().toString().trim().isEmpty() &&
                        !inputNumber3.getText().toString().trim().isEmpty() &&
                        !inputNumber4.getText().toString().trim().isEmpty() &&
                        !inputNumber5.getText().toString().trim().isEmpty() &&
                        !inputNumber6.getText().toString().trim().isEmpty()) {

                    String enterCodeOtp = inputNumber1.getText().toString() +
                            inputNumber2.getText().toString() +
                            inputNumber3.getText().toString() +
                            inputNumber4.getText().toString() +
                            inputNumber5.getText().toString() +
                            inputNumber6.getText().toString();

                    if(getOtpBackend != null) {
                        progressBarVerify.setVisibility(View.VISIBLE);
                        verifyButtonOnclick.setVisibility(View.INVISIBLE);

                        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(getOtpBackend, enterCodeOtp);

                        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBarVerify.setVisibility(View.GONE);
                                verifyButtonOnclick.setVisibility(View.VISIBLE);

                                if(task.isSuccessful()) {
                                    Toast.makeText(OtpActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), UserDashboardActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(OtpActivity.this, "Enter correct OTP", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(OtpActivity.this, "Please check internet", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OtpActivity.this, "Please Enter valid OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up OTP input field navigation
        numberOtpMoveForward();
        numberOtpMoveBack();

        // Resend OTP click listener
        resendOtpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Auto-verification completed
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OtpActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newBackendOtp,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(newBackendOtp, forceResendingToken);

                        Toast.makeText(OtpActivity.this, "OTP sent again", Toast.LENGTH_SHORT).show();
                        getOtpBackend = newBackendOtp;
                    }
                };

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber("+91" + mobileNumber)  // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)    // Timeout duration
                        .setActivity(OtpActivity.this)        // Activity for callbacks
                        .setCallbacks(callbacks)
                        .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
    }

    private void numberOtpMoveForward() {
        inputNumber1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().trim().isEmpty()) {
                    inputNumber2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        inputNumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().trim().isEmpty()) {
                    inputNumber3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        inputNumber3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().trim().isEmpty()) {
                    inputNumber4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        inputNumber4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().trim().isEmpty()) {
                    inputNumber5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        inputNumber5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!charSequence.toString().trim().isEmpty()) {
                    inputNumber6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void numberOtpMoveBack() {
        inputNumber2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputNumber2.getText().toString().isEmpty()) {
                    inputNumber1.requestFocus();
                    return true;
                }
                return false;
            }
        });

        inputNumber3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputNumber3.getText().toString().isEmpty()) {
                    inputNumber2.requestFocus();
                    return true;
                }
                return false;
            }
        });

        inputNumber4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputNumber4.getText().toString().isEmpty()) {
                    inputNumber3.requestFocus();
                    return true;
                }
                return false;
            }
        });

        inputNumber5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputNumber5.getText().toString().isEmpty()) {
                    inputNumber4.requestFocus();
                    return true;
                }
                return false;
            }
        });

        inputNumber6.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && inputNumber6.getText().toString().isEmpty()) {
                    inputNumber5.requestFocus();
                    return true;
                }
                return false;
            }
        });
    }
}