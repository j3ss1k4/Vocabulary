package com.example.vocabularyapp.ui.exercise;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.TestDao;
import com.example.vocabularyapp.data.local.entity.Question;
import com.example.vocabularyapp.data.local.entity.Test;

import java.util.List;

public class ExerciseViewModel extends AndroidViewModel {
    private final TestDao testDao;

    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        testDao = db.testDao();
    }

    public LiveData<List<Test>> getAllTests() {
        return testDao.getAllTests();
    }

    public LiveData<List<Question>> getQuestionsForTest(int testId) {
        return testDao.getQuestionsForTest(testId);
    }
}
