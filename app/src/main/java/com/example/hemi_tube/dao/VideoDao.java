package com.example.hemi_tube.dao;

import androidx.room.*;
import com.example.hemi_tube.entities.Video;
import java.util.List;

@Dao
public interface VideoDao {
    @Insert
    long insert(Video video);

    @Update
    void update(Video video);

    @Delete
    void delete(Video video);

    @Query("SELECT * FROM videos")
    List<Video> getAllVideos();

    @Query("SELECT * FROM videos WHERE id = :videoId")
    Video getVideoById(int videoId);

    @Query("SELECT * FROM videos WHERE ownerId = :userId")
    List<Video> getVideosForUser(int userId);

    @Query("UPDATE videos SET views = views + 1 WHERE id = :videoId")
    void incrementViews(int videoId);

    @Query("UPDATE videos SET likes = likes + 1 WHERE id = :videoId")
    void incrementLikes(int videoId);

    @Query("UPDATE videos SET dislikes = dislikes + 1 WHERE id = :videoId")
    void incrementDislikes(int videoId);

    @Query("SELECT * FROM videos WHERE title LIKE :query")
    List<Video> searchVideos(String query);
}