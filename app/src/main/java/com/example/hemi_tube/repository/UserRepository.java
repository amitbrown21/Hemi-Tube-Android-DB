package com.example.hemi_tube.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Part;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private UserDao userDao;
    private ApiService apiService;
    private Executor executor;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        apiService = RetrofitClient.getInstance(context).getApi();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getUserById(String userId) {
        refreshUser(userId);
        return userDao.getUserByIdLive(userId);
    }

    public LiveData<User> getUserByUsername(String username) {
        refreshUserByUsername(username);
        return userDao.getUserByUsernameLive(username);
    }

    public LiveData<List<User>> getAllUsers() {
        refreshAllUsers();
        return userDao.getAllUsersLive();
    }

    public void createUser(User user, MultipartBody.Part profileImage, final RepositoryCallback<User> callback) {
        executor.execute(() -> {
            try {
                // Prepare other parts
                RequestBody firstNamePart = RequestBody.create(MultipartBody.FORM, user.getFirstName());
                RequestBody lastNamePart = RequestBody.create(MultipartBody.FORM, user.getLastName());
                RequestBody usernamePart = RequestBody.create(MultipartBody.FORM, user.getUsername());
                RequestBody passwordPart = RequestBody.create(MultipartBody.FORM, user.getPassword());
                RequestBody genderPart = RequestBody.create(MultipartBody.FORM, user.getGender());
                RequestBody subscribersPart = RequestBody.create(MultipartBody.FORM, user.getSubscribers());

                // Make the request
                Response<User> response = apiService.createUser(
                        firstNamePart, lastNamePart, usernamePart, passwordPart, genderPart, profileImage, subscribersPart
                ).execute();

                // Log the response details
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());
                if (response.errorBody() != null) {
                    Log.e(TAG, "Error body: " + response.errorBody().string());
                }

                if (response.isSuccessful() && response.body() != null) {
                    User createdUser = response.body();
                    // Set the id from the server response
                    createdUser.setId(createdUser.getId() != null ? createdUser.getId() : "");
                    userDao.insert(createdUser);
                    callback.onSuccess(createdUser);
                    Log.d(TAG, "User created successfully: " + createdUser.getId());
                } else {
                    callback.onError(new Exception("Failed to create user"));
                    Log.e(TAG, "Failed to create user: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error creating user", e);
            }
        });
    }

    public void updateUser(User user, MultipartBody.Part profileImage, final RepositoryCallback<User> callback) {
        executor.execute(() -> {
            try {
                // Prepare other parts
                RequestBody firstNamePart = RequestBody.create(MultipartBody.FORM, user.getFirstName());
                RequestBody lastNamePart = RequestBody.create(MultipartBody.FORM, user.getLastName());
                RequestBody usernamePart = RequestBody.create(MultipartBody.FORM, user.getUsername());
                RequestBody passwordPart = RequestBody.create(MultipartBody.FORM, user.getPassword());
                RequestBody genderPart = RequestBody.create(MultipartBody.FORM, user.getGender());
                RequestBody subscribersPart = RequestBody.create(MultipartBody.FORM, user.getSubscribers());

                // Log the parts being sent
                Log.d(TAG, "First Name Part: " + user.getFirstName());
                Log.d(TAG, "Last Name Part: " + user.getLastName());
                Log.d(TAG, "Username Part: " + user.getUsername());
                Log.d(TAG, "Password Part: " + user.getPassword());
                Log.d(TAG, "Gender Part: " + user.getGender());
                Log.d(TAG, "Subscribers Part: " + user.getSubscribers());
                Log.d(TAG, "Profile Image Part: " + profileImage.body().contentType().toString());

                // Make the request
                Response<User> response = apiService.updateUser(
                        user.getId(), firstNamePart, lastNamePart, usernamePart, passwordPart, genderPart, profileImage, subscribersPart
                ).execute();

                // Log the response details
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());
                if (response.errorBody() != null) {
                    Log.e(TAG, "Error body: " + response.errorBody().string());
                }

                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    // Set the id from the server response
                    updatedUser.setId(updatedUser.getId() != null ? updatedUser.getId() : "");
                    userDao.update(updatedUser);
                    callback.onSuccess(updatedUser);
                    Log.d(TAG, "User updated successfully: " + updatedUser.getId());
                } else {
                    callback.onError(new Exception("Failed to update user"));
                    Log.e(TAG, "Failed to update user: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error updating user", e);
            }
        });
    }

    public void deleteUser(String userId, final RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                Response<Void> response = apiService.deleteUser(userId).execute();
                if (response.isSuccessful()) {
                    userDao.deleteById(userId);
                    callback.onSuccess(null);
                    Log.d(TAG, "User deleted successfully: " + userId);
                } else {
                    callback.onError(new Exception("Failed to delete user"));
                    Log.e(TAG, "Failed to delete user: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error deleting user", e);
            }
        });
    }

    public void login(String username, String password, final RepositoryCallback<ApiService.LoginResponse> callback) {
        executor.execute(() -> {
            try {
                Response<ApiService.LoginResponse> response = apiService.login(new ApiService.LoginRequest(username, password)).execute();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Login successful for user: " + username);
                } else {
                    callback.onError(new Exception("Login failed"));
                    Log.e(TAG, "Login failed for user: " + username + ", message: " + response.message());
                }
            } catch (IOException e) {
                callback.onError(e);
                Log.e(TAG, "Error during login for user: " + username, e);
            }
        });
    }

    private void refreshUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "Attempted to refresh user with null or empty ID. Skipping.");
            return;
        } else {
            executor.execute(() -> {
                try {
                    Log.d(TAG, "UserRepository: Refreshing user data for ID: " + userId);
                    Response<User> response = apiService.getUserById(userId).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        // Make sure to set the id field
                        user.setId(user.getId() != null ? user.getId() : userId);
                        userDao.insert(user);
                        Log.d(TAG, "UserRepository: User refreshed successfully: " + user.toString());
                    } else {
                        Log.e(TAG, "UserRepository: Failed to refresh user. Response: " + response.message());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "UserRepository: Error refreshing user", e);
                }
            });
        }
    }

    private void refreshUserByUsername(String username) {
        executor.execute(() -> {
            try {
                Response<User> response = apiService.getUserByUsername(username).execute();
                if (response.isSuccessful() && response.body() != null) {
                    userDao.insert(response.body());
                    Log.d(TAG, "User refreshed successfully: " + username);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing user by username", e);
            }
        });
    }

    private void refreshAllUsers() {
        executor.execute(() -> {
            try {
                Response<List<User>> response = apiService.getAllUsers().execute();
                if (response.isSuccessful() && response.body() != null) {
                    userDao.insertAll(response.body());
                    Log.d(TAG, "All users refreshed successfully");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing all users", e);
            }
        });
    }
}
