package com.example.hemi_tube.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "videos")
public class Video implements Serializable, Cloneable {
    @PrimaryKey
    @NonNull
    private String id;
    private String url;
    private String title;
    private String ownerId;
    private String date;
    private int views;
    private int likes;
    private int dislikes;
    private String thumbnail;
    private String description;
    private String duration;
    @Ignore
    private List<CommentObj> comments;


    public Video() {
    }

    public Video(String id, String url, String title, String ownerId, String date, int views, int likes, int dislikes, String thumbnail, String description, String duration, List<CommentObj> comments) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.ownerId = ownerId;
        this.date = date;
        this.views = views;
        this.likes = likes;
        this.dislikes = dislikes;
        this.thumbnail = thumbnail;
        this.description = description;
        this.duration = duration;
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getDate() {
        return date;
    }

    public int getViews() {
        return views;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public List<CommentObj> getComments() {
        return comments;
    }

    // Setters
    public void setViews(int views) {
        this.views = views;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void increaseViews() {
        this.views = this.views + 1;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setComments(List<CommentObj> comments) {
        this.comments = comments;
    }
    public void addComment(CommentObj newComment) {
        this.comments.add(newComment);
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", ownerId=" + ownerId +
                ", date='" + date + '\'' +
                ", views=" + views +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", thumbnail='" + thumbnail + '\'' +
                ", description='" + description + '\'' +
                ", duration='" + duration + '\'' +
                ", comments=" + comments +
                '}';
    }

    @NonNull
    @Override
    public Video clone() {
        try {
            Video clone = (Video) super.clone();

            clone.comments = new ArrayList<>(this.comments);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
