package com.example.akashjewller;

 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import androidx.activity.EdgeToEdge;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.appcompat.widget.AppCompatButton;
 import androidx.core.graphics.Insets;
 import androidx.core.view.ViewCompat;
 import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    EditText entername;
    EditText enternumber;
    AppCompatButton Sendotp;

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

       entername = findViewById(R.id.enter_name);
       enternumber = findViewById(R.id.phoneNumber);
       Sendotp = findViewById(R.id.sendOtpButton);

        Sendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!enternumber.getText().toString().trim().isEmpty())
                {
                    if(enternumber.getText().toString().trim().length() ==10){
                        Intent intent = new Intent(getApplicationContext(),OtpActivity.class);
                        intent.putExtra("mobile", enternumber.getText().toString());
                        startActivity(intent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Please Enter valid Number", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(LoginActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}