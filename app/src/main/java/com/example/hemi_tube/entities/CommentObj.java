package com.example.hemi_tube.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


@Entity(tableName = "comments")
public class CommentObj implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("_id")
    private String id;
    private String videoId;
    private String username;
    private String body;
    private String profilePicture;
    private String userId;

    public CommentObj() {
    }

    public CommentObj(String id, String videoId, String username, String body, String profilePicture, String userId) {
        this.id = id;
        this.videoId = videoId;
        this.username = username;
        this.body = body;
        this.profilePicture = profilePicture;
        this.userId = userId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", videoId='" + videoId + '\'' +
                ", username='" + username + '\'' +
                ", body=" + body +
                ", profilePicture=" + profilePicture +
                ", userId=" + userId +
                '}';
    }
}
