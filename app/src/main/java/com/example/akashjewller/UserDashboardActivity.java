package com.example.akashjewller;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Firebase Imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Other necessary imports
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// Material Design Imports
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class UserDashboardActivity extends AppCompatActivity {

    private static final String TAG = "UserDashboardActivity";

    // Declare TextViews matching the XML IDs
    private TextView tvGoldPrice, tvGoldUpdateTime;
    private TextView tvSilverPrice, tvSilverUpdateTime;
    private TextView tvGoldUpiPrice, tvGoldUpiUpdateTime; // Corresponds to RTGS price in Firebase
    private TextView tvSilverUpiPrice, tvSilverUpiUpdateTime; // Corresponds to RTGS price in Firebase

    // Declare Firebase Database reference and Listener
    private DatabaseReference priceUpdateNodeReference;
    private ValueEventListener priceValueEventListener;

    // Declare Bottom Navigation View
//    private BottomNavigationView bottomNavigationView; // Keep commented as per last request

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
        // bottomNavigationView = findViewById(R.id.bottomNavigation); // Keep commented

        // Initialize Firebase Database reference
        priceUpdateNodeReference = FirebaseDatabase.getInstance().getReference("price_updates");

        // Setup the real-time listener for price updates
        setupFirebaseListener();

        // Setup listener for Bottom Navigation View item clicks
        // setupBottomNavigation(); // Keep commented
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
                // *** ALIGNED FORMAT *** Use the same format as Admin's display button
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm a", Locale.getDefault());
                // sdf.setTimeZone(TimeZone.getDefault()); // Optional
                Date resultDate = new Date(prices.getTimestamp());
                formattedTime = sdf.format(resultDate);
            } catch (Exception e) {
                Log.e(TAG, "Error formatting timestamp", e);
                formattedTime = "Invalid Date";
            }
        }

        // Set prices and the formatted server timestamp
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

    // Setup listener for Bottom Navigation View (Keep commented)
    /*
    private void setupBottomNavigation() {
        // ... (implementation remains the same)
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (priceUpdateNodeReference != null && priceValueEventListener != null) {
            priceUpdateNodeReference.removeEventListener(priceValueEventListener);
            Log.d(TAG, "Firebase ValueEventListener removed.");
        }
    }
}