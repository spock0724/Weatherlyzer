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

public class ForgotPasswordActivity extends ComponentActivity {
    private Button cancelButton;
    private Button sendEmailButton;
    private EditText editEmail;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);

        mAuth = FirebaseAuth.getInstance();
        cancelButton = findViewById(R.id.cancelButton);
        sendEmailButton = findViewById(R.id.sendEmailButton);
        editEmail = findViewById(R.id.editEmail);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });
    }

    private void sendPasswordResetEmail() {
        String emailAddress = editEmail.getText().toString().trim();

        if (!emailAddress.isEmpty()) {
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                finish(); // Finish the activity after sending the email
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(ForgotPasswordActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
        }
    }
}
