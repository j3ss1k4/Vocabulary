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

@Database(entities = {User.class, Word.class, Lesson.class, Progress.class, Post.class, Test.class, Question.class, Score.class}, version = 80)
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
                                    seedData(db);
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Kiểm tra và nạp lại nếu dữ liệu bị mất
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        try {
                                            android.database.Cursor cursor = db.query("SELECT COUNT(*) FROM tests");
                                            cursor.moveToFirst();
                                            int count = cursor.getInt(0);
                                            cursor.close();
                                            if (count == 0) {
                                                Log.d("AppDatabase", "Database empty on open, seeding data...");
                                                seedData(db);
                                            }
                                        } catch (Exception e) {
                                            Log.e("AppDatabase", "Error checking data: " + e.getMessage());
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

    private static void seedData(SupportSQLiteDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.beginTransaction();
            try {
                // FIX: Sử dụng đúng cột username của entity User
                db.execSQL("INSERT OR IGNORE INTO users (username, email, password, streak, rank) " +
                        "VALUES ('user', 'user@example.com', '123', 0, 'Người mới bắt đầu')");

                // Chèn thông tin bài Test
                db.execSQL("INSERT OR IGNORE INTO tests (id, title, description, durationMinutes, totalQuestions) VALUES (100, 'Multiple Choice Quiz', 'Grammar and Vocabulary', 20, 100)");
                db.execSQL("INSERT OR IGNORE INTO tests (id, title, description, durationMinutes, totalQuestions) VALUES (200, 'Fill-in-the-Blanks', 'Verb forms and structures', 20, 100)");
                db.execSQL("INSERT OR IGNORE INTO tests (id, title, description, durationMinutes, totalQuestions) VALUES (300, 'Listening Multiple Choice', 'Listen and answer', 20, 100)");
                db.execSQL("INSERT OR IGNORE INTO tests (id, title, description, durationMinutes, totalQuestions) VALUES (400, 'Dictation Practice', 'Listen and type', 20, 100)");

                // --- TEST 100: Multiple Choice ---
                String[][] mcBase = {
                        {"She _______ to school every day.", "goes", "go", "went", "gone", "Thì hiện tại đơn diễn tả thói quen hàng ngày."},
                        {"I _______ a movie when the phone rang.", "was watching", "watched", "watch", "am watching", "Quá khứ tiếp diễn diễn tả hành động đang xảy ra thì bị hành động khác cắt ngang."},
                        {"If I _______ you, I would take the job.", "were", "am", "was", "be", "Câu điều kiện loại 2 giả định trái thực tế hiện tại (dùng 'were' cho mọi ngôi)."},
                        {"The book _______ by my mother yesterday.", "was bought", "bought", "is bought", "has bought", "Bị động quá khứ đơn: S + was/were + V3/ed."},
                        {"He is good _______ playing soccer.", "at", "in", "on", "for", "Cấu trúc: Good at + V-ing (Giỏi làm việc gì)."},
                        {"I haven't seen her _______ 2015.", "since", "for", "during", "at", "Dùng 'Since' + mốc thời gian trong thì hiện tại hoàn thành."},
                        {"They _______ lunch right now.", "are having", "have", "has", "had", "Hành động đang diễn ra tại thời điểm nói (Hiện tại tiếp diễn)."},
                        {"She is the girl _______ lives next door.", "who", "whom", "which", "whose", "Đại từ quan hệ 'Who' thay thế cho người làm chủ ngữ."},
                        {"I suggest _______ a taxi.", "taking", "to take", "take", "takes", "Cấu trúc: Suggest + V-ing (Gợi ý cùng làm gì)."},
                        {"He said he _______ be late.", "would", "will", "shall", "can", "Lùi thì 'Will' thành 'Would' trong câu tường thuật."},
                        {"They _______ each other since they were children.", "have known", "know", "knew", "are knowing", "Hiện tại hoàn thành diễn tả hành động kéo dài từ quá khứ đến nay."},
                        {"By the time I arrived, the train _______.", "had left", "left", "has left", "was leaving", "Quá khứ hoàn thành diễn tả hành động xảy ra trước hành động khác trong quá khứ."},
                        {"Water _______ at 100 degrees Celsius.", "boils", "boil", "is boiling", "boiled", "Hiện tại đơn diễn tả sự thật hiển nhiên/chân lý."},
                        {"Listen! Someone _______ the piano.", "is playing", "plays", "played", "has played", "Hiện tại tiếp diễn diễn tả hành động đang xảy ra (dấu hiệu: Listen!)."},
                        {"I _______ my homework yet.", "haven't finished", "didn't finish", "don't finish", "finish", "Yet dùng trong câu phủ định thì hiện tại hoàn thành."},
                        {"This time tomorrow, we _______ on the beach.", "will be lying", "will lie", "are lying", "lie", "Tương lai tiếp diễn diễn tả hành động đang diễn ra tại thời điểm xác định trong tương lai."},
                        {"The sun _______ in the East.", "rises", "rise", "is rising", "rose", "Sự thật hiển nhiên dùng thì hiện tại đơn."},
                        {"How long _______ English?", "have you been learning", "do you learn", "did you learn", "are you learning", "Hiện tại hoàn thành tiếp diễn nhấn mạnh sự liên tục của hành động kéo dài."},
                        {"I _______ to Paris three times so far.", "have been", "was", "am", "went", "Nói về trải nghiệm tính đến hiện tại dùng thì hiện tại hoàn thành."},
                        {"Yesterday at 6 PM, I _______ dinner.", "was having", "had", "have", "am having", "Quá khứ tiếp diễn diễn tả hành động đang diễn ra tại thời điểm cụ thể trong quá khứ."},
                        {"Neither the teacher nor the students _______ here.", "are", "is", "be", "been", "Cấu trúc Neither... nor... chia theo chủ ngữ gần nhất (students)."},
                        {"The number of students _______ increasing.", "is", "are", "be", "were", "The number of + N số nhiều + V số ít."},
                        {"A number of cars _______ parked outside.", "are", "is", "was", "be", "A number of + N số nhiều + V số nhiều."},
                        {"Physics _______ my favorite subject.", "is", "are", "be", "were", "Tên môn học kết thúc bằng 's' vẫn chia số ít."},
                        {"The news _______ very shocking.", "is", "are", "be", "were", "News là danh từ không đếm được, chia số ít."},
                        {"Someone _______ at the door.", "is", "are", "be", "were", "Đại từ bất định (Someone, Anyone...) luôn chia số ít."},
                        {"Ten miles _______ a long distance to walk.", "is", "are", "be", "were", "Đơn vị đo lường (khoảng cách, thời gian, tiền bạc) chia số ít."},
                        {"The police _______ investigating the crime.", "are", "is", "was", "has", "Police là danh từ tập hợp nhưng luôn chia số nhiều."},
                        {"Bread and butter _______ my daily breakfast.", "is", "are", "be", "were", "Hai danh từ chỉ một món ăn/kết hợp quen thuộc → chia số ít."},
                        {"Everything _______ ready for the party.", "is", "are", "be", "were", "Everything/Something/Anything luôn chia số ít."},
                        {"I _______ a new car next month.", "am going to buy", "bought", "buy", "have bought", "Dự định, kế hoạch trong tương lai dùng Be going to."},
                        {"While I _______, the lights went out.", "was reading", "read", "am reading", "have read", "Quá khứ tiếp diễn diễn tả hành động đang xảy ra thì bị hành động khác cắt ngang."},
                        {"She _______ always _______ her keys!", "is/losing", "does/lose", "has/lost", "was/lost", "Hiện tại tiếp diễn với 'always' dùng để phàn nàn về thói quen xấu."},
                        {"We _______ dinner when it started to rain.", "were having", "had", "have", "are having", "Quá khứ tiếp diễn diễn tả hành động đang diễn ra trong quá khứ."},
                        {"I _______ her for ages.", "haven't seen", "didn't see", "don't see", "am not seeing", "Dấu hiệu 'for ages' dùng thì hiện tại hoàn thành."},
                        {"He _______ his keys. He can't get in.", "has lost", "lost", "loses", "is losing", "Hiện tại hoàn thành diễn tả hành động vừa xảy ra để lại kết quả ở hiện tại."},
                        {"By next year, I _______ from university.", "will have graduated", "will graduate", "graduate", "am graduating", "Tương lai hoàn thành diễn tả hành động hoàn thành trước một mốc thời gian trong tương lai."},
                        {"It _______ since early morning.", "has been raining", "rains", "is raining", "rained", "Hiện tại hoàn thành tiếp diễn: hành động bắt đầu ở quá khứ và kéo dài liên tục đến hiện tại."},
                        {"When I was young, I _______ to the zoo every Sunday.", "went", "go", "have gone", "was going", "Diễn tả thói quen hoặc hành động lặp lại trong quá khứ dùng quá khứ đơn."},
                        {"They _______ the house by the time we arrive.", "will have cleaned", "clean", "cleaned", "are cleaning", "Tương lai hoàn thành: hoàn thành trước một hành động khác ở tương lai."},
                        {"The man _______ stole my bag was caught.", "who", "whom", "which", "whose", "Who làm chủ ngữ thay thế cho người."},
                        {"The book _______ you lent me is great.", "which", "who", "whom", "whose", "Which thay thế cho vật làm tân ngữ."},
                        {"The woman _______ daughter is my friend.", "whose", "who", "whom", "which", "Whose chỉ sự sở hữu cho cả người và vật."},
                        {"I know a place _______ we can have lunch.", "where", "when", "which", "who", "Where thay thế cho nơi chốn."},
                        {"2020 was the year _______ everything changed.", "when", "where", "which", "who", "When thay thế cho thời gian."},
                        {"The cake _______ by Mary was delicious.", "made", "make", "making", "was made", "Rút gọn mệnh đề quan hệ dạng bị động (V3/ed)."},
                        {"The students _______ in the exam will be punished.", "cheating", "cheat", "cheated", "to cheat", "Rút gọn mệnh đề quan hệ chủ động: who are cheating → cheating (V-ing)."},
                        {"This bridge _______ in 1995.", "was built", "built", "is built", "has been built", "Bị động quá khứ đơn (mốc thời gian cụ thể trong quá khứ)."},
                        {"Rice _______ in many Asian countries.", "is grown", "grows", "is growing", "was grown", "Bị động hiện tại đơn diễn tả sự thật hiển nhiên."},
                        {"A new hospital _______ in this area next year.", "will be built", "will build", "is built", "built", "Bị động tương lai đơn (S + will be + V3/ed)."},
                        {"My phone _______.", "has been stolen", "was stealing", "stole", "is stealing", "Bị động hiện tại hoàn thành (hành động vừa xảy ra không rõ thời gian)."},
                        {"The house _______ at the moment.", "is being painted", "is painting", "was painted", "has been painted", "Bị động hiện tại tiếp diễn (S + am/is/are + being + V3/ed)."},
                        {"He is said _______ a genius.", "to be", "being", "is", "was", "Bị động khách quan: S + be + said/thought... + to V."},
                        {"The flowers _______ every morning.", "are watered", "water", "is watered", "watered", "Bị động hiện tại đơn với chủ ngữ số nhiều."},
                        {"The report _______ by the end of the day.", "must be finished", "must finish", "must be finishing", "must finished", "Bị động với động từ khuyết thiếu: Modal + be + V3/ed."},
                        {"The car _______ yesterday.", "was repaired", "repaired", "is repaired", "has been repaired", "Bị động quá khứ đơn."},
                        {"I had my hair _______ yesterday.", "cut", "cutting", "to cut", "was cut", "Cấu trúc truyền khiến: Have something done (V3/ed)."},
                        {"They got the roof _______.", "fixed", "fixing", "to fix", "was fixed", "Cấu trúc truyền khiến: Get something done (V3/ed)."},
                        {"The children _______ to the zoo by their parents.", "were taken", "took", "take", "are taking", "Bị động quá khứ đơn với chủ ngữ số nhiều."},
                        {"Everything _______ before the guests arrived.", "had been prepared", "prepared", "has been prepared", "was prepared", "Bị động quá khứ hoàn thành (xảy ra trước một hành động quá khứ khác)."},
                        {"The message _______ to you later.", "will be sent", "sends", "is sent", "sent", "Bị động tương lai đơn."},
                        {"The trees _______ by the storm.", "were blown down", "blew down", "blown down", "are blowing down", "Bị động quá khứ đơn."},
                        {"It _______ that the company is closing.", "is rumored", "rumors", "was rumored", "is rumor", "Bị động khách quan: It + be + V3/ed + that..."},
                        {"He was seen _______ the building.", "leaving", "leave", "left", "to leave", "Sau động từ tri giác ở bị động: thường dùng V-ing để nhấn mạnh hành động đang diễn ra."},
                        {"English _______ as a second language here.", "is taught", "teaches", "taught", "is teaching", "Bị động hiện tại đơn."},
                        {"If it rains, we _______ the match.", "will cancel", "would cancel", "canceled", "cancel", "Câu điều kiện loại 1: Có thể xảy ra ở hiện tại hoặc tương lai."},
                        {"If I _______ you, I would tell the truth.", "were", "am", "was", "be", "Câu điều kiện loại 2: Giả định trái thực tế ở hiện tại."},
                        {"If she _______ harder, she would have passed.", "had studied", "studied", "studies", "has studied", "Câu điều kiện loại 3: Giả định trái thực tế trong quá khứ."},
                        {"I wish I _______ more money.", "had", "have", "am having", "will have", "Câu ước ở hiện tại: Wish + S + V2/ed."},
                        {"If only he _______ here now.", "were", "is", "was", "be", "If only tương đương với Wish (ước ở hiện tại dùng quá khứ đơn)."},
                        {"Unless you _______ now, you will be late.", "leave", "don't leave", "left", "will leave", "Unless = If not (Nếu không làm... thì sẽ...)."},
                        {"Suppose you _______ a million dollars, what would you do?", "won", "win", "will win", "had won", "Giả định loại 2 (trái thực tế hiện tại)."},
                        {"It's time you _______ home.", "went", "go", "going", "to go", "Cấu trúc: It's time + S + V2/ed (Đã đến lúc làm gì)."},
                        {"I'd rather you _______ smoke here.", "didn't", "don't", "not", "won't", "Cấu trúc: S1 + would rather + S2 + V2/ed (Ước cho người khác)."},
                        {"He behaves as if he _______ the boss.", "were", "is", "was", "be", "As if/As though: Diễn tả điều giả định không có thật ở hiện tại."},
                        {"I wish I _______ to the party last night.", "had gone", "went", "go", "have gone", "Câu ước ở quá khứ: Wish + S + V-quá khứ hoàn thành."},
                        {"If the weather _______ fine tomorrow, we will go camping.", "is", "will be", "was", "were", "Mệnh đề If loại 1 dùng thì hiện tại đơn."},
                        {"Provided that you _______ hard, you will succeed.", "work", "will work", "worked", "working", "Provided that/As long as tương đương với If."},
                        {"What _______ if you were in my shoes?", "would you do", "will you do", "do you do", "did you do", "Câu hỏi điều kiện loại 2."},
                        {"Had I known the truth, I _______ differently.", "would have acted", "will act", "acted", "would act", "Đảo ngữ câu điều kiện loại 3 (Had + S + V3/ed)."},
                        {"I enjoy _______ football.", "playing", "to play", "play", "played", "Cấu trúc: Enjoy + V-ing."},
                        {"He promised _______ me back.", "to call", "calling", "call", "called", "Cấu trúc: Promise + To V."},
                        {"I am looking forward to _______ you.", "seeing", "see", "to see", "seen", "Cấu trúc: Look forward to + V-ing."},
                        {"It's no use _______ over spilled milk.", "crying", "to cry", "cry", "cried", "Cấu trúc: It's no use + V-ing."},
                        {"She suggested _______ for a walk.", "going", "to go", "go", "goes", "Cấu trúc: Suggest + V-ing."},
                        {"I forgot _______ the door.", "to lock", "locking", "lock", "locked", "Forget + To V: Quên phải làm gì (chưa làm)."},
                        {"He stopped _______ because he was tired.", "working", "to work", "work", "worked", "Stop + V-ing: Dừng hẳn việc đang làm."},
                        {"They managed _______ the mountain.", "to climb", "climbing", "climb", "climbed", "Cấu trúc: Manage + To V."},
                        {"I'm used to _______ up early.", "getting", "get", "to get", "got", "Cấu trúc: Be used to + V-ing (Quen với việc gì)."},
                        {"He is afraid _______ spiders.", "of", "in", "at", "on", "Cấu trúc: Afraid of + N/V-ing."},
                        {"She is interested _______ learning English.", "in", "at", "on", "for", "Cấu trúc: Interested in + N/V-ing."},
                        {"Congratulations _______ your success!", "on", "in", "at", "for", "Cấu trúc: Congratulate someone on something."},
                        {"She is famous _______ her beautiful voice.", "for", "in", "at", "with", "Cấu trúc: Famous for something."},
                        {"The table is made _______ wood.", "of", "from", "by", "in", "Made of: Làm từ vật liệu (không đổi bản chất)."},
                        {"I'm bored _______ doing the same thing.", "with", "at", "in", "on", "Cấu trúc: Bored with something."},
                        {"He apologized _______ being late.", "for", "in", "at", "on", "Cấu trúc: Apologize for something."},
                        {"Please pay attention _______ the teacher.", "to", "in", "at", "on", "Cấu trúc: Pay attention to something."},
                        {"She is capable _______ doing it herself.", "of", "in", "at", "on", "Cấu trúc: Capable of + V-ing."},
                        {"Depend _______ yourself, not others.", "on", "in", "at", "to", "Cấu trúc: Depend on someone/something."},
                        {"I'm not familiar _______ this area.", "with", "to", "at", "in", "Cấu trúc: Familiar with something."}
                };
                for (int i = 0; i < mcBase.length; i++) {
                    String[] d = mcBase[i];
                    List<String> ops = new ArrayList<>();
                    ops.add(d[1]); ops.add(d[2]); ops.add(d[3]); ops.add(d[4]);
                    Collections.shuffle(ops);
                    String correct = "A";
                    if (ops.get(1).equals(d[1])) correct = "B";
                    else if (ops.get(2).equals(d[1])) correct = "C";
                    else if (ops.get(3).equals(d[1])) correct = "D";
                    db.execSQL("INSERT OR IGNORE INTO questions (testId, type, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, audioPath) VALUES (100, 'MULTIPLE_CHOICE', '" + d[0].replace("'", "''") + "', '" + ops.get(0).replace("'", "''") + "', '" + ops.get(1).replace("'", "''") + "', '" + ops.get(2).replace("'", "''") + "', '" + ops.get(3).replace("'", "''") + "', '" + correct + "', '" + d[5].replace("'", "''") + "', '')");
                }

                // --- TEST 200: Fill in the Blanks ---
                String[][] fbBase = {
                        {"He _______ (not/like) apples.", "doesn't like", "Phủ định hiện tại đơn với chủ ngữ số ít (He/She/It)."},
                        {"We _______ (visit) Hanoi last week.", "visited", "Quá khứ đơn diễn tả hành động đã kết thúc (last week)."},
                        {"Look! The cat _______ (run).", "is running", "Hành động đang diễn ra tại thời điểm nói (có dấu hiệu Look!)."},
                        {"They _______ (live) here since 2010.", "have lived", "Hiện tại hoàn thành diễn tả hành động kéo dài từ quá khứ đến nay."},
                        {"If it _______ (rain), we'll stay home.", "rains", "Câu điều kiện loại 1: Mệnh đề If chia hiện tại đơn."},
                        {"I am _______ (interest) in music.", "interested", "Cấu trúc: Be interested in + N/V-ing (Cảm thấy hứng thú)."},
                        {"She is _______ (tall) than her brother.", "taller", "So sánh hơn với tính từ ngắn: Adj-er + than."},
                        {"The car _______ (repair) yesterday.", "was repaired", "Bị động quá khứ đơn: S + was/were + V3/ed."},
                        {"You had better _______ (go) now.", "go", "Cấu trúc: Had better + V nguyên mẫu (Nên làm gì)."},
                        {"I _______ (see) that movie already.", "have seen", "Hiện tại hoàn thành diễn tả hành động đã hoàn thành (already)."},
                        {"Water _______ (boil) at 100 degrees Celsius.", "boils", "Sự thật hiển nhiên/Chân lý dùng hiện tại đơn."},
                        {"My father _______ (work) in this factory for 20 years.", "has worked", "Hiện tại hoàn thành diễn tả hành động kéo dài đến hiện tại."},
                        {"I _______ (not/finish) my homework yet.", "haven't finished", "Dấu hiệu 'Yet' dùng trong câu phủ định thì hiện tại hoàn thành."},
                        {"By the time we arrived, the film _______ (start).", "had started", "Quá khứ hoàn thành diễn tả hành động xảy ra trước một hành động quá khứ khác."},
                        {"Listen! Someone _______ (knock) at the door.", "is knocking", "Hành động đang xảy ra tại thời điểm nói (Listen!)."},
                        {"They _______ (watch) TV when I came.", "were watching", "Quá khứ tiếp diễn diễn tả hành động đang diễn ra thì bị hành động khác cắt ngang."},
                        {"I promise I _______ (help) you with your project.", "will help", "Lời hứa hoặc quyết định tại thời điểm nói dùng tương lai đơn."},
                        {"She _______ (always/lose) her keys!", "is always losing", "Hiện tại tiếp diễn với 'Always' phàn nàn về một thói quen xấu."},
                        {"How long _______ (you/learn) English?", "have you been learning", "Hiện tại hoàn thành tiếp diễn nhấn mạnh hành động kéo dài liên tục đến hiện tại."},
                        {"Yesterday, we _______ (meet) our old teacher in the park.", "met", "Hành động đã xảy ra và kết thúc hoàn toàn trong quá khứ (Yesterday)."},
                        {"The sun _______ (rise) in the East.", "rises", "Sự thật hiển nhiên dùng hiện tại đơn."},
                        {"I _______ (be) to Paris three times so far.", "have been", "Nói về trải nghiệm tính đến thời điểm hiện tại (so far)."},
                        {"We _______ (have) dinner at 7 PM yesterday when you called.", "were having", "Quá khứ tiếp diễn bị hành động khác xen vào."},
                        {"This time tomorrow, I _______ (sit) on the plane.", "will be sitting", "Tương lai tiếp diễn diễn tả hành động đang xảy ra tại thời điểm xác định ở tương lai."},
                        {"Be quiet! The baby _______ (sleep).", "is sleeping", "Hành động đang xảy ra tại thời điểm nói (Be quiet!)."},
                        {"I _______ (not/see) her since we left school.", "haven't seen", "Cấu trúc: Hiện tại hoàn thành + since + Quá khứ đơn."},
                        {"He _______ (just/leave) the office.", "has just left", "Hiện tại hoàn thành diễn tả hành động vừa mới xảy ra."},
                        {"We _______ (study) for the exam all night.", "have been studying", "Hiện tại hoàn thành tiếp diễn nhấn mạnh tính liên tục và kéo dài của hành động."},
                        {"The train _______ (leave) at 8:00 AM every morning.", "leaves", "Lịch trình, thời khóa biểu dùng hiện tại đơn."},
                        {"I _______ (think) about you when you called.", "was thinking", "Hành động đang diễn ra trong quá khứ thì bị hành động khác cắt ngang."},
                        {"Someone _______ (steal) my bicycle last night.", "stole", "Quá khứ đơn diễn tả hành động đã kết thúc (last night)."},
                        {"If I _______ (have) a lot of money, I would buy a car.", "had", "Câu điều kiện loại 2: Giả định không có thật ở hiện tại."},
                        {"They _______ (play) soccer in the rain now.", "are playing", "Hiện tại tiếp diễn với dấu hiệu 'now'."},
                        {"I _______ (never/be) to America before.", "have never been", "Trải nghiệm tính đến hiện tại (dùng never)."},
                        {"She _______ (study) French for five years now.", "has been studying", "Nhấn mạnh sự kéo dài và liên tục của hành động đến hiện tại."},
                        {"The plane _______ (take) off in 10 minutes.", "takes", "Lịch trình cố định (timetable) dùng hiện tại đơn."},
                        {"I _______ (not/eat) anything since breakfast.", "haven't eaten", "Hành động kéo dài từ một mốc quá khứ đến hiện tại."},
                        {"What _______ (you/do) at this time last Sunday?", "were you doing", "Hành động đang diễn ra tại thời điểm cụ thể trong quá khứ."},
                        {"By next year, he _______ (graduate) from university.", "will have graduated", "Tương lai hoàn thành: hoàn thành trước một mốc thời gian ở tương lai."},
                        {"The earth _______ (go) around the sun.", "goes", "Chân lý hiển nhiên dùng hiện tại đơn."},
                        {"English _______ (speak) all over the world.", "is spoken", "Bị động hiện tại đơn (S + am/is/are + V3/ed)."},
                        {"The house _______ (build) in 1995.", "was built", "Bị động quá khứ đơn (mốc thời gian 1995)."},
                        {"Rice _______ (grow) in Asian countries.", "is grown", "Bị động hiện tại đơn diễn tả sự thật."},
                        {"The window _______ (break) by the wind yesterday.", "was broken", "Bị động quá khứ đơn."},
                        {"A new school _______ (build) in this area next year.", "will be built", "Bị động tương lai đơn (S + will be + V3/ed)."},
                        {"The room _______ (clean) at the moment.", "is being cleaned", "Bị động hiện tại tiếp diễn (S + am/is/are + being + V3/ed)."},
                        {"The report _______ (finish) by the end of this week.", "will have been finished", "Bị động tương lai đơn/hoàn thành."},
                        {"My phone _______ (steal) while I was on the bus.", "was stolen", "Bị động quá khứ đơn."},
                        {"Trees _______ (plant) along the street recently.", "have been planted", "Bị động hiện tại hoàn thành (dấu hiệu recently)."},
                        {"The dinner _______ (prepare) when we got home.", "was being prepared", "Bị động quá khứ tiếp diễn (đang được chuẩn bị)."},
                        {"The man _______ (stand) over there is my uncle.", "standing", "Rút gọn mệnh đề quan hệ dạng chủ động: dùng V-ing."},
                        {"The book _______ (write) by Nam Cao is famous.", "written", "Rút gọn mệnh đề quan hệ dạng bị động: dùng V3/ed."},
                        {"I saw the girl _______ (help) the old man.", "helping", "Cấu trúc: See + O + V-ing (thấy một phần hành động)."},
                        {"The bridge _______ (destroy) in the war has been rebuilt.", "destroyed", "Rút gọn mệnh đề quan hệ dạng bị động."},
                        {"That is the house _______ (where) I was born.", "where", "Trạng từ quan hệ 'Where' thay thế cho nơi chốn."},
                        {"Do you know the boy _______ (who) is talking to Lan?", "who", "Đại từ quan hệ 'Who' làm chủ ngữ chỉ người."},
                        {"The car _______ (which) he bought is very expensive.", "which", "Đại từ quan hệ 'Which' thay thế cho vật."},
                        {"The girl _______ (whose) father is a doctor is my classmate.", "whose", "Đại từ quan hệ 'Whose' chỉ sự sở hữu."},
                        {"This is the most beautiful place I _______ (ever/visit).", "have ever visited", "Hiện tại hoàn thành đi kèm với so sánh nhất."},
                        {"The homework _______ (must/do) by 9 PM.", "must be done", "Bị động với động từ khuyết thiếu: Modal + be + V3/ed."},
                        {"I had my hair _______ (cut) yesterday.", "cut", "Cấu trúc truyền khiến: Have something done (V3 của cut vẫn là cut)."},
                        {"She got her car _______ (repair).", "repaired", "Cấu trúc truyền khiến: Get something done (V3/ed)."},
                        {"The flowers _______ (water) every day.", "are watered", "Bị động hiện tại đơn với chủ ngữ số nhiều."},
                        {"It _______ (say) that he is a millionaire.", "is said", "Bị động khách quan: It is said that..."},
                        {"He was seen _______ (enter) the building.", "entering", "Bị động của động từ tri giác: thường dùng V-ing để nhấn mạnh hành động đang diễn ra."},
                        {"The letter _______ (send) two days ago.", "was sent", "Bị động quá khứ đơn."},
                        {"Many people _______ (injure) in the accident.", "were injured", "Bị động quá khứ đơn với chủ ngữ số nhiều."},
                        {"The results _______ (announce) tomorrow.", "will be announced", "Bị động tương lai đơn."},
                        {"Plastic bottles should _______ (recycle).", "be recycled", "Bị động với Should: Should + be + V3/ed."},
                        {"The cake _______ (make) by my mother is delicious.", "made", "Rút gọn mệnh đề quan hệ bị động: which was made → made (V3/ed)."},
                        {"I enjoy _______ (read) books in my free time.", "reading", "Sau 'Enjoy' luôn dùng V-ing."},
                        {"She decided _______ (buy) a new computer.", "to buy", "Sau 'Decide' luôn dùng To-V."},
                        {"I look forward to _______ (see) you soon.", "seeing", "Cấu trúc: Look forward to + V-ing (Mong đợi)."},
                        {"It's no use _______ (cry) over spilled milk.", "crying", "Cấu trúc: It's no use + V-ing (Vô ích khi làm gì)."},
                        {"He promised _______ (call) me later.", "to call", "Sau 'Promise' dùng To-V."},
                        {"Stop _______ (make) so much noise!", "making", "Stop + V-ing: Dừng hẳn hành động đang làm."},
                        {"He stopped _______ (buy) a newspaper on the way home.", "to buy", "Stop + To V: Dừng lại để thực hiện mục đích khác."},
                        {"I am used to _______ (get) up early.", "getting", "Be used to + V-ing: Quen với việc gì ở hiện tại."},
                        {"You should _______ (brush) your teeth twice a day.", "brush", "Sau động từ khuyết thiếu 'Should' là V nguyên mẫu."},
                        {"I suggest _______ (go) to the cinema.", "going", "Sau 'Suggest' dùng V-ing."},
                        {"They managed _______ (finish) the task on time.", "to finish", "Sau 'Manage' dùng To-V (Xoay xở để làm gì)."},
                        {"I saw him _______ (cross) the street.", "crossing", "Cấu trúc tri giác: See + O + V-ing."},
                        {"Would you mind _______ (open) the window?", "opening", "Cấu trúc: Would you mind + V-ing?"},
                        {"It is difficult _______ (learn) Japanese.", "to learn", "Cấu trúc: It + be + Adj + To V."},
                        {"He failed _______ (pass) the exam.", "to pass", "Sau 'Fail' dùng To-V."},
                        {"I'd rather _______ (stay) at home tonight.", "stay", "Sau 'Would rather' dùng V nguyên mẫu."},
                        {"You had better _______ (not/smoke) here.", "not smoke", "Cấu trúc: Had better + (not) + V nguyên mẫu."},
                        {"She is too young _______ (see) that movie.", "to see", "Cấu trúc: Too + Adj + To V (Quá... để làm gì)."},
                        {"The water is warm enough _______ (swim) in.", "to swim", "Cấu trúc: Adj + enough + To V (Đủ... để làm gì)."},
                        {"He denied _______ (steal) the money.", "stealing", "Sau 'Deny' dùng V-ing."},
                        {"I can't help _______ (laugh) at his jokes.", "laughing", "Cấu trúc: Can't help + V-ing (Không thể nhịn được)."},
                        {"Avoid _______ (drink) too much coffee.", "drinking", "Sau 'Avoid' dùng V-ing."},
                        {"They let him _______ (go) home early.", "go", "Cấu trúc: Let + O + V nguyên mẫu."},
                        {"Make him _______ (clean) the floor.", "clean", "Cấu trúc: Make + O + V nguyên mẫu (Bắt ai làm gì)."},
                        {"He is interested in _______ (collect) stamps.", "collecting", "Sau giới từ (in/on/at...) dùng V-ing."},
                        {"Instead of _______ (stay) home, she went out.", "staying", "Sau cụm giới từ 'Instead of' dùng V-ing."},
                        {"I want _______ (become) a doctor.", "to become", "Sau 'Want' dùng To-V."},
                        {"She forgot _______ (lock) the door.", "to lock", "Forget + To V: Quên phải làm gì (chưa thực hiện)."},
                        {"Try _______ (finish) it before noon.", "to finish", "Try + to V: cố gắng hoàn thành việc gì."},
                        {"He practiced _______ (speak) English every day.", "speaking", "Sau 'Practice' dùng V-ing."}
                };
                for (int i = 0; i < fbBase.length; i++) {
                    String[] d = fbBase[i];
                    db.execSQL("INSERT OR IGNORE INTO questions (testId, type, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, audioPath) VALUES (200, 'FILL_BLANK', '" + d[0].replace("'", "''") + "', '', '', '', '', '" + d[1].replace("'", "''") + "', '" + d[2].replace("'", "''") + "', '')");
                }

                // --- TEST 300: Listening Multiple Choice ---
                String[][] lMcBase = {
                        // Group 1: Daily Life & Routines (1-20)
                        {"Where is the speaker going?", "Supermarket", "Bank", "Park", "School", "I'm going to the supermarket to buy some milk and bread.", "Người nói đề cập rõ ràng điểm đến là siêu thị (supermarket)."},
                        {"What time does the meeting start?", "9 AM", "8 AM", "10 AM", "11 AM", "Please be on time. The meeting starts at 9 AM tomorrow.", "Thời gian họp là 9 giờ sáng."},
                        {"What is the weather like?", "Rainy", "Sunny", "Cloudy", "Snowy", "Don't forget your umbrella, it is raining heavily outside.", "Người nói nhắc mang ô do trời đang mưa."},
                        {"What did the man buy?", "Watch", "Shirt", "Hat", "Phone", "I just bought this new watch. It was very expensive.", "Vật phẩm là chiếc đồng hồ (watch)."},
                        {"How many people are there?", "Four", "Two", "Three", "Five", "There are four of us coming for dinner tonight.", "Số lượng người là 4."},
                        {"What's for dinner?", "Pasta", "Pizza", "Sushi", "Steak", "I am cooking some Italian pasta for dinner.", "Món ăn là Pasta."},
                        {"Where's the cat?", "Under the table", "On the bed", "In the garden", "Behind the sofa", "Look! The cat is sleeping under the kitchen table.", "Vị trí là dưới gầm bàn."},
                        {"What is her job?", "Doctor", "Nurse", "Teacher", "Singer", "She works at the city hospital as a doctor.", "Nghề nghiệp là bác sĩ."},
                        {"Why was he late?", "Missed the bus", "Traffic", "Alarm", "Rain", "I missed the bus this morning, so I arrived late.", "Lý do là lỡ chuyến xe buýt."},
                        {"What color is the car?", "Blue", "Red", "Black", "White", "My father just bought a beautiful blue car yesterday.", "Màu sắc là xanh dương (blue)."},
                        {"What does he do in the morning?", "Go to the gym", "Read", "Sleep", "Run", "I always go to the gym before starting my work.", "Đi tập gym trước khi làm việc."},
                        {"How does she go to work?", "Train", "Car", "Bike", "Walk", "The train station is near my house, so I take it every day.", "Phương tiện là tàu hỏa (train)."},
                        {"What is his favorite fruit?", "Apple", "Banana", "Orange", "Mango", "I really love apples, they are so sweet.", "Trái cây yêu thích là táo."},
                        {"Where did they meet?", "Cafe", "Office", "Library", "Cinema", "We met at the local cafe to discuss the project.", "Địa điểm là quán cà phê (cafe)."},
                        {"What are they watching?", "Movie", "News", "Match", "Show", "Turn up the volume, this action movie is great!", "Họ đang xem phim hành động (movie)."},
                        {"Who is coming tonight?", "Parents", "Friends", "Boss", "Cousin", "My parents are coming over for dinner today.", "Bố mẹ sẽ đến ăn tối."},
                        {"What is the problem?", "Key", "Wallet", "Phone", "Bag", "Oh no! I can't find my car keys anywhere.", "Vấn đề là mất chìa khóa xe."},
                        {"What season is it?", "Summer", "Winter", "Spring", "Autumn", "It's so hot today. I love summer holidays.", "Thời tiết nóng là mùa hè."},
                        {"What is her hobby?", "Painting", "Singing", "Cooking", "Dancing", "I spend my weekends painting landscapes.", "Sở thích là vẽ tranh."},
                        {"Which floor is the office on?", "Fifth", "Third", "First", "Second", "Our office is located on the fifth floor.", "Văn phòng ở tầng 5."},

                        // Group 2: Shopping & Services (21-40)
                        {"How much is the shirt?", "$20", "$15", "$30", "$50", "This shirt is on sale for only twenty dollars.", "Giá chiếc áo là 20 đô la."},
                        {"What size does he need?", "Medium", "Small", "Large", "XL", "This small shirt is too tight. I need a medium size.", "Cần cỡ trung bình (medium)."},
                        {"Where is the post office?", "Next to the bank", "Opposite school", "Near park", "Far away", "The post office is right next to the city bank.", "Nằm cạnh ngân hàng."},
                        {"How will he pay?", "Cash", "Card", "Check", "Phone", "I don't have my card here. Can I pay with cash?", "Thanh toán bằng tiền mặt."},
                        {"What did she order?", "Coffee", "Tea", "Juice", "Milk", "I'll have a hot cup of black coffee, please.", "Đồ uống là cà phê."},
                        {"When is the delivery?", "Friday", "Monday", "Today", "Next week", "Your package will arrive this Friday afternoon.", "Giao hàng vào thứ Sáu."},
                        {"What is the discount?", "10%", "20%", "50%", "30%", "If you buy two, you get a ten percent discount.", "Giảm giá 10%."},
                        {"What's wrong with the soup?", "Cold", "Too Salty", "Spicy", "Hot", "Excuse me, this soup is a bit too salty.", "Món súp bị mặn."},
                        {"Where is the fitting room?", "Behind counter", "Left side", "Right side", "Upstairs", "The fitting rooms are behind the payment counter.", "Nằm sau quầy thu ngân."},
                        {"What drink is free?", "Water", "Soda", "Wine", "Beer", "Every meal comes with a free glass of mineral water.", "Nước khoáng (water) miễn phí."},
                        {"Is the store open?", "Yes", "No", "Closing soon", "Opening later", "The store stays open until 10 PM every night.", "Cửa hàng mở cửa đến 10 giờ tối."},
                        {"Where can I find milk?", "Aisle 4", "Aisle 1", "Aisle 5", "Entrance", "Fresh milk is located in aisle four, next to the cheese.", "Sữa ở lối đi số 4."},
                        {"What is on sale?", "Shoes", "Hats", "Jeans", "Socks", "All running shoes are fifty percent off today.", "Giày đang được giảm giá."},
                        {"How long is the warranty?", "One year", "Two years", "Six months", "Lifetime", "This laptop comes with a full one-year warranty.", "Bảo hành 1 năm."},
                        {"What is the return policy?", "30 days", "14 days", "7 days", "No returns", "You can return any item within thirty days with a receipt.", "Hạn trả hàng là 30 ngày."},
                        {"Can I help you?", "Looking for dress", "Just looking", "Need a bag", "Want a refund", "I'm looking for a blue dress for a wedding.", "Khách hàng tìm mua váy."},
                        {"Where is the elevator?", "Near entrance", "In the back", "Left corner", "Next to stairs", "The elevator is right near the main entrance.", "Thang máy ở gần lối vào chính."},
                        {"What time does it close?", "9:00 PM", "8:00 PM", "10:00 PM", "11:00 PM", "We close at nine o'clock on weekdays.", "Đóng cửa lúc 9 giờ tối."},
                        {"Who is the manager?", "Mr. Lee", "Ms. Kim", "Mr. West", "Ms. Rose", "Mr. Lee is the store manager today.", "Quản lý là ông Lee."},
                        {"Do you have this in red?", "No", "Yes", "Out of stock", "Coming soon", "I'm sorry, we only have this model in black and white.", "Không có màu đỏ."},

                        // Group 3: Travel & Transport (41-60)
                        {"Which platform for London?", "Platform 4", "Platform 1", "Platform 3", "Platform 9", "The train to London departs from platform four.", "Đường ray số 4."},
                        {"How long is the flight?", "Two hours", "Five hours", "One hour", "Ten hours", "It's a short flight, only about two hours.", "Bay mất 2 tiếng."},
                        {"What time is check-in start?", "2 PM", "12 PM", "10 AM", "3 PM", "Check-in at the hotel starts at 2 PM.", "Nhận phòng lúc 2 giờ chiều."},
                        {"What is the gate number?", "Gate A12", "Gate B5", "Gate C3", "Gate D10", "Please proceed to gate A twelve for boarding.", "Cổng lên máy bay A12."},
                        {"Where is his suitcase?", "Car", "Taxi", "Airport", "Home", "I think I left my suitcase in the taxi!", "Bỏ quên trên taxi."},
                        {"What's the best way to the museum?", "Bus 10", "Walking", "Taxi", "Subway", "Take bus number ten; it stops right in front of the museum.", "Đi xe buýt số 10."},
                        {"How is the sea?", "Calm", "Rough", "Cold", "Dirty", "The sea is very calm today, perfect for swimming.", "Biển hôm nay rất êm."},
                        {"Which city are they in?", "New York", "London", "Paris", "Tokyo", "Welcome to New York! The Big Apple.", "Họ đang ở New York."},
                        {"What is the ticket price?", "$50", "$40", "$60", "$100", "A one-way ticket costs fifty dollars.", "Giá vé là 50 đô la."},
                        {"Where is the gas station?", "Around corner", "Straight ahead", "Behind us", "Left", "There is a gas station just around the corner.", "Ở ngay góc đường."},
                        {"When is the next bus?", "In 10 minutes", "In 5 minutes", "In 20 minutes", "Now", "The next bus to downtown will arrive in ten minutes.", "10 phút nữa."},
                        {"What is the flight status?", "On time", "Delayed", "Cancelled", "Boarding", "Flight 402 is on schedule for a 3 PM departure.", "Chuyến bay đúng giờ."},
                        {"Where is the taxi stand?", "Outside exit", "Level 2", "Parking lot", "Lobby", "You can find a taxi stand right outside the main exit.", "Ở ngoài lối ra chính."},
                        {"What is the cabin number?", "104", "205", "306", "407", "Your cabin is on the first deck, room one zero four.", "Số phòng là 104."},
                        {"How much is the fare?", "$2.50", "$1.50", "$3.00", "$5.00", "The bus fare is two dollars and fifty cents.", "Giá vé là 2.5 đô la."},
                        {"Where is the hotel?", "By the lake", "Near airport", "Downtown", "In the forest", "Our hotel is located right by the lake.", "Khách sạn bên hồ."},
                        {"Is there a map?", "In the brochure", "On the wall", "At the desk", "Online", "There is a detailed map inside the travel brochure.", "Bản đồ trong tập quảng cáo."},
                        {"Which way to the station?", "Turn right", "Turn left", "Go straight", "Backwards", "Turn right at the lights to reach the station.", "Rẽ phải ở đèn giao thông."},
                        {"What is the seat number?", "12A", "14B", "10C", "15D", "Your assigned seat for this trip is twelve A.", "Số ghế là 12A."},
                        {"Can I book a tour?", "Yes", "No", "Sold out", "Only online", "Certainly, we have tours starting every hour.", "Có thể đặt tour."},

                        // Group 4: Work & Education (61-80)
                        {"What is the subject?", "History", "Math", "English", "Science", "Today we will learn about the French Revolution in history class.", "Môn học là Lịch sử."},
                        {"When is the deadline?", "Tomorrow", "Tonight", "Monday", "Friday", "The report must be finished by tomorrow morning.", "Hạn chót là sáng mai."},
                        {"Who is the manager?", "Mr. Smith", "Mr. Brown", "Ms. Green", "Ms. White", "Mr. Smith is our new department manager.", "Quản lý là ông Smith."},
                        {"Where is the meeting?", "Room 302", "Room 101", "Lobby", "Cafe", "The meeting has been moved to room three zero two.", "Họp ở phòng 302."},
                        {"What is the presentation about?", "Marketing", "Sales", "Design", "IT", "I'm giving a presentation on our new marketing strategy.", "Về marketing."},
                        {"Why is she happy?", "Promotion", "Birthday", "Holiday", "Wedding", "I finally got the promotion I wanted!", "Vui vì được thăng chức."},
                        {"How many students are in class?", "Twenty", "Ten", "Thirty", "Fifteen", "There are exactly twenty students registered for this course.", "Có 20 học sinh."},
                        {"What's for lunch at work?", "Sandwich", "Salad", "Soup", "Burger", "I brought a chicken sandwich for lunch today.", "Món bánh mì kẹp (sandwich)."},
                        {"What is the password?", "Apple123", "Admin", "User", "Pass", "The Wi-Fi password is 'apple one two three'.", "Mật khẩu là Apple123."},
                        {"Where can I park?", "Basement", "Street", "Backyard", "Nowhere", "Please park your car in the building's basement.", "Đỗ ở tầng hầm (basement)."},
                        {"When is the interview?", "10:00 AM", "11:00 AM", "9:00 AM", "1:00 PM", "Your interview with the HR manager is at 10 AM.", "Phỏng vấn lúc 10 giờ sáng."},
                        {"Did he pass the exam?", "Yes", "No", "Maybe", "Not yet", "He studied hard and passed the final exam with flying colors.", "Anh ấy đã đỗ kỳ thi."},
                        {"Where is the printer?", "By the window", "Near the door", "On the table", "In the hall", "The printer is over there, right by the window.", "Máy in ở cạnh cửa sổ."},
                        {"What is the salary?", "$3000", "$2500", "$4000", "$5000", "The starting salary for this position is three thousand dollars.", "Lương khởi điểm là 3000 đô la."},
                        {"Who is calling?", "A client", "The boss", "A co-worker", "A friend", "There is a client on line one waiting for you.", "Một khách hàng đang gọi."},
                        {"Where is the lab?", "Second floor", "Third floor", "Basement", "First floor", "The science lab is located on the second floor.", "Phòng lab ở tầng 2."},
                        {"When is the break?", "12:30", "12:00", "1:00", "1:30", "We will take a thirty-minute break at twelve thirty.", "Nghỉ lúc 12:30."},
                        {"When is the office open?", "Mon to Fri", "Mon to Sat", "Every day", "Weekends", "The office is open from Monday to Friday, 9 to 5.", "Mở từ thứ 2 đến thứ 6."},
                        {"What is his degree?", "Engineering", "Business", "Art", "Law", "He graduated last year with a degree in Engineering.", "Bằng kỹ sư."},
                        {"Can I use the phone?", "Yes", "No", "Broken", "Emergency only", "Of course, you can use the phone on my desk.", "Có thể dùng điện thoại."},

                        // Group 5: Health & Social (81-100)
                        {"What hurts?", "Headache", "Back", "Leg", "Arm", "I have a terrible headache since this morning.", "Bị đau đầu (headache)."},
                        {"What should he take?", "Aspirin", "Water", "Rest", "Vitamin", "You should take an aspirin and lie down.", "Nên uống aspirin."},
                        {"Who is calling?", "Doctor", "Sister", "Bank", "Police", "My sister is calling me on my other phone.", "Chị/em gái đang gọi."},
                        {"What is the party for?", "Birthday", "Graduation", "Farewell", "New Year", "We are having a party for Sarah's graduation.", "Tiệc mừng tốt nghiệp."},
                        {"When is the appointment?", "3:30 PM", "2:00 PM", "4:00 PM", "5:00 PM", "Your dental appointment is at three thirty this afternoon.", "Cuộc hẹn lúc 3:30 chiều."},
                        {"What did he lose?", "Glasses", "Watch", "Ring", "Keys", "Have you seen my glasses? I can't read without them.", "Làm mất kính mắt."},
                        {"What is the gift?", "Book", "Flower", "Chocolate", "Perfume", "I bought some red roses as a gift for her.", "Món quà là hoa (flower)."},
                        {"Where is the gym?", "Third floor", "Basement", "Top floor", "Ground floor", "The gym is located on the top floor with a great view.", "Phòng gym ở tầng thượng."},
                        {"What is the movie genre?", "Comedy", "Horror", "Action", "Drama", "Let's watch this comedy. I want to laugh!", "Phim hài (comedy)."},
                        {"What is the date today?", "March 30th", "April 1st", "May 1st", "June 1st", "Today is Monday, March thirtieth.", "Ngày 30 tháng 3."},
                        {"How do you feel?", "Better", "Worse", "Same", "Great", "I took the medicine and I feel much better now.", "Cảm thấy tốt hơn."},
                        {"What is the doctor's name?", "Dr. Smith", "Dr. Jones", "Dr. White", "Dr. Green", "You will be seeing Dr. Smith in room five.", "Bác sĩ Smith."},
                        {"Can I have some water?", "Yes", "No", "Only tea", "Later", "Certainly, let me get you a glass of cold water.", "Có thể uống nước."},
                        {"Where is the pharmacy?", "Opposite park", "Near hotel", "By the bank", "Inside clinic", "There is a pharmacy right opposite the city park.", "Đối diện công viên."},
                        {"Is the gym busy?", "Yes", "No", "Closing", "Renovating", "The gym is very busy at this hour of the day.", "Phòng gym đang rất đông."},
                        {"What is the event?", "Concert", "Wedding", "Lecture", "Game", "We are attending a classical music concert tonight.", "Đi xem hòa nhạc (concert)."},
                        {"Do you have a fever?", "Yes", "No", "Slightly", "Don't know", "My temperature is normal, so I don't have a fever.", "Không bị sốt."},
                        {"What did she say?", "Happy birthday", "Good luck", "Congratulations", "Goodbye", "She called to say congratulations on your new job.", "Chúc mừng (congratulations)."},
                        {"Where is the party?", "At my house", "At a club", "At a hotel", "In the park", "The party is being held at my house tonight.", "Tổ chức tại nhà."},
                        {"Are you hungry?", "Yes", "No", "A little", "Starving", "No, I just had a very big lunch.", "Không đói vì vừa ăn trưa xong."}
                };
                for (int i = 0; i < lMcBase.length; i++) {
                    String[] d = lMcBase[i];
                    List<String> ops = new ArrayList<>();
                    ops.add(d[1]); ops.add(d[2]); ops.add(d[3]); ops.add(d[4]);
                    Collections.shuffle(ops);
                    String correct = "A";
                    if (ops.get(1).equals(d[1])) correct = "B";
                    else if (ops.get(2).equals(d[1])) correct = "C";
                    else if (ops.get(3).equals(d[1])) correct = "D";
                    db.execSQL("INSERT OR IGNORE INTO questions (testId, type, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, audioPath) VALUES (300, 'LISTENING_MC', '" + d[0].replace("'", "''") + "', '" + ops.get(0).replace("'", "''") + "', '" + ops.get(1).replace("'", "''") + "', '" + ops.get(2).replace("'", "''") + "', '" + ops.get(3).replace("'", "''") + "', '" + correct + "', '" + d[6].replace("'", "''") + "', '" + d[5].replace("'", "''") + "')");
                }

                // --- TEST 400: Dictation Practice ---
                String[][] lDictBase = {
                        {"I like to _______ music.", "listen to", "I like to listen to music.", "Cấu trúc: listen to + something."},
                    {"The sky is _______ today.", "blue", "The sky is blue today.", "Màu sắc bầu trời được mô tả là màu xanh."},
                    {"She _______ to school every day.", "walks", "She walks to school every day.", "Diễn tả thói quen đi bộ dùng động từ Walks."},
                    {"Open the _______, please.", "window", "Open the window, please.", "Yêu cầu mở cửa sổ."},
                    {"He is a _______ student.", "good", "He is a good student.", "Tính từ khen ngợi học sinh giỏi."},
                    {"It is _______ outside.", "raining", "It is raining outside.", "Thời tiết bên ngoài đang mưa."},
                    {"I have _______ apples.", "three", "I have three apples.", "Số lượng táo nghe được là 3."},
                    {"The dog is _______.", "barking", "The dog is barking.", "Hành động của con chó là đang sủa."},
                    {"See you _______!", "tomorrow", "See you tomorrow!", "Hẹn gặp lại vào ngày mai."},
                    {"We eat _______ for dinner.", "rice", "We eat rice for dinner.", "Món ăn trong bữa tối là cơm (rice)."},
                    {"My mother is _______ a cake.", "baking", "My mother is baking a cake.", "Hành động nướng bánh dùng Baking."},
                    {"Please turn off the _______.", "lights", "Please turn off the lights.", "Yêu cầu tắt đèn (số nhiều)."},
                    {"I need to _______ some milk.", "buy", "I need to buy some milk.", "Mua sữa dùng động từ Buy."},
                    {"The cat is _______ on the sofa.", "sleeping", "The cat is sleeping on the sofa.", "Con mèo đang ngủ."},
                    {"Where is my _______?", "wallet", "Where is my wallet?", "Hỏi về chiếc ví tiền."},
                    {"The coffee is too _______.", "hot", "The coffee is too hot.", "Tính từ chỉ nhiệt độ cà phê."},
                    {"He _______ his teeth twice a day.", "brushes", "He brushes his teeth twice a day.", "Động từ đánh răng chia theo ngôi số ít."},
                    {"Can you _______ the door?", "close", "Can you close the door?", "Yêu cầu đóng cửa."},
                    {"I am _______ a letter.", "writing", "I am writing a letter.", "Đang viết một bức thư."},
                    {"The birds are _______.", "singing", "The birds are singing.", "Tiếng chim hót dùng Singing."},
                    {"I wear a _______ in winter.", "jacket", "I wear a jacket in winter.", "Áo khoác dùng cho mùa đông."},
                    {"The flowers are _______.", "beautiful", "The flowers are beautiful.", "Tính từ miêu tả hoa đẹp."},
                    {"My father _______ a blue car.", "drives", "My father drives a blue car.", "Lái xe dùng động từ Drives."},
                    {"The baby is _______.", "crying", "The baby is crying.", "Đứa trẻ đang khóc."},
                    {"Put the books on the _______.", "shelf", "Put the books on the shelf.", "Vị trí đặt sách là trên giá/kệ."},
                    {"I love to _______ photos.", "take", "I love to take photos.", "Cấu trúc Chụp ảnh: Take photos."},
                    {"The soup tastes _______.", "delicious", "The soup tastes delicious.", "Tính từ chỉ món ăn ngon."},
                    {"We are _______ a movie.", "watching", "We are watching a movie.", "Đang xem phim dùng Watching."},
                    {"The garden is full of _______.", "roses", "The garden is full of roses.", "Vườn đầy hoa hồng."},
                    {"He is _______ for his keys.", "looking", "He is looking for his keys.", "Cấu trúc Tìm kiếm: Look for."},
                    {"I have an _______ at 2 PM.", "appointment", "I have an appointment at 2 PM.", "Cuộc hẹn dùng Appointment."},
                    {"The _______ is very busy today.", "office", "The office is very busy today.", "Văn phòng bận rộn."},
                    {"Please send me the _______.", "report", "Please send me the report.", "Yêu cầu gửi bản báo cáo."},
                    {"He works as a _______.", "manager", "He works as a manager.", "Nghề nghiệp: Quản lý."},
                    {"We have a _______ tomorrow morning.", "meeting", "We have a meeting tomorrow morning.", "Cuộc họp dùng Meeting."},
                    {"Type your _______ here.", "password", "Type your password here.", "Yêu cầu nhập mật khẩu."},
                        {"The computer is _______.", "slowing down", "The computer is slowing down.", "Cụm động từ: slow down (chậm lại)."},
                    {"I need to _______ this document.", "print", "I need to print this document.", "In tài liệu dùng động từ Print."},
                    {"She is answering the _______.", "phone", "She is answering the phone.", "Trả lời điện thoại."},
                    {"The _______ was very long.", "presentation", "The presentation was very long.", "Bài thuyết trình dùng Presentation."},
                    {"He is checking his _______.", "emails", "He is checking his emails.", "Kiểm tra hòm thư điện tử."},
                    {"I forgot my _______.", "laptop", "I forgot my laptop.", "Để quên máy tính xách tay."},
                    {"The _______ starts at 9 AM.", "conference", "The conference starts at 9 AM.", "Hội nghị dùng Conference."},
                    {"We need more _______.", "information", "We need more information.", "Cần thêm thông tin."},
                    {"The project is almost _______.", "finished", "The project is almost finished.", "Dự án sắp hoàn thành."},
                    {"Sign your _______ on this line.", "name", "Sign your name on this line.", "Yêu cầu ký tên."},
                    {"The desk is very _______.", "messy", "The desk is very messy.", "Bàn làm việc bừa bộn."},
                    {"I work in the _______ department.", "marketing", "I work in the marketing department.", "Bộ phận tiếp thị."},
                    {"The _______ is out of paper.", "printer", "The printer is out of paper.", "Máy in hết giấy."},
                    {"She is a very _______ person.", "creative", "She is a very creative person.", "Tính từ chỉ sự sáng tạo."},
                    {"How much is the _______?", "salary", "How much is the salary?", "Hỏi về mức lương (Salary)."},
                    {"The factory _______ cars.", "produces", "The factory produces cars.", "Nhà máy sản xuất (Produces)."},
                    {"We must _______ the problem.", "solve", "We must solve the problem.", "Giải quyết vấn đề dùng Solve."},
                    {"He is traveling on _______.", "business", "He is traveling on business.", "On business = đi công tác (fixed expression)."},
                    {"The elevator is _______.", "broken", "The elevator is broken.", "Thang máy bị hỏng."},
                    {"Wait for me in the _______.", "lobby", "Wait for me in the lobby.", "Đợi ở sảnh (Lobby)."},
                    {"The company has many _______.", "employees", "The company has many employees.", "Nhân viên dùng Employees."},
                    {"I am _______ for a job.", "applying", "I am applying for a job.", "Ứng tuyển việc làm dùng Apply."},
                    {"The deadline is next _______.", "Monday", "The deadline is next Monday.", "Hạn chót vào thứ Hai tới."},
                    {"Can you help me with this _______?", "task", "Can you help me with this task?", "Nhiệm vụ/công việc dùng Task."},
                    {"The train arrives at the _______.", "station", "The train arrives at the station.", "Nhà ga dùng Station."},
                    {"I am staying at a _______.", "hotel", "I am staying at a hotel.", "Ở tại khách sạn."},
                        {"Fasten your _______.", "seat belt", "Fasten your seat belt, please.", "Cụm danh từ cố định: seat belt."},
                    {"The flight was _______.", "delayed", "The flight was delayed.", "Chuyến bay bị hoãn."},
                    {"Where is the nearest _______?", "bank", "Where is the nearest bank?", "Hỏi về ngân hàng gần nhất."},
                    {"The beach is very _______.", "crowded", "The beach is very crowded.", "Bãi biển rất đông đúc."},
                    {"I lost my _______.", "passport", "I lost my passport.", "Mất hộ chiếu."},
                    {"The map is on the _______.", "table", "The map is on the table.", "Bản đồ ở trên bàn."},
                    {"We are visiting a _______.", "museum", "We are visiting a museum.", "Thăm bảo tàng."},
                    {"The bus is _______.", "late", "The bus is late.", "Xe buýt đến muộn."},
                    {"I need to book a _______.", "ticket", "I need to book a ticket.", "Đặt vé dùng Book a ticket."},
                    {"The mountains are _______.", "high", "The mountains are high.", "Núi cao (High)."},
                    {"Turn _______ at the corner.", "left", "Turn left at the corner.", "Rẽ trái (Left)."},
                    {"The street is very _______.", "noisy", "The street is very noisy.", "Đường phố ồn ào."},
                    {"Take a taxi to the _______.", "airport", "Take a taxi to the airport.", "Đi taxi đến sân bay."},
                    {"The weather is _______.", "sunny", "The weather is sunny.", "Trời nắng (Sunny)."},
                    {"I like to _______ around the world.", "travel", "I like to travel around the world.", "Du lịch thế giới."},
                    {"Is there a _______ nearby?", "pharmacy", "Is there a pharmacy nearby?", "Có hiệu thuốc nào gần đây không?"},
                    {"The bridge is very _______.", "old", "The bridge is very old.", "Cây cầu rất cũ."},
                    {"We are lost. Where is the _______?", "exit", "We are lost. Where is the exit?", "Lối thoát dùng Exit."},
                    {"The lake is _______.", "beautiful", "The lake is beautiful.", "Hồ nước đẹp."},
                    {"I am _______ a postcard.", "sending", "I am sending a postcard.", "Đang gửi bưu thiếp."},
                    {"The road is _______.", "narrow", "The road is narrow.", "Con đường hẹp."},
                    {"Look at the _______.", "stars", "Look at the stars.", "Nhìn lên những ngôi sao."},
                    {"The park is _______ today.", "quiet", "The park is quiet today.", "Công viên yên tĩnh."},
                    {"I have a _______.", "headache", "I have a headache.", "Bị đau đầu (Headache)."},
                        {"Eat more _______ and vegetables.", "fruit", "Eat more fruit and vegetables.", "Fruit thường dùng như danh từ không đếm được."},
                    {"She is wearing a beautiful _______.", "dress", "She is wearing a beautiful dress.", "Mặc một chiếc váy đẹp."},
                    {"Happy _______ to you!", "birthday", "Happy birthday to you!", "Chúc mừng sinh nhật."},
                    {"The movie was very _______.", "funny", "The movie was very funny.", "Bộ phim rất hài hước."},
                    {"I am _______ of spiders.", "afraid", "I am afraid of spiders.", "Sợ nhện (Afraid)."},
                    {"Would you like some _______?", "water", "Would you like some water?", "Bạn có muốn uống nước không?"},
                    {"He is very _______ today.", "tired", "He is very tired today.", "Anh ấy đang mệt."},
                    {"Congratulations on your _______!", "success", "Congratulations on your success!", "Chúc mừng thành công."},
                    {"Let's go for a _______.", "walk", "Let's go for a walk.", "Đi dạo (Go for a walk)."},
                    {"I am _______ to meet you.", "glad", "I am glad to meet you.", "Rất vui được gặp bạn."},
                    {"The soup is too _______.", "salty", "The soup is too salty.", "Món súp quá mặn."},
                    {"I need to see a _______.", "doctor", "I need to see a doctor.", "Cần gặp bác sĩ."},
                    {"The exercise is _______.", "easy", "The exercise is easy.", "Bài tập dễ (Easy)."},
                    {"Good _______, everyone!", "morning", "Good morning, everyone!", "Chào buổi sáng."}
                };
                for (int i = 0; i < lDictBase.length; i++) {
                    String[] d = lDictBase[i];
                    db.execSQL("INSERT OR IGNORE INTO questions (testId, type, questionText, optionA, optionB, optionC, optionD, correctOption, explanation, audioPath) VALUES (400, 'LISTENING_DICTATION', '" + d[0].replace("'", "''") + "', '', '', '', '', '" + d[1].replace("'", "''") + "', '" + d[3].replace("'", "''") + "', '" + d[2].replace("'", "''") + "')");
                }

                db.setTransactionSuccessful();
                Log.d("AppDatabase", "Seeding successful with 4 tests and all questions.");
            } finally {
                db.endTransaction();
            }
        });
    }
}
