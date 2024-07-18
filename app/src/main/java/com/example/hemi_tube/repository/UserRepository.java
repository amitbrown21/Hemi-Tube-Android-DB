package com.example.hemi_tube.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.database.AppDatabase;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.network.RetrofitClient;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Response;

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

    public LiveData<User> getUserById(int userId) {
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

    public void createUser(User user, final RepositoryCallback<User> callback) {
        executor.execute(() -> {
            try {
                Response<User> response = apiService.createUser(user).execute();
                if (response.isSuccessful() && response.body() != null) {
                    User createdUser = response.body();
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

    public void updateUser(User user, final RepositoryCallback<User> callback) {
        executor.execute(() -> {
            try {
                Response<User> response = apiService.updateUser(user.getId(), user).execute();
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
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

    public void deleteUser(int userId, final RepositoryCallback<Void> callback) {
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

    private void refreshUser(int userId) {
        executor.execute(() -> {
            try {
                Response<User> response = apiService.getUserById(userId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    userDao.insert(response.body());
                    Log.d(TAG, "User refreshed successfully: " + userId);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error refreshing user", e);
            }
        });
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
