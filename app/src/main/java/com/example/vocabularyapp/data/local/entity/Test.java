package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "tests")
public class Test implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public int durationMinutes;
    public int totalQuestions;
}
