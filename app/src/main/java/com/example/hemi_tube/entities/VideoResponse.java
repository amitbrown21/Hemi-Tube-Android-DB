package com.example.hemi_tube.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoResponse {
    @SerializedName("topVideos")
    private List<Video> topVideos;

    @SerializedName("otherVideos")
    private List<Video> otherVideos;

    public List<Video> getTopVideos() {
        return topVideos;
    }

    public void setTopVideos(List<Video> topVideos) {
        this.topVideos = topVideos;
    }

    public List<Video> getOtherVideos() {
        return otherVideos;
    }

    public void setOtherVideos(List<Video> otherVideos) {
        this.otherVideos = otherVideos;
    }
}
