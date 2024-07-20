package com.example.hemi_tube.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.hemi_tube.dao.CommentDao;
import com.example.hemi_tube.dao.UserDao;
import com.example.hemi_tube.dao.VideoDao;
import com.example.hemi_tube.entities.CommentObj;
import com.example.hemi_tube.entities.User;
import com.example.hemi_tube.entities.Video;

@Database(entities = {User.class, Video.class, CommentObj.class}, version = 4)
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
