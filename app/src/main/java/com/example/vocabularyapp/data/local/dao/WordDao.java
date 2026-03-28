package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.data.local.model.CategoryInfo;

import java.util.List;

@Dao
public interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Word word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Word> words);

    @Update
    void update(Word word);

    @Delete
    void delete(Word word);

    @Query("DELETE FROM words WHERE category = :category")
    void deleteByCategory(String category);

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

    @Query("SELECT * FROM words WHERE term LIKE :query OR definition LIKE :query")
    LiveData<List<Word>> searchWords(String query);

    @Query("SELECT DISTINCT category FROM words WHERE category IS NOT NULL AND category != ''")
    LiveData<List<String>> getAllCategories();

    @Query("SELECT category, COUNT(*) as wordCount FROM words GROUP BY category")
    LiveData<List<CategoryInfo>> getCategoryStats();
}
