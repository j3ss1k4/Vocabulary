package com.example.vocabularyapp.ui.vocabulary;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.databinding.FragmentVocabularyBinding;

public class VocabularyFragment extends BaseFragment<FragmentVocabularyBinding> {
    @Override
    protected FragmentVocabularyBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVocabularyBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {}

    @Override
    protected void initData() {}
}
