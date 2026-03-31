package com.example.vocabularyapp.ui.test;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.data.local.AppDatabase;
import com.example.vocabularyapp.databinding.FragmentTestBinding;

public class TestFragment extends BaseFragment<FragmentTestBinding> {
    
    private TestAdapter adapter;

    @Override
    protected FragmentTestBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentTestBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        adapter = new TestAdapter();
        binding.rvTests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTests.setAdapter(adapter);

        adapter.setOnTestClickListener(test -> {
            Intent intent = new Intent(getActivity(), TestSessionActivity.class);
            intent.putExtra("test", test);
            startActivity(intent);
        });
    }

    @Override
    protected void initData() {
        AppDatabase.getInstance(requireContext()).testDao().getAllTests().observe(getViewLifecycleOwner(), tests -> {
            if (tests != null) {
                adapter.setTests(tests);
            }
        });
    }
}
