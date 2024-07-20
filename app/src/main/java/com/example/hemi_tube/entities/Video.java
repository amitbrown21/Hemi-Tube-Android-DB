package com.example.hemi_tube.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "videos")
public class Video implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    @SerializedName("_id")
    private String id;

    private String url;
    private String title;

    @SerializedName("owner")
    private Owner owner; // Change to Owner class

    private int views;
    private int likes;
    private int dislikes;
    private String thumbnail;
    private String description;
    private String duration;
    private String date;

    @Ignore
    private List<CommentObj> comments;

    public Video() {
    }

    public Video(@NonNull String id, String url, String title, Owner owner, int views, int likes, int dislikes, String thumbnail, String description, String duration, String date, List<CommentObj> comments) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.owner = owner;
        this.views = views;
        this.likes = likes;
        this.dislikes = dislikes;
        this.thumbnail = thumbnail;
        this.description = description;
        this.duration = duration;
        this.date = date;
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    // Getters and Setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CommentObj> getComments() {
        return comments;
    }

    public void setComments(List<CommentObj> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", owner=" + owner +
                ", views=" + views +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", thumbnail='" + thumbnail + '\'' +
                ", description='" + description + '\'' +
                ", duration='" + duration + '\'' +
                ", date='" + date + '\'' +
                ", comments=" + comments +
                '}';
    }

    // Owner class to match the owner object in JSON response
    public static class Owner implements Serializable {

        public Owner() {}

        public Owner(String id, String username) {
            this.id = id;
            this.username = username;
        }

        @SerializedName("_id")
        private String id;

        @SerializedName("username")
        private String username;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "Owner{" +
                    "id='" + id + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
