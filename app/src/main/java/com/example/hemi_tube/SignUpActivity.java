package com.example.hemi_tube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.viewmodel.UserViewModel;

public class SignUpActivity extends AppCompatActivity {
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final String TAG = "SignUpActivity";

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private RadioGroup genderRadioGroup;

    private Uri profileImageUri;
    private ImageButton selectImageButton;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

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
                User newUser = new User("0", firstName, lastName, username, password, gender, profilePictureUri, 0);
                createUser(newUser);
            }
        });
    }

    private void createUser(User user) {
        userViewModel.createUser(user, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newUser", result);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error creating user", e);
                    Toast.makeText(SignUpActivity.this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void selectProfileImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PROFILE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PROFILE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            if (profileImageUri != null) {
                try {
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(profileImageUri, takeFlags);
                    selectImageButton.setImageURI(profileImageUri);
                } catch (SecurityException e) {
                    Log.e(TAG, "Failed to take persistable URI permission", e);
                    Toast.makeText(this, "Failed to access the selected image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^[a-zA-Z0-9]{8,20}$";
        return password.matches(passwordPattern);
    }
}