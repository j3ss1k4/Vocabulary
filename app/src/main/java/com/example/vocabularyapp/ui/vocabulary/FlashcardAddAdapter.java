package com.example.vocabularyapp.ui.vocabulary;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.databinding.ItemAddFlashcardBinding;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAddAdapter extends RecyclerView.Adapter<FlashcardAddAdapter.ViewHolder> {

    public static class FlashcardInput {
        public String term = "";
        public String definition = "";
    }

    private final List<FlashcardInput> inputs = new ArrayList<>();
    private OnCountChangeListener countChangeListener;

    public interface OnCountChangeListener {
        void onCountChange(int count);
    }

    public FlashcardAddAdapter() {
        // Bắt đầu với 1 thẻ trống
        inputs.add(new FlashcardInput());
    }

    public void setOnCountChangeListener(OnCountChangeListener listener) {
        this.countChangeListener = listener;
        if (listener != null) listener.onCountChange(inputs.size());
    }

    public void addNewCard() {
        inputs.add(new FlashcardInput());
        notifyItemInserted(inputs.size() - 1);
        if (countChangeListener != null) countChangeListener.onCountChange(inputs.size());
    }

    public List<FlashcardInput> getInputs() {
        return inputs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddFlashcardBinding binding = ItemAddFlashcardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FlashcardInput input = inputs.get(position);
        
        // Tránh lỗi trigger listener khi bind lại
        holder.binding.etTerm.setText(input.term);
        holder.binding.etDefinition.setText(input.definition);

        holder.binding.etTerm.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                input.term = s.toString();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        holder.binding.etDefinition.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                input.definition = s.toString();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        holder.binding.btnRemove.setOnClickListener(v -> {
            if (inputs.size() > 1) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    inputs.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                    if (countChangeListener != null) countChangeListener.onCountChange(inputs.size());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return inputs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAddFlashcardBinding binding;
        ViewHolder(ItemAddFlashcardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
