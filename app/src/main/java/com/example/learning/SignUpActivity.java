package com.example.learning;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText username, email, confirmEmail, password, confirmPassword, phone;
    Button createAccountButton;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Get references
        username = findViewById(R.id.signupUsername);
        email = findViewById(R.id.signupEmail);
        confirmEmail = findViewById(R.id.signupConfirmEmail);
        password = findViewById(R.id.signupPassword);
        confirmPassword = findViewById(R.id.signupConfirmPassword);
        phone = findViewById(R.id.signupPhone);
        profilePicture = findViewById(R.id.profilePicture);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Later we can handle photo upload
        profilePicture.setOnClickListener(v -> {
            Toast.makeText(this, "Profile picture upload coming soon!", Toast.LENGTH_SHORT).show();
        });

        createAccountButton.setOnClickListener(v -> {
            // Simple validation for now
            if (!email.getText().toString().equals(confirmEmail.getText().toString())) {
                Toast.makeText(this, "Emails do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.getText().toString().isEmpty() || email.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user session data (for now, just username)
            UserSession.username = username.getText().toString();

            // Pass basic info to next activity for now
            Intent intent = new Intent(SignUpActivity.this, InterestSelectionActivity.class);
            intent.putExtra("userName", username.getText().toString());
            startActivity(intent);
        });
    }
}
