package com.example.vocabularyapp.ui.exercise;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.databinding.FragmentExerciseBinding;

public class ExerciseFragment extends BaseFragment<FragmentExerciseBinding> {
    @Override
    protected FragmentExerciseBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExerciseBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {}

    @Override
    protected void initData() {}
}
