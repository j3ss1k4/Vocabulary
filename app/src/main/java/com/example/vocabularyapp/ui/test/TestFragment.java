package com.example.vocabularyapp.ui.test;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.databinding.FragmentTestBinding;

public class TestFragment extends BaseFragment<FragmentTestBinding> {
    @Override
    protected FragmentTestBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentTestBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {}

    @Override
    protected void initData() {}
}
