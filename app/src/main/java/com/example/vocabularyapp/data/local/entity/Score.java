package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scores")
public class Score {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public int testId;
    public int score;
    public int totalQuestions;
    public long testDate;
}
