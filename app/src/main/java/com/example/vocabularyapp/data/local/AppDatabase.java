package com.example.vocabularyapp.data.local;

import android.content.Context;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Word.class, Lesson.class, Progress.class, Post.class, Test.class, Question.class, Score.class}, version = 25)
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
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        AppDatabase instance = getInstance(context);
                                        
                                        if (instance.userDao().getUserByEmail("user@example.com") == null) {
                                            instance.userDao().insert(new User("user", "user@example.com", "123"));
                                        }

                                        db.execSQL("INSERT OR IGNORE INTO tests (id, title, description, durationMinutes, totalQuestions) " +
                                                "VALUES (100, 'Trắc nghiệm tổng hợp', '100 câu trắc nghiệm ngữ pháp & từ vựng', 20, 10)");
                                        db.execSQL("INSERT OR IGNORE INTO tests (id, title, description, durationMinutes, totalQuestions) " +
                                                "VALUES (200, 'Điền vào chỗ trống', '100 câu điền từ đúng ngữ pháp & ngữ cảnh', 20, 10)");

                                        // --- 100 Multiple Choice Questions (Test 100) ---
                                        String[][] mcData = {
                                            {"She _______ to the cinema yesterday.", "went", "goes", "has gone", "was going", "Thì quá khứ đơn với 'yesterday'."},
                                            {"If I _______ you, I would take that offer.", "were", "am", "was", "be", "Câu điều kiện loại 2."},
                                            {"He is really good _______ playing the piano.", "at", "in", "on", "with", "Good at: giỏi về..."},
                                            {"I haven''t seen him _______ 2010.", "since", "for", "during", "in", "Since + mốc thời gian."},
                                            {"The cake _______ by my mother right now.", "is being made", "is making", "makes", "was made", "Bị động hiện tại tiếp diễn."},
                                            {"She is the girl _______ father is a doctor.", "whose", "who", "whom", "which", "Whose: chỉ sở hữu."},
                                            {"I suggest _______ a taxi.", "taking", "to take", "take", "takes", "Suggest + V-ing."},
                                            {"They _______ their homework yet.", "haven''t finished", "didn''t finish", "don''t finish", "hasn''t finished", "Hiện tại hoàn thành với 'yet'."},
                                            {"It''s no use _______ about the past.", "crying", "to cry", "cry", "cried", "It''s no use + V-ing."},
                                            {"He left without _______ goodbye.", "saying", "to say", "say", "said", "Giới từ + V-ing."},
                                            {"I wish I _______ more time now.", "had", "have", "would have", "was having", "Ước ở hiện tại (loại 2)."},
                                            {"The sun _______ in the East.", "rises", "rise", "is rising", "has risen", "Sự thật hiển nhiên."},
                                            {"By the time he came, she _______.", "had left", "left", "has left", "was leaving", "Quá khứ hoàn thành."},
                                            {"He avoided _______ me in the street.", "meeting", "to meet", "meet", "meets", "Avoid + V-ing."},
                                            {"She is bored _______ her job.", "with", "at", "in", "about", "Bored with: chán nản với..."},
                                            {"I am looking forward to _______ you.", "seeing", "see", "to see", "saw", "Look forward to + V-ing."},
                                            {"The book _______ I bought is good.", "which", "who", "whom", "whose", "Which: vật."},
                                            {"He is too young _______ this movie.", "to watch", "watching", "watch", "for watching", "Too + adj + to V."},
                                            {"We _______ in this house since 2005.", "have lived", "lived", "are living", "live", "Hiện tại hoàn thành."},
                                            {"He said he _______ a doctor later.", "would be", "will be", "is", "was", "Câu tường thuật."}
                                        };

                                        for (int i = 0; i < 100; i++) {
                                            String[] d = mcData[i % mcData.length];
                                            List<String> ops = new ArrayList<>();
                                            ops.add(d[1]); ops.add(d[2]); ops.add(d[3]); ops.add(d[4]);
                                            Collections.shuffle(ops);
                                            String correct = "A";
                                            if (ops.get(1).equals(d[1])) correct = "B";
                                            else if (ops.get(2).equals(d[1])) correct = "C";
                                            else if (ops.get(3).equals(d[1])) correct = "D";

                                            db.execSQL("INSERT OR IGNORE INTO questions (id, testId, type, questionText, optionA, optionB, optionC, optionD, correctOption, explanation) VALUES (" +
                                                (i + 1) + ", 100, 'MULTIPLE_CHOICE', '" + d[0].replace("'", "''") + "', '" +
                                                ops.get(0).replace("'", "''") + "', '" + ops.get(1).replace("'", "''") + "', '" +
                                                ops.get(2).replace("'", "''") + "', '" + ops.get(3).replace("'", "''") + "', '" +
                                                correct + "', '" + d[5].replace("'", "''") + "')");
                                        }

                                        // --- 100 Fill in the Blank Questions (Test 200) ---
                                        String[][] fbData = {
                                            {"She is _______ (tốt) than her sister.", "better", "So sánh hơn của 'good' là 'better'."},
                                            {"I go to school _______ (bằng) bus.", "by", "Go by + phương tiện."},
                                            {"They are _______ (nghe) to music now.", "listening", "Hiện tại tiếp diễn: be + V-ing."},
                                            {"He _______ (không) like apples.", "doesn''t", "Phủ định hiện tại đơn chủ ngữ số ít."},
                                            {"I have been here _______ (trong) 2 hours.", "for", "For + khoảng thời gian."},
                                            {"Look! The cat is _______ (chạy).", "running", "Be + V-ing (nhân đôi phụ âm cuối)."},
                                            {"We _______ (đã đi) to Hanoi last week.", "went", "Quá khứ của 'go' là 'went'."},
                                            {"She _______ (có) a beautiful cat.", "has", "Chủ ngữ số ít đi với 'has'."},
                                            {"Please _______ (ngắt) the light.", "turn off", "Cụm động từ tắt đèn."},
                                            {"I am _______ (quan tâm) in music.", "interested", "Interested in: quan tâm..."},
                                            {"This is _______ (nhất) expensive car.", "the most", "So sánh nhất tính từ dài."},
                                            {"He _______ (có thể) swim when he was 5.", "could", "Khả năng trong quá khứ."},
                                            {"If it _______ (mưa), we will stay home.", "rains", "Câu điều kiện loại 1."},
                                            {"She is _______ (đang) reading a book.", "reading", "Hiện tại tiếp diễn."},
                                            {"They _______ (sẽ) visit us next month.", "will", "Tương lai đơn."},
                                            {"He is afraid _______ (về) dogs.", "of", "Afraid of: sợ..."},
                                            {"I _______ (ăn) breakfast at 7 AM.", "eat", "Thói quen hiện tại."},
                                            {"The car _______ (đã được) repaired yesterday.", "was", "Bị động quá khứ đơn."},
                                            {"_______ (Đừng) open the door.", "Don''t", "Câu mệnh lệnh phủ định."},
                                            {"It is _______ (quá) cold to go out.", "too", "Cấu trúc Too + adj."}
                                        };

                                        for (int i = 0; i < 100; i++) {
                                            String[] d = fbData[i % fbData.length];
                                            db.execSQL("INSERT OR IGNORE INTO questions (id, testId, type, questionText, correctOption, explanation) VALUES (" +
                                                (i + 101) + ", 200, 'FILL_BLANK', '" + d[0].replace("'", "''") + "', '" +
                                                d[1].replace("'", "''") + "', '" + d[2].replace("'", "''") + "')");
                                        }
                                        
                                        Log.d("AppDatabase", "200 questions inserted (100 MC, 100 FB)");
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
