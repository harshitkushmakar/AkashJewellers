package com.example.akashjewller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UserDashboardActivity extends AppCompatActivity {

    private static final String TAG = "UserDashboardActivity";
    private static final String PREF_LAST_LOGIN = "last_login";
    private static final long LOGOUT_THRESHOLD = TimeUnit.DAYS.toMillis(15);

    private TextView tvGoldPrice, tvGoldUpdateTime;
    private TextView tvSilverPrice, tvSilverUpdateTime;
    private TextView tvGoldUpiPrice, tvGoldUpiUpdateTime;
    private TextView tvSilverUpiPrice, tvSilverUpiUpdateTime;

    private DatabaseReference priceUpdateNodeReference;
    private ValueEventListener priceValueEventListener;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            findViewById(R.id.scrollView).setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Check for inactivity and logout if needed
        checkLastLogin();

        // Update the last login time
        updateLastLogin();

        // Initialize TextViews from the layout
        tvGoldPrice = findViewById(R.id.gold_price);
        tvGoldUpdateTime = findViewById(R.id.gold_update_time);
        tvSilverPrice = findViewById(R.id.silver_price);
        tvSilverUpdateTime = findViewById(R.id.silver_update_time);
        tvGoldUpiPrice = findViewById(R.id.gold_upi_price);
        tvGoldUpiUpdateTime = findViewById(R.id.gold_upi_update_time);
        tvSilverUpiPrice = findViewById(R.id.silver_upi_price);
        tvSilverUpiUpdateTime = findViewById(R.id.silver_upi_update_time);

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();

        // Initialize Firebase Database reference
        priceUpdateNodeReference = FirebaseDatabase.getInstance().getReference("price_updates");

        // Setup the real-time listener for price updates
        setupFirebaseListener();
    }

    private void checkLastLogin() {
        long lastLoginTime = sharedPreferences.getLong(PREF_LAST_LOGIN, 0);
        long currentTime = System.currentTimeMillis();

        if (lastLoginTime != 0 && (currentTime - lastLoginTime) > LOGOUT_THRESHOLD) {
            // User hasn't opened the app for more than 15 days, log them out
            mAuth.signOut();
            Toast.makeText(this, "Logged out due to inactivity.", Toast.LENGTH_LONG).show();
            navigateToLoginActivity();
        }
    }

    private void updateLastLogin() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_LAST_LOGIN, System.currentTimeMillis());
        editor.apply();
        Log.d(TAG, "Last login time updated.");
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    private void setupFirebaseListener() {
        priceValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    PriceData currentPrices = dataSnapshot.getValue(PriceData.class);
                    if (currentPrices != null) {
                        Log.d(TAG, "Data received: Gold=" + currentPrices.getGold_price());
                        updatePriceUI(currentPrices);
                    } else {
                        Log.w(TAG, "PriceData object is null.");
                        clearPriceUI("Data Error");
                    }
                } else {
                    Log.w(TAG, "price_updates node does not exist.");
                    clearPriceUI("No Data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read price data.", databaseError.toException());
                Toast.makeText(UserDashboardActivity.this, "Failed to load price data.", Toast.LENGTH_SHORT).show();
                clearPriceUI("Load Error");
            }
        };
        priceUpdateNodeReference.addValueEventListener(priceValueEventListener);
        Log.d(TAG, "Firebase ValueEventListener attached.");
    }

    private void updatePriceUI(PriceData prices) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setMaximumFractionDigits(2);
        currencyFormat.setMinimumFractionDigits(2);

        String formattedTime = "--";
        if (prices.getTimestamp() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm a", Locale.getDefault());
                Date resultDate = new Date(prices.getTimestamp());
                formattedTime = sdf.format(resultDate);
            } catch (Exception e) {
                Log.e(TAG, "Error formatting timestamp", e);
                formattedTime = "Invalid Date";
            }
        }

        tvGoldPrice.setText(prices.getGold_price() != null ? currencyFormat.format(prices.getGold_price()) : "N/A");
        tvGoldUpdateTime.setText(formattedTime);

        tvSilverPrice.setText(prices.getSilver_price() != null ? currencyFormat.format(prices.getSilver_price()) : "N/A");
        tvSilverUpdateTime.setText(formattedTime);

        tvGoldUpiPrice.setText(prices.getGold_rtgs_price() != null ? currencyFormat.format(prices.getGold_rtgs_price()) : "N/A");
        tvGoldUpiUpdateTime.setText(formattedTime);

        tvSilverUpiPrice.setText(prices.getSilver_rtgs_price() != null ? currencyFormat.format(prices.getSilver_rtgs_price()) : "N/A");
        tvSilverUpiUpdateTime.setText(formattedTime);
    }

    private void clearPriceUI(String message) {
        tvGoldPrice.setText(message);
        tvSilverPrice.setText("");
        tvGoldUpiPrice.setText("");
        tvSilverUpiPrice.setText("");

        String placeholderTime = "--";
        tvGoldUpdateTime.setText(placeholderTime);
        tvSilverUpdateTime.setText(placeholderTime);
        tvGoldUpiUpdateTime.setText(placeholderTime);
        tvSilverUpiUpdateTime.setText(placeholderTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (priceUpdateNodeReference != null && priceValueEventListener != null) {
            priceUpdateNodeReference.removeEventListener(priceValueEventListener);
            Log.d(TAG, "Firebase ValueEventListener removed.");
        }
    }
}