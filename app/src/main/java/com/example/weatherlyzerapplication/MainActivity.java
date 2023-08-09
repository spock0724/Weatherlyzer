package com.example.weatherlyzerapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.activity.ComponentActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in successful, navigate to the home again
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Finish the MainActivity so that the user can't navigate back to it
                            // (UNLESS IN PROFILE PAGE with logout button!!)
                        } else {
                            // If sign-in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
//TODO I think we ditch this shared pref thing bc we have firebasenow (prop will delete everything below)
//private SharedPreferences sharedPreferences;

//private UserDatabaseHelper userDbHelper;
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        //userDbHelper = new UserDatabaseHelper(this); // Initialize the UserDatabaseHelper
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

        /*
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText uText = findViewById(R.id.inputUsername);
                EditText pText = findViewById(R.id.inputPassword);
                String username = uText.getText().toString();
                String password = pText.getText().toString();

                // Use the UserDatabaseHelper to authenticate the user
                //User user = userDbHelper.getUserByUsernameAndPassword(username, password);
                //if (user != null) {
                //long userId = user.getId();

                    // Save the logged-in user's ID to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                //    editor.putLong("userId", userId);
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
         */