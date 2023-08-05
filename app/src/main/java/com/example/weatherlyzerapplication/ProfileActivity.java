package com.example.weatherlyzerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.Context;



import androidx.activity.ComponentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends ComponentActivity {

    private Button backbutton;
    private Button logoutbutton;
    private TextView nameTextView;
    private TextView emailTextView;
    private UserDatabaseHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        userDbHelper = new UserDatabaseHelper(this);

        // Retrieve the logged-in user's ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        long userId = sharedPreferences.getLong("userId", -1);


        // Call the method to read user info and display based on the user ID
        readUserInfoAndDisplay(userId);

        setupButtons();
    }

    private void setupButtons() {
        backbutton = findViewById(R.id.backButton);
        logoutbutton = findViewById(R.id.logoutButton);

        logoutbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void readUserInfoAndDisplay(long userId) {
        // Use the UserDatabaseHelper to get the User object based on the user ID
        User user = userDbHelper.getUserById(userId);

        if (user != null) {
            // Display the user's name and email in the TextViews
            nameTextView.setText(user.getName());
            emailTextView.setText(user.getEmail());
        } else {
            nameTextView.setText("Error");
            emailTextView.setText("Error");
        }
    }
}
