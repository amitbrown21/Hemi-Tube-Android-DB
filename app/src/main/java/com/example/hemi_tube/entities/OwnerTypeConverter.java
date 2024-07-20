package com.example.hemi_tube.entities;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class OwnerTypeConverter {

    @TypeConverter
    public static String fromOwner(Video.Owner owner) {
        if (owner == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Video.Owner>() {}.getType();
        return gson.toJson(owner, type);
    }

    @TypeConverter
    public static Video.Owner toOwner(String ownerString) {
        if (ownerString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Video.Owner>() {}.getType();
        return gson.fromJson(ownerString, type);
    }
}
