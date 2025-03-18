package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OtpActivity extends AppCompatActivity {

    AppCompatButton verifyButtonOnclick;
    EditText inputNumber1, inputNumber2, inputNumber3, inputNumber4, inputNumber5, inputNumber6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inputNumber1 = findViewById(R.id.otp_number_1);
        inputNumber2 = findViewById(R.id.otp_number_2);
        inputNumber3 = findViewById(R.id.otp_number_3);
        inputNumber4 = findViewById(R.id.otp_number_4);
        inputNumber5 = findViewById(R.id.otp_number_5);
        inputNumber6 = findViewById(R.id.otp_number_6);

        verifyButtonOnclick = findViewById(R.id.btnVerify);

        TextView textView = findViewById(R.id.text_mobile_number);
        textView.setText(String.format("+91 | %s" , getIntent().getStringExtra("mobile")
        ));

        verifyButtonOnclick.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!inputNumber1.getText().toString().trim().isEmpty() &&
                    !inputNumber2.getText().toString().trim().isEmpty() &&
                    !inputNumber3.getText().toString().trim().isEmpty() &&
                    !inputNumber4.getText().toString().trim().isEmpty() &&
                    !inputNumber5.getText().toString().trim().isEmpty() &&
                    !inputNumber6.getText().toString().trim().isEmpty()) {


                Toast.makeText(OtpActivity.this, "Otp Verified", Toast.LENGTH_SHORT).show();

                // Create an Intent to go to MainActivity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                // Optionally, you can finish the current activity if you don't want the user to return to OTP screen
                finish();
            }
            else {
                Toast.makeText(OtpActivity.this, "Please Enter valid Otp", Toast.LENGTH_SHORT).show();
            }
        }
    });

    numberOtpMoveForward();
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
}



