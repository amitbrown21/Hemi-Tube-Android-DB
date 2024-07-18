package com.example.hemi_tube.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.hemi_tube.entities.Video;
import java.util.List;

@Dao
public interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Video video);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Video> videos);

    @Update
    void update(Video video);

    @Delete
    void delete(Video video);

    @Query("DELETE FROM videos WHERE id = :videoId")
    void deleteById(int videoId);

    @Query("SELECT * FROM videos")
    List<Video> getAllVideos();

    @Query("SELECT * FROM videos")
    LiveData<List<Video>> getAllVideosLive();

    @Query("SELECT * FROM videos WHERE id = :videoId")
    Video getVideoById(int videoId);

    @Query("SELECT * FROM videos WHERE id = :videoId")
    LiveData<Video> getVideoByIdLive(int videoId);

    @Query("SELECT * FROM videos WHERE ownerId = :userId")
    List<Video> getVideosForUser(int userId);

    @Query("SELECT * FROM videos WHERE ownerId = :userId")
    LiveData<List<Video>> getVideosForUserLive(int userId);

    @Query("UPDATE videos SET views = views + 1 WHERE id = :videoId")
    void incrementViews(int videoId);

    @Query("UPDATE videos SET likes = likes + 1 WHERE id = :videoId")
    void incrementLikes(int videoId);

    @Query("UPDATE videos SET likes = likes - 1 WHERE id = :videoId")
    void decrementLikes(int videoId);

    @Query("UPDATE videos SET dislikes = dislikes + 1 WHERE id = :videoId")
    void incrementDislikes(int videoId);

    @Query("UPDATE videos SET dislikes = dislikes - 1 WHERE id = :videoId")
    void decrementDislikes(int videoId);

    @Query("SELECT * FROM videos WHERE title LIKE :query")
    List<Video> searchVideos(String query);

    @Query("SELECT * FROM videos WHERE title LIKE :query")
    LiveData<List<Video>> searchVideosLive(String query);
}