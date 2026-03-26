package com.example.vocabularyapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabularyapp.data.local.entity.Progress;

import java.util.List;

@Dao
public interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Progress progress);

    @Update
    void update(Progress progress);

    @Query("SELECT * FROM progress WHERE userId = :userId")
    LiveData<List<Progress>> getProgressByUser(int userId);

    @Query("SELECT * FROM progress WHERE lessonId = :lessonId")
    LiveData<List<Progress>> getProgressByLesson(int lessonId);
}
