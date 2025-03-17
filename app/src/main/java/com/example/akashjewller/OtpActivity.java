package com.example.akashjewller;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OtpActivity extends AppCompatActivity {

    EditText inputNumber1, inputNumber2, inputNumber3, inputNumber4, inputNumber5,inputNumber6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inputNumber1 = findViewById(R.id.otp_number_1);
        inputNumber1 = findViewById(R.id.otp_number_2);
        inputNumber1 = findViewById(R.id.otp_number_3);
        inputNumber1 = findViewById(R.id.otp_number_4);
        inputNumber1 = findViewById(R.id.otp_number_5);
        inputNumber1 = findViewById(R.id.otp_number_6);


        TextView textView = findViewById(R.id.text_mobile_number);

        textView.setText(String.format("+91-%s" , getIntent().getStringExtra("mobile")

        ));
    }
}