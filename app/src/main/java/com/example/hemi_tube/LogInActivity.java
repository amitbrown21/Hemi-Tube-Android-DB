package com.example.hemi_tube;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.viewmodel.UserViewModel;

public class LogInActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button signUpButton;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        signInButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LogInActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else {
                login(username, password);
            }
        });

        signUpButton.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivityForResult(signUpIntent, 1);
        });
    }

    private void login(String username, String password) {
        userViewModel.login(username, password, new RepositoryCallback<ApiService.LoginResponse>() {
            @Override
            public void onSuccess(ApiService.LoginResponse result) {
                runOnUiThread(() -> {
                    Toast.makeText(LogInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("currentUserId", result.userId);
                    resultIntent.putExtra("token", result.token);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(LogInActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                User newUser = (User) data.getSerializableExtra("newUser");
                if (newUser != null) {
                    Log.d("LogInActivity", "onActivityResult: New user created: " + newUser.toString());
                    // You might want to automatically log in the new user here
                    login(newUser.getUsername(), newUser.getPassword());
                }
            }
        }
    }
}