package com.example.weatherlyzerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        // Get the user ID passed from the login screen
        int userId = getIntent().getIntExtra("userId", -1);

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

    private void readUserInfoAndDisplay(int userId) {
        try {
            InputStream inputStream = getAssets().open("login.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            Map<Integer, String> userDataMap = new HashMap<>();

            while ((line = bufferedReader.readLine()) != null) {
                String[] userData = line.split(",");
                int id = Integer.parseInt(userData[0]);
                String username = userData[1];
                String password = userData[2];
                String fullName = userData[3];
                String email = userData[4];

                // Add the user data to the map with the user ID as the key
                userDataMap.put(id, fullName + "," + email);
            }

            bufferedReader.close();
            inputStream.close();

            if (userDataMap.containsKey(userId)) {
                String userData = userDataMap.get(userId);
                String[] userDataArray = userData.split(",");

                // Display the user's name and email in the TextViews
                nameTextView.setText(userDataArray[0]);
                emailTextView.setText(userDataArray[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
