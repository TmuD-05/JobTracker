package com.example.jobtracker;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class Registraction extends AppCompatActivity
{
    TextInputEditText email, password;
    Button reg;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registraction);

        email = findViewById(R.id._email);
        mAuth = FirebaseAuth.getInstance();
        password = findViewById(R.id._password);
        reg = findViewById(R.id.btn_reg);
        progressBar = findViewById(R.id.progressBar);

        reg.setOnClickListener(v -> {
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
            mAuth.createUserWithEmailAndPassword(email1, password1)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(Registraction.this,"Account created",Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(Registraction.this, Sign_up.class);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Registraction.this, "Authentication failed.",

                                        Toast.LENGTH_SHORT).show();
                                if(password1.length()<6)
                                {
                                    Toast.makeText(Registraction.this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        });

    }

}