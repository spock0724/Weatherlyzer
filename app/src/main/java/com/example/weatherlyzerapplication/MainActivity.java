package com.example.weatherlyzerapplication;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends ComponentActivity {
    private EditText inputUsername;
    private EditText inputPassword;
    private Button loginButton;
    private Button createAccountButton;
    private Button forgotPasswordButton;




    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        firebaseAuth = FirebaseAuth.getInstance();


        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginbutton);
        createAccountButton = findViewById(R.id.newaccountbutton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);


        loginButton.setOnClickListener(v -> loginUser());


        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });


        forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });


    }


    private void loginUser() {
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();


        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            return;
        }


        firebaseAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // successful, navigate to the home again
                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // finish MainActivity so that you can't navigate back to it
                        // (UNLESS IN PROFILE PAGE with logout button!!)
                    } else {
                        // else it fails, display error
                        Toast.makeText(MainActivity.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
