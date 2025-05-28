package com.example.learning;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText userName, password;
    Button startButton;
    TextView signUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        startButton = findViewById(R.id.startButton);
        signUpLink = findViewById(R.id.signUpLink);

        // Disable login button until both fields are filled
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                startButton.setEnabled(
                        userName.getText().toString().trim().length() > 0 &&
                                password.getText().toString().trim().length() > 0
                );
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        userName.addTextChangedListener(inputWatcher);
        password.addTextChangedListener(inputWatcher);

        // Login button (store credentials in UserSession and go to next screen)
        startButton.setOnClickListener(v -> {
            String name = userName.getText().toString().trim();
            String pass = password.getText().toString().trim();

            // Store credentials in UserSession
            UserSession.username = name;
            UserSession.password = pass;

            // You can log the credentials for debugging, but make sure to remove them before production
            // Log.d("UserSession", "Username: " + UserSession.username + ", Password: " + UserSession.password);

            // Navigate to the next screen (QuestionActivity)
            Intent intent = new Intent(MainActivity.this, TaskActivity.class);
            startActivity(intent);  // Since username is already in UserSession, no need to pass it here
        });

        // Go to sign-up screen (to implement next)
        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class); // You’ll create this screen
            startActivity(intent);
        });
    }
}
