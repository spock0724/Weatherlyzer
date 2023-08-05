package com.example.weatherlyzerapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.ComponentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends ComponentActivity {
    private Button button;
    private AssetManager assets;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        assets = getAssets();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        setupButtons();
    }

    private void setupButtons() {
        button = findViewById(R.id.loginbutton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText uText = findViewById(R.id.inputUsername);
                EditText pText = findViewById(R.id.inputPassword);
                String username = uText.getText().toString();
                String password = pText.getText().toString();

                if (authenticate(username, password)) {
                    int userId = getUserId(username);

                    // Save the logged-in user's ID to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("userId", userId);
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

    private boolean authenticate(String username, String password) {
        try {
            InputStream inputStream = assets.open("login.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] userData = line.split(",");
                String user = userData[1];
                String pass = userData[2];

                if (username.equalsIgnoreCase(user) && password.equals(pass)) {
                    bufferedReader.close();
                    inputStream.close();
                    return true;
                }
            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private int getUserId(String username) {
        try {
            InputStream inputStream = assets.open("login.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] userData = line.split(",");
                String user = userData[1];
                int userId = Integer.parseInt(userData[0]);

                if (username.equalsIgnoreCase(user)) {
                    bufferedReader.close();
                    inputStream.close();
                    return userId;
                }
            }

            bufferedReader.close();
            inputStream.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
