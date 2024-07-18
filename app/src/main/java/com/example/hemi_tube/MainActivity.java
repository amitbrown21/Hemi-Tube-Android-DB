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

import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;
import com.example.hemi_tube.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ShonLog";
    private static final int SIGN_IN_REQUEST = 1;
    private static final int UPLOAD_VIDEO_REQUEST = 2;
    private static final int SIGN_UP_REQUEST = 3;
    public static final int WATCH_VIDEO_REQUEST = 4;

    private List<Video> videos = new ArrayList<>();
    private User currentUser;

    private VideoRecyclerViewAdapter videoAdapter;
    private RecyclerView videoRecyclerView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageView logoImageView;
    private TextView noResultsTextView;
    private boolean isDarkMode = false;
    private boolean isSignedIn = false;

    private AppDatabase database;
    private UserDao userDao;
    private VideoDao videoDao;
    private ExecutorService executorService;

    private VideoRepository videoRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        videoDao = database.videoDao();
        executorService = Executors.newSingleThreadExecutor();

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
        if (intent.hasExtra("currentUser")) {
            currentUser = (User) intent.getSerializableExtra("currentUser");
            isSignedIn = true;
        }

        // Initialize VideoRepository
        videoRepository = new VideoRepository(getApplicationContext());

        loadVideos();

        // All on click methods

        // Theme
        modeButton.setOnClickListener(v -> modeChange());

        // UserIcon
        logInButton.setOnClickListener(v -> {
            if (isSignedIn) {
                showUserPopupMenu(v);
            } else {
                Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                startActivityForResult(logInIntent, SIGN_IN_REQUEST);
            }
        });

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
        videoRepository.getAllVideos().observe(this, videos -> {
            if (videos != null && !videos.isEmpty()) {
                videoAdapter = new VideoRecyclerViewAdapter(this, videos, userDao, videoDao, currentUser);
                videoRecyclerView.setAdapter(videoAdapter);
                videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                videoRecyclerView.setVisibility(View.VISIBLE);
                noResultsTextView.setVisibility(View.GONE);
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
        executorService.execute(() -> {
            List<Video> searchResults = videoDao.searchVideos("%" + query + "%");
            runOnUiThread(() -> {
                if (searchResults.isEmpty()) {
                    videoRecyclerView.setVisibility(View.GONE);
                    noResultsTextView.setText("No videos matched with '" + query + "'");
                    noResultsTextView.setVisibility(View.VISIBLE);
                } else {
                    videoAdapter.updateList(searchResults);
                    videoRecyclerView.setVisibility(View.VISIBLE);
                    noResultsTextView.setVisibility(View.GONE);
                }
                searchEditText.setVisibility(View.GONE);
                logoImageView.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SIGN_IN_REQUEST) {
                currentUser = (User) data.getSerializableExtra("currentUser");
                isSignedIn = true;
                videoAdapter.updateCurrentUser(currentUser);
                loadVideos();
            } else if (requestCode == UPLOAD_VIDEO_REQUEST) {
                loadVideos();
            } else if (requestCode == SIGN_UP_REQUEST) {
                Intent logInIntent = new Intent(MainActivity.this, LogInActivity.class);
                startActivityForResult(logInIntent, SIGN_IN_REQUEST);
            }
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
        loadVideos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}