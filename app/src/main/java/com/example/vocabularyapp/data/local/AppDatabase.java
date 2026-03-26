package com.example.vocabularyapp.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.vocabularyapp.data.local.dao.LessonDao;
import com.example.vocabularyapp.data.local.dao.ProgressDao;
import com.example.vocabularyapp.data.local.dao.UserDao;
import com.example.vocabularyapp.data.local.dao.WordDao;
import com.example.vocabularyapp.data.local.entity.Lesson;
import com.example.vocabularyapp.data.local.entity.Post;
import com.example.vocabularyapp.data.local.entity.Progress;
import com.example.vocabularyapp.data.local.entity.User;
import com.example.vocabularyapp.data.local.entity.Word;

@Database(entities = {User.class, Word.class, Lesson.class, Progress.class, Post.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract WordDao wordDao();
    public abstract LessonDao lessonDao();
    public abstract ProgressDao progressDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "vocabulary_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
