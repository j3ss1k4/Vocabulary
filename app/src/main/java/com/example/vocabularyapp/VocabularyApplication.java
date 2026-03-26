package com.example.vocabularyapp;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.vocabularyapp.worker.ReminderWorker;

import java.util.concurrent.TimeUnit;

public class VocabularyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleDailyReminder();
    }

    private void scheduleDailyReminder() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyReminder",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
        );
    }
}
