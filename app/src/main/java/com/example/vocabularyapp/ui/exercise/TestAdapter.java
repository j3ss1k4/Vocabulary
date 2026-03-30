package com.example.vocabularyapp.ui.exercise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.databinding.ItemExerciseTypeBinding;

import java.util.ArrayList;
import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private List<Test> tests = new ArrayList<>();
    private OnTestClickListener listener;

    public void setTests(List<Test> tests) {
        this.tests = tests;
        notifyDataSetChanged();
    }

    public void setOnTestClickListener(OnTestClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseTypeBinding binding = ItemExerciseTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = tests.get(position);
        holder.binding.tvTitle.setText(test.title);
        holder.binding.tvDesc.setText(test.description);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTestClick(test);
        });
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        ItemExerciseTypeBinding binding;

        public TestViewHolder(@NonNull ItemExerciseTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnTestClickListener {
        void onTestClick(Test test);
    }
}
