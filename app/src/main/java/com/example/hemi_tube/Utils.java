package com.example.hemi_tube;

import android.annotation.SuppressLint;

import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

import java.util.List;

public class Utils {
    public static String likeBalance(Video video) {
        int num_of_likes = video.getLikes() - video.getDislikes();
        return formatNumber(num_of_likes);
    }

    @SuppressLint("DefaultLocale")
    public static String formatNumber(int number) {
        if (number >= 1000000) {
            double millions = number / 1000000.0;
            return String.format("%.1fM", millions);
        } else if (number >= 1000) {
            double thousands = number / 1000.0;
            return String.format("%.1fK", thousands);
        } else {
            return String.valueOf(number);
        }
    }

    public static User getVideoOwner(Video video, List<User> userList) {
        String ownerId = video.getOwner();

        for (User user : userList) {
            if (user.getId() == ownerId) {
                return user;
            }
        }

        return null;
    }
}
