package com.example.akashjewller;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ContactActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    ImageView phone_btn;

    ImageView whatsapp_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        phone_btn = findViewById(R.id.btn_phone);
        whatsapp_btn =findViewById(R.id.btn_whatsapp);

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();

        whatsapp_btn.setOnClickListener(v -> {
            String phoneNumber = "9162766127"; // First number
            String whatsappUrl = "https://wa.me/" + phoneNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl));
            startActivity(intent);
        });

        // Set up phone number click for dialing
        phone_btn.setOnClickListener(v -> {
            String phoneNumber = "8579909138"; // Second number
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }



    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.Home_menu) {
                    navigateToUserDashboardActivity();
                    return true;
                } else if (itemId == R.id.Contact_menu) {
                    navigateToContactActivity();
                    return true;
                } else if (itemId == R.id.Payment_menu) {
                    navigateToPaymentDetailsActivity();
                    return true;
                }
                return false;
            }
        });
    }

    private void navigateToUserDashboardActivity() {
        Intent intent = new Intent(this, UserDashboardActivity.class);
        startActivity(intent);
    }

    private void navigateToContactActivity() {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    private void navigateToPaymentDetailsActivity() {
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivity(intent);
    }
}