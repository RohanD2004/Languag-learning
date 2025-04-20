package com.example.languagelearningapplication;



import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegistrationActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword;
    private Button btnRegister;
    private TextView txtLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link UI elements
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
        progressBar = new ProgressBar(this);

        // Button Click Listener
        btnRegister.setOnClickListener(view -> registerUser());

        // Redirect to Login Page
        txtLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            inputName.setError("Enter your full name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Enter password");
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("Password must be at least 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Create a user object
                            User user = new User(userId, name, email);

                            // Save to Realtime Database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.child(userId).setValue(user)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(RegistrationActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegistrationActivity.this, "Failed to save user: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
