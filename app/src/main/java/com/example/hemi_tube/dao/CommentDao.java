package com.example.hemi_tube.dao;

import androidx.room.*;
import com.example.hemi_tube.entities.CommentObj;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    long insert(CommentObj comment);

    @Update
    void update(CommentObj comment);

    @Delete
    void delete(CommentObj comment);

    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY id DESC")
    List<CommentObj> getCommentsForVideo(int videoId);

    @Query("SELECT * FROM comments WHERE id = :commentId")
    CommentObj getCommentById(int commentId);

    @Query("SELECT * FROM comments WHERE username = :username")
    List<CommentObj> getCommentsByUser(String username);
}