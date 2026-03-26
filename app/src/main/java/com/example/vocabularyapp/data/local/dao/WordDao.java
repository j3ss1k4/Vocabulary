package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabularyapp.data.local.entity.Word;

import java.util.List;

@Dao
public interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Word word);

    @Update
    void update(Word word);

    @Delete
    void delete(Word word);

    @Query("SELECT * FROM words")
    LiveData<List<Word>> getAllWords();

    @Query("SELECT * FROM words WHERE category = :category")
    LiveData<List<Word>> getWordsByCategory(String category);

    @Query("SELECT * FROM words WHERE nextReviewTime <= :currentTime")
    LiveData<List<Word>> getWordsToReview(long currentTime);
    
    @Query("SELECT COUNT(*) FROM words WHERE masteryLevel >= 80")
    LiveData<Integer> getMasteredWordsCount();

    @Query("SELECT COUNT(*) FROM words")
    LiveData<Integer> getTotalWordsCount();
}
