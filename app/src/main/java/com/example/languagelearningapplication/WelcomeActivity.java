package com.example.languagelearningapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User already signed in, redirect to HomeActivity
            startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
            finish(); // Prevent going back to LoginActivity
            return; // Optional: stop further execution
        }

        Button getStartedButton = findViewById(R.id.btn_get_started);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Login Page
                Intent intent = new Intent(WelcomeActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish(); // Close WelcomeActivity
            }
        });
    }


}
