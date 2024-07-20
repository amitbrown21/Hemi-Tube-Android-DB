package com.example.hemi_tube.viewmodel;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.network.ApiService;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.repository.UserRepository;

import java.util.List;

import okhttp3.MultipartBody;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<User> getUserById(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "Attempted to get user with null or empty ID");
            return new MutableLiveData<>(null);
        }
        return userRepository.getUserById(userId);
    }

    public LiveData<User> getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public void createUser(User user, MultipartBody.Part profileImage, RepositoryCallback<User> callback) {
        userRepository.createUser(user, profileImage, callback);
    }


    public void updateUser(User user, RepositoryCallback<User> callback) {
        userRepository.updateUser(user, callback);
    }

    public void deleteUser(String userId, RepositoryCallback<Void> callback) {
        userRepository.deleteUser(userId, callback);
    }

    public void login(String username, String password, RepositoryCallback<ApiService.LoginResponse> callback) {
        userRepository.login(username, password, callback);
    }
}