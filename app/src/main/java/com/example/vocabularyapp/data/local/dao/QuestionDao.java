package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabularyapp.data.local.entity.Question;

import java.util.List;

@Dao
public interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Question question);

    @Update
    void update(Question question);

    @Delete
    void delete(Question question);

    @Query("SELECT * FROM questions WHERE testId = :testId")
    LiveData<List<Question>> getQuestionsByTestId(int testId);

    /**
     * Lấy ngẫu nhiên các câu hỏi duy nhất cho một bài test.
     * GROUP BY questionText đảm bảo các câu hỏi có nội dung giống nhau chỉ xuất hiện 1 lần.
     * ORDER BY RANDOM() đảm bảo mỗi lần gọi là một bộ câu hỏi khác nhau.
     */
    @Query("SELECT * FROM questions WHERE testId = :testId GROUP BY questionText ORDER BY RANDOM() LIMIT :limit")
    LiveData<List<Question>> getRandomQuestionsByTestId(int testId, int limit);

    @Query("SELECT * FROM questions")
    LiveData<List<Question>> getAllQuestions();
}
