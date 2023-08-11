package com.example.weatherlyzerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.ComponentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends ComponentActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button createAccountButton;
    private Button cancelButton;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount);

        mAuth = FirebaseAuth.getInstance();

        nameEditText = findViewById(R.id.editName);
        emailEditText = findViewById(R.id.editEmail);
        usernameEditText = findViewById(R.id.editUsername);
        passwordEditText = findViewById(R.id.editPassword);

        createAccountButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(CreateAccountActivity.this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                } else {
                    createAccountWithEmailPassword(name, email, username, password);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void createAccountWithEmailPassword(final String name, final String email, final String username, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                saveUserDataToDatabase(userId, name, email, username);
                            }
                        } else {
                            //creation failed,
                            Toast.makeText(CreateAccountActivity.this, "Failed to create account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDataToDatabase(String userId, String name, String email, String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        User user = new User(name, email, username);
        usersRef.child(userId).setValue(user);

        // After saving the user data go auto back 2 thelogin page
        Toast.makeText(CreateAccountActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }
}