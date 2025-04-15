package com.example.akashjewller.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class FirebaseUtils {

    // Get current user ID
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    // Get reference to the current user's document in the "users" collection
    public static DocumentReference currentUserDetails() {
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId());
    }
}

