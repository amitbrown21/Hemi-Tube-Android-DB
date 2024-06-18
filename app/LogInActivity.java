package com.example.hemi_tube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hemi_tube.entities.User;

import java.io.Serializable;
import java.util.ArrayList;

public class LogInActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button signUpButton;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Get users from the intent
        Intent intent = getIntent();
        users = (ArrayList<User>) intent.getSerializableExtra("users");

        signInButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else {
                User currentUser = validateUser(username, password);
                if (currentUser != null) {
                    Toast.makeText(SignInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    // Return to the main activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("currentUser", currentUser);
                    resultIntent.putExtra("users", (Serializable) users);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUpButton.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
            signUpIntent.putExtra("users", (Serializable) users);
            startActivity(signUpIntent);
        });
    }

    private User validateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}