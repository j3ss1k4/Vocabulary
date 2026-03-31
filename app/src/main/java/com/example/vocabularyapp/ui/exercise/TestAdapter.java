package com.example.vocabularyapp.ui.exercise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.data.local.entity.Test;
import com.example.vocabularyapp.databinding.ItemExerciseHeaderBinding;
import com.example.vocabularyapp.databinding.ItemExerciseTypeBinding;

import java.util.ArrayList;
import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();
    private OnTestClickListener listener;

    public void setTests(List<Test> tests) {
        items.clear();
        List<Test> grammarTests = new ArrayList<>();
        List<Test> listeningTests = new ArrayList<>();
        List<Test> speakingTests = new ArrayList<>();

        for (Test t : tests) {
            if (t.id < 300) {
                grammarTests.add(t);
            } else if (t.id < 500) {
                listeningTests.add(t);
            } else {
                speakingTests.add(t);
            }
        }

        if (!grammarTests.isEmpty()) {
            items.add("I. GRAMMAR");
            items.addAll(grammarTests);
        }
        if (!listeningTests.isEmpty()) {
            items.add("II. LISTENING");
            items.addAll(listeningTests);
        }
        if (!speakingTests.isEmpty()) {
            items.add("III. SPEAKING");
            items.addAll(speakingTests);
        }
        notifyDataSetChanged();
    }

    public void setOnTestClickListener(OnTestClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            ItemExerciseHeaderBinding binding = ItemExerciseHeaderBinding.inflate(inflater, parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemExerciseTypeBinding binding = ItemExerciseTypeBinding.inflate(inflater, parent, false);
            return new TestViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).binding.tvHeaderTitle.setText((String) items.get(position));
        } else if (holder instanceof TestViewHolder) {
            Test test = (Test) items.get(position);
            TestViewHolder testHolder = (TestViewHolder) holder;
            
            String displayTitle = test.title;
            // Clean up titles for display
            if (test.id == 100) displayTitle = "Multiple Choice Quiz";
            else if (test.id == 200) displayTitle = "Fill-in-the-Blanks";
            else if (test.id == 300) displayTitle = "Multiple Choice Quiz";
            else if (test.id == 400) displayTitle = "Dictation Practice";
            else if (test.id == 500) displayTitle = "Vocabulary Practice";
            else if (test.id == 600) displayTitle = "Sentence Practice";
            
            testHolder.binding.tvTitle.setText(displayTitle);
            testHolder.binding.tvDesc.setText(test.description);
            testHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTestClick(test);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ItemExerciseHeaderBinding binding;
        public HeaderViewHolder(@NonNull ItemExerciseHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
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
