package com.example.hemi_tube.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.hemi_tube.entities.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteById(int userId);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsersLive();

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(int userId);

    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<User> getUserByIdLive(int userId);

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username")
    LiveData<User> getUserByUsernameLive(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User getUserForLogin(String username, String password);

    @Query("UPDATE users SET subscribers = subscribers + 1 WHERE id = :userId")
    void incrementSubscribers(int userId);

    @Query("UPDATE users SET subscribers = subscribers - 1 WHERE id = :userId")
    void decrementSubscribers(int userId);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean isUsernameExists(String username);

    @Query("UPDATE users SET profilePicture = :profilePicturePath WHERE id = :userId")
    void updateProfilePicture(int userId, String profilePicturePath);
}