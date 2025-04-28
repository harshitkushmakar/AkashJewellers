package com.example.akashjewller;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.akashjewller.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge should be enabled before setting the content view
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the root view of the inflated binding

        // Set the initial fragment
        replaceFragment(new UserDashboardFragment());

        // Set background to null if needed (as per your original code)
        binding.bottomNavigationView.setBackground(null);

        // Set up the item selected listener for the bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.Home_menu) {
                replaceFragment(new UserDashboardFragment());
                return true;
            } else if (id == R.id.Payment_menu) {
                replaceFragment(new PaymentFragment());
                return true;
            } else if (id == R.id.Contact_menu) {
                replaceFragment(new ContactFragment());
                return true;
            }

            return false;
        });

        // *** MODIFIED ViewCompat.setOnApplyWindowInsetsListener ***
        // Apply window insets as padding, but exclude the bottom inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply top, left, and right system bar insets as padding to the root view.
            // DO NOT apply the bottom system bar inset here.
            // The BottomNavigationView at the bottom should handle the bottom inset itself
            // and extend into the system navigation bar area.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Removed systemBars.bottom

            // Return the insets. This is important so that child views (like BottomNavigationView)
            // can consume the insets they need (e.g., the bottom inset for the nav bar).
            return insets;
        });
    }

    // Method to replace fragments in the frame_layout
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Ensure R.id.frame_layout is the ID of the FrameLayout in your activity_main.xml
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}