package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tests")
public class Test {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public int durationMinutes;
    public int totalQuestions;
}
