package com.example.hemi_tube.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.hemi_tube.dao.*;
import com.example.hemi_tube.entities.*;

@Database(entities = {User.class, Video.class, CommentObj.class}, version = 22)
@TypeConverters({OwnerTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract VideoDao videoDao();
    public abstract CommentDao commentDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "hemi_tube_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}