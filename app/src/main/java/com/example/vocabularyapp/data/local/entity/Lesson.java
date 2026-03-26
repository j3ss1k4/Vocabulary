package com.example.vocabularyapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lessons")
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public String category; // grammar, listening, pronunciation
    public String level; // basic, intermediate, advanced
}
