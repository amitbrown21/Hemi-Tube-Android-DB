package com.example.hemi_tube.entities;

import java.io.Serializable;
import java.util.Random;

public class CommentObj implements Serializable {
    private int id;
    private String username;
    private String body;

    CommentObj() {
    }

    public CommentObj(String username, String body) {
        this.username = username;
        this.body = body;
        Random rand = new Random();
        this.id = rand.nextInt(Integer.MAX_VALUE) + 1;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
