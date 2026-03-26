package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "progress")
public class Progress {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public int wordId;
    public int lessonId;
    public boolean isCompleted;
    public long completionDate;
    public int score;
}
