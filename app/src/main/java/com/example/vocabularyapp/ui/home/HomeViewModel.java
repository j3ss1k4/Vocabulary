package com.example.vocabularyapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.entity.Lesson;
import com.example.vocabularyapp.data.local.entity.Word;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final LiveData<List<Lesson>> allLessons;
    private final LiveData<Integer> totalWordsCount;
    private final LiveData<Integer> masteredWordsCount;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        allLessons = db.lessonDao().getAllLessons();
        totalWordsCount = db.wordDao().getTotalWordsCount();
        masteredWordsCount = db.wordDao().getMasteredWordsCount();
    }

    public LiveData<List<Lesson>> getAllLessons() {
        return allLessons;
    }

    public LiveData<Integer> getTotalWordsCount() {
        return totalWordsCount;
    }

    public LiveData<Integer> getMasteredWordsCount() {
        return masteredWordsCount;
    }
}
