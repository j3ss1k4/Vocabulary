package com.example.vocabularyapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.data.local.dao.WordDao;

public class HomeViewModel extends AndroidViewModel {
    private final WordDao wordDao;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        wordDao = db.wordDao();
    }

    public LiveData<Integer> getTotalWordsCount() {
        return wordDao.getTotalWordsCount();
    }

    public LiveData<Integer> getMasteredWordsCount() {
        return wordDao.getMasteredWordsCount();
    }
}
