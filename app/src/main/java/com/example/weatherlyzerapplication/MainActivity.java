package com.example.weatherlyzerapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.ComponentActivity;

public class MainActivity extends ComponentActivity {
    private Button button;
    private Button createaccount;
    private SharedPreferences sharedPreferences;

    private UserDatabaseHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userDbHelper = new UserDatabaseHelper(this); // Initialize the UserDatabaseHelper
        setupButtons();
    }

    private void setupButtons() {
        createaccount = findViewById(R.id.newaccountbutton);
        button = findViewById(R.id.loginbutton);

        createaccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText uText = findViewById(R.id.inputUsername);
                EditText pText = findViewById(R.id.inputPassword);
                String username = uText.getText().toString();
                String password = pText.getText().toString();

                // Use the UserDatabaseHelper to authenticate the user
                User user = userDbHelper.getUserByUsernameAndPassword(username, password);
                if (user != null) {
                    long userId = user.getId();

                    // Save the logged-in user's ID to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong("userId", userId);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    uText.setText("");
                    pText.setText("");
                    uText.setError("Incorrect username and password combination.");
                    pText.setError("Incorrect username and password combination.");
                }
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
