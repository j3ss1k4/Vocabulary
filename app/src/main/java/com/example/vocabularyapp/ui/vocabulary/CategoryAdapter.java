package com.example.vocabularyapp.ui.vocabulary;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.data.local.model.CategoryInfo;
import com.example.vocabularyapp.databinding.ItemCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryInfo> categories = new ArrayList<>();
    private OnCategoryClickListener listener;
    private OnCategoryLongClickListener longClickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(String categoryName);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setOnCategoryLongClickListener(OnCategoryLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setCategories(List<CategoryInfo> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryInfo category = categories.get(position);
        holder.binding.tvCategoryName.setText(category.category);
        holder.binding.tvWordCount.setText(category.wordCount + " từ vựng");
        
        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(category.category);
        });

        holder.binding.getRoot().setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onCategoryLongClick(category.category);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
