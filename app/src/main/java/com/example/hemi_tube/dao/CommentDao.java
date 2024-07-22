package com.example.hemi_tube.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.hemi_tube.entities.CommentObj;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CommentObj comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommentObj> comments);

    @Update
    void update(CommentObj comment);

    @Query("DELETE FROM comments WHERE videoId = :videoId")
    void deleteAllCommentsForVideo(String videoId);

    @Delete
    void delete(CommentObj comment);

    @Query("DELETE FROM comments WHERE id = :commentId")
    void deleteById(String commentId);

    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY id DESC")
    List<CommentObj> getCommentsForVideo(String videoId);

    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY id DESC")
    LiveData<List<CommentObj>> getCommentsForVideoLive(String videoId);

    @Query("SELECT * FROM comments WHERE id = :commentId")
    CommentObj getCommentById(String commentId);

    @Query("SELECT * FROM comments WHERE id = :commentId")
    LiveData<CommentObj> getCommentByIdLive(String  commentId);

    @Query("SELECT * FROM comments WHERE username = :username")
    List<CommentObj> getCommentsByUser(String username);

    @Query("SELECT * FROM comments WHERE username = :username")
    LiveData<List<CommentObj>> getCommentsByUserLive(String username);
}