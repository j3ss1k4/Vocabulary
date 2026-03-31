package com.example.vocabularyapp.ui.test;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.databinding.ItemTestBinding;

import java.util.ArrayList;
import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private List<Test> tests = new ArrayList<>();
    private OnTestClickListener listener;

    public interface OnTestClickListener {
        void onTestClick(Test test);
    }

    public void setOnTestClickListener(OnTestClickListener listener) {
        this.listener = listener;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTestBinding binding = ItemTestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        holder.bind(tests.get(position));
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    class TestViewHolder extends RecyclerView.ViewHolder {
        private final ItemTestBinding binding;

        public TestViewHolder(ItemTestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Test test) {
            binding.tvTestTitle.setText(test.title);
            binding.tvTestDescription.setText(test.description);
            binding.tvDuration.setText(test.durationMinutes + " phút");
            binding.tvTotalQuestions.setText(test.totalQuestions + " câu hỏi");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTestClick(test);
                }
            });
        }
    }
}
