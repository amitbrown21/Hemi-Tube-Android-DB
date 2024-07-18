package com.example.hemi_tube.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.repository.UserRepository;
import com.example.hemi_tube.repository.RepositoryCallback;
import com.example.hemi_tube.network.ApiService;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<User> getUserById(String userId) {
        return userRepository.getUserById(userId);
    }

    public LiveData<User> getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public void createUser(User user, RepositoryCallback<User> callback) {
        userRepository.createUser(user, callback);
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