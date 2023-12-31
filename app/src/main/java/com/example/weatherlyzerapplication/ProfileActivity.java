package com.example.weatherlyzerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends ComponentActivity {

    private Button backButton;
    private Button logoutButton;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView usernameTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        logoutButton = findViewById(R.id.logoutButton);
        backButton = findViewById(R.id.backButton);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        usernameTextView = findViewById(R.id.usernameTextView);

        // Get the current user from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the user ID of the current user
            String userId = currentUser.getUid();

            // Get the user data from the "users" node in the Firebase Database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Retrieve user data from the DataSnapshot
                    User user = dataSnapshot.getValue(User.class);

                    // Update the TextViews with user data
                    if (user != null) {
                        nameTextView.setText("Name: " + user.getName());
                        emailTextView.setText("Email: " + user.getEmail());
                        usernameTextView.setText("Username: " + user.getUsername());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //database read error
                    // (we can delete probs)
                }
            });
        }
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}