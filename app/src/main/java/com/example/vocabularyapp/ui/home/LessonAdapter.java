package com.example.vocabularyapp.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.data.local.entity.Lesson;
import com.example.vocabularyapp.databinding.ItemLessonBinding;

import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons = new ArrayList<>();
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonPoint(Lesson lesson);
    }

    public void setOnLessonClickListener(OnLessonClickListener listener) {
        this.listener = listener;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLessonBinding binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LessonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.binding.tvLessonTitle.setText(lesson.title);
        holder.binding.tvLessonDesc.setText(lesson.description);
        holder.binding.tvLessonCategory.setText(lesson.category);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLessonPoint(lesson);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        ItemLessonBinding binding;

        public LessonViewHolder(ItemLessonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
