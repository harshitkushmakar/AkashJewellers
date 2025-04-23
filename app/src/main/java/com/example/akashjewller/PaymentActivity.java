package com.example.akashjewller;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
// Import View
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


// import androidx.activity.EdgeToEdge; // Uncomment if using EdgeToEdge
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class PaymentActivity extends AppCompatActivity {

    // Optional: Declare copy ImageViews if needed elsewhere, otherwise they can be local to setup methods
    // ImageView copyUnionAcName, copyUnionAcNumber, copyUnionIfsc, copyUnionBranch;
    // ImageView copySbiAcName, copySbiAcNumber, copySbiIfsc, copySbiBranch;
    // ImageView copyMobileNumber;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment if using EdgeToEdge
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Setup Copy Functionality ---
        setupAllBanksCopyFunctionality();
        setupMobileNumberCopy(); // Added call to setup mobile copy

        // --- Initialize and Setup Bottom Navigation ---
        bottomNavigationView = findViewById(R.id.bottomNavigation); // Ensure this ID exists in activity_payment.xml
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        } else {
            // Handle case where BottomNavigationView is not found (e.g., wrong layout)
            Toast.makeText(this, "Error: Bottom Navigation not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        // Set default selected item (optional, if needed)
        // bottomNavigationView.setSelectedItemId(R.id.Payment_menu);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.Home_menu) {
                    // Avoid navigating to self if already here
                    // if (!(this instanceof UserDashboardActivity)) { // Example check
                    navigateToUserDashboardActivity();
                    // }
                    return true;
                } else if (itemId == R.id.Contact_menu) {
                    // if (!(this instanceof ContactActivity)) {
                    navigateToContactActivity();
                    // }
                    return true;
                } else if (itemId == R.id.Payment_menu) {
                    // Already in PaymentActivity, do nothing or refresh if needed
                    // navigateToPaymentDetailsActivity(); // Avoid restarting self
                    return true; // Return true as it's handled (by doing nothing)
                }
                return false; // Return false if the item ID is not handled
            }
        });
    }

    private void setupAllBanksCopyFunctionality() {
        // Setup copy functionality for Union Bank
        setupBankCopyFunctionality(
                R.id.union_ac_name_value, R.id.union_ac_number_value, R.id.union_ifsc_value, R.id.union_branch_value,
                R.id.copy_ac_name, R.id.copy_ac_number, R.id.copy_ifsc, R.id.copy_branch,
                "Union Bank" // Bank name prefix for clipboard label
        );

        // Setup copy functionality for SBI Bank
        setupBankCopyFunctionality(
                R.id.sbi_ac_name_value, R.id.sbi_ac_number_value, R.id.sbi_ifsc_value, R.id.sbi_branch_value,
                R.id.copy_sbi_ac_name, R.id.copy_sbi_ac_number, R.id.copy_sbi_ifsc, R.id.copy_sbi_branch,
                "SBI" // Bank name prefix for clipboard label
        );
    }

    // Method to set up copy functionality for a specific bank's details
    private void setupBankCopyFunctionality(
            int acNameValueId, int acNumberValueId, int ifscValueId, int branchValueId,
            int copyAcNameId, int copyAcNumberId, int copyIfscId, int copyBranchId,
            String bankName) {

        // Find text views (ensure these IDs exist in your layout)
        TextView acNameValue = findViewById(acNameValueId);
        TextView acNumberValue = findViewById(acNumberValueId);
        TextView ifscValue = findViewById(ifscValueId);
        TextView branchValue = findViewById(branchValueId);

        // Find copy icons (ensure these IDs exist in your layout)
        ImageView copyAcNameIcon = findViewById(copyAcNameId);
        ImageView copyAcNumberIcon = findViewById(copyAcNumberId);
        ImageView copyIfscIcon = findViewById(copyIfscId);
        ImageView copyBranchIcon = findViewById(copyBranchId);

        // Set click listeners only if views are found
        if (acNameValue != null && copyAcNameIcon != null) {
            copyAcNameIcon.setOnClickListener(v -> copyToClipboard(bankName + " Account Name", acNameValue.getText().toString()));
        }
        if (acNumberValue != null && copyAcNumberIcon != null) {
            copyAcNumberIcon.setOnClickListener(v -> copyToClipboard(bankName + " Account Number", acNumberValue.getText().toString()));
        }
        if (ifscValue != null && copyIfscIcon != null) {
            copyIfscIcon.setOnClickListener(v -> copyToClipboard(bankName + " IFSC Code", ifscValue.getText().toString()));
        }
        if (branchValue != null && copyBranchIcon != null) {
            copyBranchIcon.setOnClickListener(v -> copyToClipboard(bankName + " Branch", branchValue.getText().toString()));
        }

        // --- Removed the duplicate/incorrect setOnClickListener lines ---
    }

    // Method to set up copy functionality for the mobile number in the QR section
    private void setupMobileNumberCopy() {
        TextView mobileNumberTextView = findViewById(R.id.mobile_number);
        ImageView copyMobileIcon = findViewById(R.id.copy_mobile_number);

        if (mobileNumberTextView != null && copyMobileIcon != null) {
            copyMobileIcon.setOnClickListener(v -> copyToClipboard("Mobile Number", mobileNumberTextView.getText().toString()));
        }
    }


    private void copyToClipboard(String label, String text) {
        // Get clipboard manager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(this, "Clipboard service not available.", Toast.LENGTH_SHORT).show();
            return; // Exit if service is not available
        }
        // Create ClipData object
        ClipData clip = ClipData.newPlainText(label, text);
        // Set the primary clip on the clipboard
        clipboard.setPrimaryClip(clip);

        // Show a confirmation toast message
        Toast.makeText(this, label + " copied!", Toast.LENGTH_SHORT).show();
    }

    // --- Navigation Methods ---
    private void navigateToUserDashboardActivity() {
        
        Intent intent = new Intent(this, UserDashboardActivity.class);
        // Consider adding flags if needed, e.g., clear top
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        // Optional: finish(); // Finish current activity if navigating away permanently
    }

    private void navigateToContactActivity() {
        Intent intent = new Intent(this, ContactActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        // Optional: finish();
    }

    private void navigateToPaymentDetailsActivity() {
        // Intent intent = new Intent(this, PaymentActivity.class); // Avoid restarting self
        // startActivity(intent);
        // Usually do nothing if already on this activity
        Toast.makeText(this, "Already on Payment Screen", Toast.LENGTH_SHORT).show();
    }
}