package com.example.hemi_tube.entities;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

public class CommentObj {
    @PrimaryKey
    @NonNull
    private String id;
    private String videoId;
    private String username;
    private String body;

    public CommentObj() {
    }

    public CommentObj(String videoId, String username, String body) {
        this.videoId = videoId;
        this.username = username;
        this.body = body;
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
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