package com.example.jobtracker;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Sign_up extends AppCompatActivity
{
    TextView register, reset_password;
    TextInputEditText email, password;
    Button login;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        register = findViewById(R.id.register);
        email = findViewById(R.id.email);
        reset_password = findViewById(R.id.reset_password);
        password = findViewById(R.id.password);
        login = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id._progressBar);
        mAuth = FirebaseAuth.getInstance();

        reset_password.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_up.this, ForgotPassword.class);
            startActivity(intent);
        });
        register.setOnClickListener(v -> {
            Intent intent = new Intent(Sign_up.this, Registraction.class);
            startActivity(intent);
        });
        login.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email1 , password1;
            email1 = String.valueOf(email.getText());
            password1 = String.valueOf(password.getText());
            if(email1.isEmpty()){
                Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password1.isEmpty()){
                Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email1, password1)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"login successful",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Sign_up.this, HomePage.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(Sign_up.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

}