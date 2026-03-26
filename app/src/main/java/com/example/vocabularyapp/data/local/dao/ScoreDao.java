package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.vocabularyapp.data.local.entity.Score;

import java.util.List;

@Dao
public interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScore(Score score);

    @Query("SELECT * FROM scores WHERE userId = :userId ORDER BY testDate DESC")
    LiveData<List<Score>> getScoresByUser(int userId);

    @Query("SELECT SUM(score) FROM scores WHERE userId = :userId")
    LiveData<Integer> getTotalScoreByUser(int userId);
}
