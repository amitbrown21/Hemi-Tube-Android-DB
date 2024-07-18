package com.example.hemi_tube.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.hemi_tube.entities.Video;
import java.util.List;

@Dao
public interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Video video);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Video> videos);

    @Update
    void update(Video video);

    @Delete
    void delete(Video video);

    @Query("DELETE FROM videos WHERE id = :videoId")
    void deleteById(String videoId);

    @Query("SELECT * FROM videos")
    List<Video> getAllVideos();

    @Query("SELECT * FROM videos")
    LiveData<List<Video>> getAllVideosLive();

    @Query("SELECT * FROM videos WHERE id = :videoId")
    Video getVideoById(String videoId);

    @Query("SELECT * FROM videos WHERE id = :videoId")
    LiveData<Video> getVideoByIdLive(String videoId);

    @Query("SELECT * FROM videos WHERE ownerId = :userId")
    List<Video> getVideosForUser(String userId);

    @Query("SELECT * FROM videos WHERE ownerId = :userId")
    LiveData<List<Video>> getVideosForUserLive(String userId);

    @Query("UPDATE videos SET views = views + 1 WHERE id = :videoId")
    void incrementViews(String videoId);

    @Query("UPDATE videos SET likes = likes + 1 WHERE id = :videoId")
    void incrementLikes(String videoId);

    @Query("UPDATE videos SET likes = likes - 1 WHERE id = :videoId")
    void decrementLikes(String videoId);

    @Query("UPDATE videos SET dislikes = dislikes + 1 WHERE id = :videoId")
    void incrementDislikes(String videoId);

    @Query("UPDATE videos SET dislikes = dislikes - 1 WHERE id = :videoId")
    void decrementDislikes(String videoId);

    @Query("SELECT * FROM videos WHERE title LIKE :query")
    List<Video> searchVideos(String query);

    @Query("SELECT * FROM videos WHERE title LIKE :query")
    LiveData<List<Video>> searchVideosLive(String query);
}