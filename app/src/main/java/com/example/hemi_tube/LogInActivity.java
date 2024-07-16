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

import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogInActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private Button signUpButton;
    private AppDatabase database;
    private UserDao userDao;
    private ExecutorService executorService;

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

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();

        signInButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LogInActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else {
                validateUser(username, password);
            }
        });

        signUpButton.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivityForResult(signUpIntent, 1);
        });
    }

    private void validateUser(String username, String password) {
        executorService.execute(() -> {
            User currentUser = userDao.getUserForLogin(username, password);
            runOnUiThread(() -> {
                if (currentUser != null) {
                    Toast.makeText(LogInActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("currentUser", currentUser);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(LogInActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
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
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}