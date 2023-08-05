package com.example.weatherlyzerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

public class CreateAccountActivity extends ComponentActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button createAccountButton;
    private Button cancelButton;


    private UserDatabaseHelper userDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount);

        // Initialize the UserDatabaseHelper
        userDbHelper = new UserDatabaseHelper(this);

        // Find views by their IDs
        nameEditText = findViewById(R.id.editName);
        emailEditText = findViewById(R.id.editEmail);
        usernameEditText = findViewById(R.id.editUsername);
        passwordEditText = findViewById(R.id.editPassword);
        createAccountButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Set click listener for the createAccountButton
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve user input from EditText fields
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                // Check if the username already exists in the database
                if (userDbHelper.isUsernameTaken(username)) {
                    // Show an error message to the user that the username is already taken
                    Toast.makeText(CreateAccountActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                } else {
                    // Add the new user to the database
                    long id = userDbHelper.addUser(name, email, username, password);

                    // Navigate back to the login page
                    onBackPressed();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection when the activity is destroyed
        if (userDbHelper != null) {
            userDbHelper.close();
        }
    }
}
