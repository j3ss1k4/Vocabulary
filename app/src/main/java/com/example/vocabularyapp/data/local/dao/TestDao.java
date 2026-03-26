package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Test;

import java.util.List;

@Dao
public interface TestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTest(Test test);

    @Update
    void updateTest(Test test);

    @Delete
    void deleteTest(Test test);

    @Query("SELECT * FROM tests")
    LiveData<List<Test>> getAllTests();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuestion(Question question);

    @Query("SELECT * FROM questions WHERE testId = :testId")
    LiveData<List<Question>> getQuestionsForTest(int testId);
}
