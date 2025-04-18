package com.example.akashjewller;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class PaymentActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
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
