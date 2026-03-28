package com.example.vocabularyapp.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.vocabularyapp.data.local.dao.LessonDao;
import com.example.vocabularyapp.data.local.dao.PostDao;
import com.example.vocabularyapp.data.local.dao.ProgressDao;
import com.example.vocabularyapp.data.local.dao.QuestionDao;
import com.example.vocabularyapp.data.local.dao.ScoreDao;
import com.example.vocabularyapp.data.local.dao.TestDao;
import com.example.vocabularyapp.data.local.dao.UserDao;
import com.example.vocabularyapp.data.local.dao.WordDao;
import com.example.vocabularyapp.data.local.entity.Lesson;
import com.example.vocabularyapp.data.local.entity.Post;
import com.example.vocabularyapp.data.local.entity.Progress;
import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Score;
import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.data.local.entity.User;
import com.example.vocabularyapp.data.local.entity.Word;

import java.util.concurrent.Executors;

@Database(entities = {User.class, Word.class, Lesson.class, Progress.class, Post.class, Test.class, Question.class, Score.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract WordDao wordDao();
    public abstract LessonDao lessonDao();
    public abstract ProgressDao progressDao();
    public abstract TestDao testDao();
    public abstract QuestionDao questionDao();
    public abstract ScoreDao scoreDao();
    public abstract PostDao postDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "vocabulary_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Chèn dữ liệu mẫu cho User để đăng nhập
                                    db.execSQL("INSERT INTO users (username, email, password, streak, rank) " +
                                            "VALUES ('user', 'user@example.com', '123', 0, 'Người mới bắt đầu')");
                                    
                                    // Đã xóa dữ liệu mẫu Words (Học tập, Giao tiếp) theo yêu cầu
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        UserDao dao = getInstance(context).userDao();
                                        if (dao.getUserByEmail("user@example.com") == null) {
                                            dao.insert(new User("user", "user@example.com", "123"));
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
