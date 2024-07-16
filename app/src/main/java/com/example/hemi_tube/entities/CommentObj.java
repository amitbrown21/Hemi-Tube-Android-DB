package com.example.hemi_tube.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "comments")
public class CommentObj implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int videoId;
    private String username;
    private String body;

    public CommentObj() {
    }

    public CommentObj(int videoId, String username, String body) {
        this.videoId = videoId;
        this.username = username;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}