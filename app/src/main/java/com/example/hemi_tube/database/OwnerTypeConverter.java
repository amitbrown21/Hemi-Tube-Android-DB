package com.example.hemi_tube.database;

import androidx.room.TypeConverter;

import com.example.hemi_tube.entities.Video;
import com.google.gson.Gson;

public class OwnerTypeConverter {
    @TypeConverter
    public static Video.Owner fromString(String value) {
        if (value == null) return null;
        return new Gson().fromJson(value, Video.Owner.class);
    }

    @TypeConverter
    public static String toString(Video.Owner owner) {
        if (owner == null) return null;
        return new Gson().toJson(owner);
    }
}
