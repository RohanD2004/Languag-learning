package com.example.languagelearningapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnReset;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        btnReset = findViewById(R.id.btnReset);
        progressBar = new ProgressBar(this);

        btnReset.setOnClickListener(view -> resetPassword());
    }

    private void resetPassword() {
        String email = inputEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Enter email");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();

                        // Redirect to Login Page after success
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Close ForgotPasswordActivity

                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
