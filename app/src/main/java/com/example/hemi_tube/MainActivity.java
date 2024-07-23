package com.example.hemi_tube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.viewmodel.UserViewModel;
import com.example.hemi_tube.viewmodel.VideoViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int SIGN_IN_REQUEST = 1;
    private static final int UPLOAD_VIDEO_REQUEST = 2;
    private static final int SIGN_UP_REQUEST = 3;
    public static final int WATCH_VIDEO_REQUEST = 4;
    private static final int PICK_PROFILE_PICTURE_REQUEST = 5;
    private static final int MENU_CHANNEL_ID = View.generateViewId();
    private List<Video> videos = new ArrayList<>();
    private User currentUser;

    private Uri profilePictureUri;


    private VideoRecyclerViewAdapter videoAdapter;
    private RecyclerView videoRecyclerView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageView logoImageView;
    private TextView noResultsTextView;
    private boolean isDarkMode = false;
    private boolean isSignedIn = false;

    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModels
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Dark Mode Preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isDarkMode = preferences.getBoolean("isDarkMode", isDarkMode);

        // Initialize UI elements
        ImageButton modeButton = findViewById(R.id.modeButton);
        ImageButton uploadVideoButton = findViewById(R.id.uploadIcon);
        ImageButton homeButton = findViewById(R.id.home_menu_btn);

        videoRecyclerView = findViewById(R.id.videoRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        logoImageView = findViewById(R.id.logoImageView);
        noResultsTextView = findViewById(R.id.noResultsTextView);

        // Check for logged in user
        SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        String userId = authPrefs.getString("user_id", null);

        // Also check if user_id is passed in the intent
        if (userId == null) {
            userId = getIntent().getStringExtra("user_id");
        }

        if (userId != null) {
            final String finalUserId = userId;
            userViewModel.getUserById(userId).observe(this, user -> {
                if (user != null) {
                    currentUser = user;
                    isSignedIn = true;
                    updateUI();
                } else {
                    // If user is null, clear SharedPreferences and show error
                    SharedPreferences.Editor editor = authPrefs.edit();
                    editor.clear();
                    editor.apply();
                    Toast.makeText(this, "Error loading user data. Please login again.", Toast.LENGTH_LONG).show();
                    // Redirect to login screen
                    Intent loginIntent = new Intent(MainActivity.this, LogInActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            });
        } else {
            updateUI(); // This will set up the default login button
        }

        loadVideos();

        // All on click methods

        // Theme
        modeButton.setOnClickListener(v -> modeChange());

        // UploadIcon
        uploadVideoButton.setOnClickListener(v -> {
            if (isSignedIn) {
                Intent uploadIntent = new Intent(MainActivity.this, UploadVideoActivity.class);
                uploadIntent.putExtra("currentUser", currentUser);
                startActivityForResult(uploadIntent, UPLOAD_VIDEO_REQUEST);
            } else {
                Toast.makeText(this, "Please sign in to upload videos", Toast.LENGTH_SHORT).show();
            }
        });

        // Home
        homeButton.setOnClickListener(v -> loadVideos());

        // Search
        setupSearch();
    }

    private void loadVideos() {
        Log.d(TAG, "Loading videos");
        videoViewModel.getAllVideos().observe(this, videos -> {
            Log.d(TAG, "Received " + (videos != null ? videos.size() : 0) + " videos");
            Log.d(TAG, videos.toString());

            if (videos != null && !videos.isEmpty()) {
                try {
                    videoAdapter = new VideoRecyclerViewAdapter(this, videos, userViewModel, videoViewModel, currentUser);
                    videoRecyclerView.setAdapter(videoAdapter);
                    videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    videoRecyclerView.setVisibility(View.VISIBLE);
                    noResultsTextView.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting up video adapter", e);
                    Toast.makeText(this, "Error loading videos", Toast.LENGTH_SHORT).show();
                }
            } else {
                videoRecyclerView.setVisibility(View.GONE);
                noResultsTextView.setText("No videos available");
                noResultsTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            if (searchEditText.getVisibility() == View.GONE) {
                searchEditText.setVisibility(View.VISIBLE);
                logoImageView.setVisibility(View.GONE);
                searchEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                performSearch();
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    loadVideos();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        videoViewModel.searchVideos(query).observe(this, searchResults -> {
            if (searchResults == null || searchResults.isEmpty()) {
                videoRecyclerView.setVisibility(View.GONE);
                noResultsTextView.setText("No videos matched with '" + query + "'");
                noResultsTextView.setVisibility(View.VISIBLE);
            } else {
                if (videoAdapter == null) {
                    videoAdapter = new VideoRecyclerViewAdapter(this, searchResults, userViewModel, videoViewModel, currentUser);
                    videoRecyclerView.setAdapter(videoAdapter);
                } else {
                    videoAdapter.updateList(searchResults);
                }
                videoRecyclerView.setVisibility(View.VISIBLE);
                noResultsTextView.setVisibility(View.GONE);
            }
            searchEditText.setVisibility(View.GONE);
            logoImageView.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (data != null) {
            Log.d(TAG, "Intent data: " + data.toString());
        } else {
            Log.d(TAG, "Intent data is null");
        }

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case SIGN_IN_REQUEST:
                    String userId = data.getStringExtra("user_id");
                    Log.d(TAG, "Sign in successful, user_id: " + userId);
                    if (userId != null) {
                        userViewModel.getUserById(userId).observe(this, user -> {
                            if (user != null) {
                                Log.d(TAG, "User data retrieved: " + user.toString());
                                currentUser = user;
                                isSignedIn = true;
                                updateUI();
                                loadVideos();
                            } else {
                                Log.e(TAG, "Failed to retrieve user data for id: " + userId);
                                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "User ID is null after sign in");
                        Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case UPLOAD_VIDEO_REQUEST:
                    String uploadedVideoId = data.getStringExtra("uploaded_video_id");
                    Log.d(TAG, "Video uploaded, id: " + uploadedVideoId);
                    if (uploadedVideoId != null) {
                        Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                        loadVideos();
                    } else {
                        Log.e(TAG, "Uploaded video ID is null");
                        Toast.makeText(this, "Failed to get uploaded video details", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case SIGN_UP_REQUEST:
                    Log.d(TAG, "Sign up successful, redirecting to sign in");
                    Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                    startActivityForResult(logInIntent, SIGN_IN_REQUEST);
                    break;

                case PICK_PROFILE_PICTURE_REQUEST:
                    profilePictureUri = data.getData();
                    if (profilePictureUri != null) {
                        grantUriPermission(getPackageName(), profilePictureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Here, update the user profile picture URI
                        if (currentUser != null) {
                            currentUser.setProfilePicture(profilePictureUri.toString());
                        }
                    }
                    break;

                default:
                    Log.w(TAG, "Unknown request code: " + requestCode);
                    break;
            }
        } else if (resultCode != RESULT_CANCELED) {
            Log.e(TAG, "Activity result not OK. ResultCode: " + resultCode);
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void modeChange() {
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        isDarkMode = !isDarkMode;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isDarkMode", isDarkMode);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void showUserPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_user, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_channel) {
                openChannelPage();
                return true;
            }
            else if (itemId == R.id.menu_edit_user) {
                showEditUserDialog(); // Call the method to show the Edit User dialog
                return true;
            } else if (itemId == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void openChannelPage() {
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, ChannelActivity.class);
            intent.putExtra("currentUserId", currentUser.getId());
            Log.d("Shon in main", currentUser.getId());
            startActivity(intent);
        }
    }

    private void logout() {
        SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        authPrefs.edit().clear().apply();
        currentUser = null;
        isSignedIn = false;
        updateUI();
    }

    private void showEditUserDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View editUserView = inflater.inflate(R.layout.dialog_edit_user, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(editUserView);

        EditText editFirstName = editUserView.findViewById(R.id.editFirstName);
        EditText editLastName = editUserView.findViewById(R.id.editLastName);
        EditText editUsername = editUserView.findViewById(R.id.editUsername);
        ImageButton selectProfilePictureButton = editUserView.findViewById(R.id.selectProfilePictureButton);
        Button saveUserButton = editUserView.findViewById(R.id.saveUserButton);

        // Populate fields with current user details
        if (currentUser != null) {
            editFirstName.setText(currentUser.getFirstName());
            editLastName.setText(currentUser.getLastName());
            editUsername.setText(currentUser.getUsername());
            profilePictureUri = Uri.parse(currentUser.getProfilePicture());
        }

        selectProfilePictureButton.setOnClickListener(v -> selectProfilePicture());

        AlertDialog dialog = builder.create();
        dialog.show();

        saveUserButton.setOnClickListener(v -> {
            String newFirstName = editFirstName.getText().toString();
            String newLastName = editLastName.getText().toString();
            String newUsername = editUsername.getText().toString();

            Log.d(TAG, "test");

            if (newFirstName.isEmpty() || newLastName.isEmpty() || newUsername.isEmpty() || profilePictureUri == null) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            } else {
                currentUser.setFirstName(newFirstName);
                currentUser.setLastName(newLastName);
                currentUser.setUsername(newUsername);
                currentUser.setProfilePicture(profilePictureUri.toString());

                Log.d(TAG, "updates User : " +currentUser.toString());

                String filePath = FileUtil.getPathFromUri(this, profilePictureUri);
                if (filePath != null) {
                    File profileImageFile = new File(filePath);
                    RequestBody profileImageRequestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(profilePictureUri)), profileImageFile);
                    MultipartBody.Part profileImageBody = MultipartBody.Part.createFormData("profileImage", profileImageFile.getName(), profileImageRequestFile);

                    updateUser(currentUser, profileImageBody, dialog);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get image path", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void selectProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PROFILE_PICTURE_REQUEST);
    }

    private void updateUser(User user, MultipartBody.Part profileImageBody, AlertDialog dialog) {
        Log.d(TAG, "new User: "+user.toString());
        userViewModel.updateUser(user, profileImageBody, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    updateUI();
                    Toast.makeText(MainActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUI() {
        ImageButton logInButton = findViewById(R.id.user_menu_btn);
        if (isSignedIn && currentUser != null) {
            // Load user's profile picture
            String profilePicturePath = currentUser.getProfilePicture();
            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                String imageUrl;
                if (profilePicturePath.startsWith("content://")) {
                    // If the profile picture is a URI, load it directly
                    imageUrl = profilePicturePath;
                } else {
                    // Otherwise, assume it is a URL from the server
                    imageUrl = "http://10.0.2.2:3000/" + profilePicturePath;
                }
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .circleCrop()
                        .into(logInButton);
            } else {
                logInButton.setImageResource(R.drawable.profile);
            }
            logInButton.setOnClickListener(this::showUserPopupMenu);
        } else {
            // Reset to default login button
            logInButton.setImageResource(R.drawable.user_icon);
            logInButton.setOnClickListener(v -> {
                Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                startActivityForResult(logInIntent, SIGN_IN_REQUEST);
            });
        }

        if (videoAdapter != null) {
            videoAdapter.updateCurrentUser(currentUser);
        }
        // Update any other UI elements that depend on the user's signed-in state
    }

}