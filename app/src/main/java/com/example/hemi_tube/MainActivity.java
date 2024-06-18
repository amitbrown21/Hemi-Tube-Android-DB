package com.example.hemi_tube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ShonLog";
    private static final int SIGN_IN_REQUEST = 1;
    private static final int UPLOAD_VIDEO_REQUEST = 2;
    private static final int SIGN_UP_REQUEST = 3;
    public static final int WATCH_VIDEO_REQUEST = 4;

    private List<Video> videos = new ArrayList<>();
    private List<Video> originalVideos = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private User currentUser;

    private VideoRecyclerViewAdapter videoAdapter;
    private RecyclerView videoRecyclerView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageView logoImageView;
    private TextView noResultsTextView;
    private boolean isDarkMode = false;
    private boolean isSignedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dark Mode Preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isDarkMode = preferences.getBoolean("isDarkMode", isDarkMode);

        // Initialize UI elements
        ImageButton modeButton = findViewById(R.id.modeButton);
        ImageButton logInButton = findViewById(R.id.user_menu_btn);
        ImageButton uploadVideoButton = findViewById(R.id.uploadIcon);
        ImageButton homeButton = findViewById(R.id.home_menu_btn);

        videoRecyclerView = findViewById(R.id.videoRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        logoImageView = findViewById(R.id.logoImageView);
        noResultsTextView = findViewById(R.id.noResultsTextView);

        // Check for intent extras and load data as needed
        Intent intent = getIntent();
        if (intent.hasExtra("users")) {
            users = (ArrayList<User>) intent.getSerializableExtra("users");
        } else {
            loadUsersFromJson();
        }

        if (intent.hasExtra("videos")) {
            videos = (ArrayList<Video>) intent.getSerializableExtra("videos");
        } else if(videos != null) {
            loadVideosFromJson();
        }
        if (intent.hasExtra("originalVideos")) {
            originalVideos = (ArrayList<Video>) intent.getSerializableExtra("originalVideos");
        }

        if (intent.hasExtra("currentUser")) {
            currentUser = (User) intent.getSerializableExtra("currentUser");
        }

        if (intent.hasExtra("isSignedIn")) {
            isSignedIn = intent.getBooleanExtra("isSignedIn", false);
        }

        loadVideos();

        // All on click methods

        // Theme
        modeButton.setOnClickListener(v -> modeChange());

        // UserIcon
        logInButton.setOnClickListener(v -> {
            if (isSignedIn) {
                // Show popup menu with logout option
                showUserPopupMenu(v);
            } else {
                // User is not signed in, start the LogInActivity
                Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                logInIntent.putExtra("users", users);
                startActivityForResult(logInIntent, SIGN_IN_REQUEST);
            }
        });

        // UploadIcon
        uploadVideoButton.setOnClickListener(v -> {
            Intent uploadIntent = new Intent(MainActivity.this, UploadVideoActivity.class);
            uploadIntent.putExtra("videos", (Serializable) videos);
            uploadIntent.putExtra("currentUser", currentUser);
            startActivityForResult(uploadIntent, UPLOAD_VIDEO_REQUEST);
        });

        // Home
        homeButton.setOnClickListener(v -> {
            Log.d("ShonLog","Clicked Home, original: "+originalVideos);
            searchVideos("", originalVideos);
        });

        // Search
        searchButton.setOnClickListener(v -> {
            if (searchEditText.getVisibility() == View.GONE) {
                // Show the search EditText and hide the logo
                searchEditText.setVisibility(View.VISIBLE);
                logoImageView.setVisibility(View.GONE);
                searchEditText.requestFocus();
                // Show the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                // Perform the search and hide the search EditText
                String query = searchEditText.getText().toString().trim();
                searchVideos(query, originalVideos); // Pass the original videos list
                searchEditText.setVisibility(View.GONE);
                logoImageView.setVisibility(View.VISIBLE);
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        });

        // Handle keyboard search action
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    // Perform search and hide the keyboard
                    String query = searchEditText.getText().toString().trim();
                    searchVideos(query, originalVideos); // Pass the original videos list
                    searchEditText.setVisibility(View.GONE);
                    logoImageView.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    return true;
                }
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    // Show all videos when the search bar is cleared
                    videoAdapter.updateList(videos);
                    videoRecyclerView.setVisibility(View.VISIBLE);
                    noResultsTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Log data
        logData();
    }

    private void loadUsersFromJson() {
        Gson gson = new Gson();
        try {
            InputStream inputStream = getAssets().open("userData.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JsonArray userArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement userElement : userArray) {
                User user = gson.fromJson(userElement, User.class);
                users.add(user);
            }

            Log.d(TAG, "loadUsersFromJson: Loaded user data successfully");
        } catch (IOException e) {
            Log.e(TAG, "loadUsersFromJson: Error loading user data", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void loadVideosFromJson() {
        Gson gson = new Gson();
        try {
            InputStream inputStream = getAssets().open("videoData.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JsonArray videoArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement videoElement : videoArray) {
                Video video = gson.fromJson(videoElement, Video.class);
                videos.add(video);
            }
            // Make a copy of the original videos list
            originalVideos.addAll(videos);

            Log.d(TAG, "loadVideosFromJson: Loaded video data successfully");
        } catch (IOException e) {
            Log.e(TAG, "loadVideosFromJson: Error loading video data", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void logData() {
        Log.d(TAG, "logData: Users: " + users.toString());
        Log.d(TAG, "logData: Videos: " + videos.toString());
        Log.d(TAG, "logData: CurrentUser: " + (currentUser != null ? currentUser.toString() : "null"));
        Log.d("ShonLog", "In Main, originalVideos: " + originalVideos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SIGN_IN_REQUEST) {
                currentUser = (User) data.getSerializableExtra("currentUser");
                users = (ArrayList<User>) data.getSerializableExtra("users");
                isSignedIn = true;
                videoAdapter.updateCurrentUser(currentUser);
                videoAdapter.updateUserList(users); // Update the users in the adapter
                logData();
            } else if (requestCode == UPLOAD_VIDEO_REQUEST) {
                videos = (ArrayList<Video>) data.getSerializableExtra("videos");
                originalVideos.clear();
                originalVideos.addAll(videos); // Update originalVideos as well
                videoAdapter.updateList(videos); // Update the adapter's data
                logData();
            } else if (requestCode == SIGN_UP_REQUEST) {
                users = (ArrayList<User>) data.getSerializableExtra("users");
                logData();

                // After signup, navigate to the login activity
                Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                logInIntent.putExtra("users", users);
                startActivityForResult(logInIntent, SIGN_IN_REQUEST);
            }
        }
    }

    private void searchVideos(String query, List<Video> allVideos) {
        // Create a new list to store the filtered videos
        List<Video> filteredVideos = new ArrayList<>();

        // Convert query to lowercase for case-insensitive comparison
        String lowerCaseQuery = query.toLowerCase();

        // Iterate through all videos and add matching videos to the filtered list
        for (Video video : allVideos) {
            if (video.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                filteredVideos.add(video);
            }
        }

        // Check if any videos matched the query
        if (filteredVideos.isEmpty()) {
            // No matching videos found
            videoRecyclerView.setVisibility(View.GONE);
            noResultsTextView.setText("No videos matched with '" + query + "'");
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            // Matching videos found
            videoAdapter.updateList(filteredVideos);
            videoRecyclerView.setVisibility(View.VISIBLE);
            noResultsTextView.setVisibility(View.GONE);
        }
    }

    private void loadVideos() {
        videoAdapter = new VideoRecyclerViewAdapter(this, videos, users, currentUser, originalVideos);
        videoRecyclerView.setAdapter(videoAdapter);
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void modeChange() {
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        isDarkMode = !isDarkMode;
        // Save the current theme mode in SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isDarkMode", isDarkMode);
        editor.apply();

        // Restart activity to apply theme change and pass necessary data
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("users", users);
        intent.putExtra("videos", (Serializable) originalVideos); // Ensure videos list is passed here
        intent.putExtra("originalVideos", (Serializable) originalVideos);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("isSignedIn", isSignedIn);
        finish();
        startActivity(intent);
    }

    private void showUserPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_user, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        currentUser = null;
        isSignedIn = false;
        videoAdapter.updateCurrentUser(null);
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }
}
