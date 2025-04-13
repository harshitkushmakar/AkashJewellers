package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText SignupName, SignupPhoneNumber, SignupEmail, SignupPassword;
    AppCompatButton SignUpButton;

    FirebaseDatabase database;

    DatabaseReference reference;

    ImageView back_to_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        SignupName = findViewById(R.id.name_register_text);
        SignupPhoneNumber = findViewById(R.id.phone_number_register);
        SignupEmail = findViewById(R.id.email_text);
        SignupPassword = findViewById(R.id.password_text);
        SignUpButton = findViewById(R.id.SignUp_button);
        back_to_login = findViewById(R.id.btnBack);

        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference();

                String name = SignupName.getText().toString();
                String phoneNumber = SignupPhoneNumber.getText().toString();
                String email = SignupEmail.getText().toString();
                String password = SignupPassword.getText().toString();

                UserClass userClass = new UserClass(name,phoneNumber,email ,password);
                reference.child("users").push().setValue(userClass);

                Toast.makeText(RegisterActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, EmailLoginActivity.class);
                startActivity(intent);
            }
        });

        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, EmailLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}