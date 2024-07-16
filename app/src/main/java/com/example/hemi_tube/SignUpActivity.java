package com.example.hemi_tube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private RadioGroup genderRadioGroup;

    private Uri profileImageUri;
    private ImageButton selectImageButton;

    private AppDatabase database;
    private UserDao userDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.passwordConfirmationEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        selectImageButton = findViewById(R.id.selectImageButton);
        Button signUpButton = findViewById(R.id.signUpButton);

        selectImageButton.setOnClickListener(v -> selectProfileImage());

        signUpButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            String gender = selectedGenderButton != null ? selectedGenderButton.getText().toString() : "";

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || gender.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if (!isValidPassword(password)) {
                Toast.makeText(SignUpActivity.this, "Password must be 8-20 characters long and contain only English letters and numbers", Toast.LENGTH_SHORT).show();
            } else {
                String profilePictureUri = (profileImageUri != null) ? profileImageUri.toString() : "drawable/placeholder";
                User newUser = new User(0, firstName, lastName, username, password, gender, profilePictureUri, 0);
                insertUser(newUser);
            }
        });
    }

    private void insertUser(User user) {
        executorService.execute(() -> {
            // Check if username already exists
            if (userDao.isUsernameExists(user.getUsername())) {
                runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Username already exists", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();

                    // Create an intent to return to MainActivity
                    Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                    mainIntent.putExtra("currentUser", user);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(mainIntent);
                    finish();
                });
            }
        });
    }

    private void selectProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PROFILE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            if (profileImageUri != null) {
                selectImageButton.setImageURI(profileImageUri);
                // Grant URI permission
                grantUriPermission(getPackageName(), profileImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(profileImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    private boolean isValidPassword(String password) {
        // Password must be 8-20 characters long and contain only English letters and numbers
        String passwordPattern = "^[a-zA-Z0-9]{8,20}$";
        return password.matches(passwordPattern);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}