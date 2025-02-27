package com.example.jobtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    TextInputEditText email;
    Button send;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id._email);
        send = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id._progressBr);
        mAuth = FirebaseAuth.getInstance();

        send.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email1 = String.valueOf(email.getText());

            if (TextUtils.isEmpty(email1)) {
                Toast.makeText(ForgotPassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email1)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(ForgotPassword.this, Sign_up.class);
                            startActivity(intent);
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error occurred";
                            Toast.makeText(ForgotPassword.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}