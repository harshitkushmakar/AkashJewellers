package com.example.akashjewller; // Make sure this matches your package name

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.akashjewller.databinding.FragmentContactBinding; // Generated binding class

public class ContactFragment extends Fragment {

    private FragmentContactBinding binding; // View binding object

    private ImageView phone_btn;
    private ImageView whatsapp_btn;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentContactBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Return the root view
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get references to the ImageViews from the binding object
        phone_btn = binding.btnPhone; // Corrected ID to match layout
        whatsapp_btn = binding.btnWhatsapp; // Corrected ID to match layout

        // Set up WhatsApp icon click listener
        whatsapp_btn.setOnClickListener(v -> {
            String phoneNumber = "9162766127"; // First number
            String whatsappUrl = "https://wa.me/" + phoneNumber;
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl));
                // Use requireActivity() or requireContext() to start activity from Fragment
                requireActivity().startActivity(intent);
            } catch (Exception e) {
                // Handle cases where WhatsApp is not installed, e.g., show a Toast
                // Toast.makeText(requireContext(), "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
                e.printStackTrace(); // Log the exception
            }
        });

        // Set up phone icon click listener for dialing
        phone_btn.setOnClickListener(v -> {
            String phoneNumber = "8579909138"; // Second number
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            // Use requireActivity() or requireContext() to start activity from Fragment
            requireActivity().startActivity(intent);
        });

        // Any other view-related setup for the contact fragment can go here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the binding object
        binding = null;
    }
}