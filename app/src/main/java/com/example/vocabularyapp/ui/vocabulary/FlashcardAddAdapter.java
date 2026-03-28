package com.example.vocabularyapp.ui.vocabulary;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabularyapp.data.local.entity.Word;
import com.example.vocabularyapp.databinding.ItemAddFlashcardBinding;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAddAdapter extends RecyclerView.Adapter<FlashcardAddAdapter.ViewHolder> {

    public static class FlashcardInput {
        public int id = 0; // 0 nghĩa là từ mới
        public String term = "";
        public String definition = "";
    }

    private final List<FlashcardInput> inputs = new ArrayList<>();
    private OnCountChangeListener countChangeListener;

    public interface OnCountChangeListener {
        void onCountChange(int count);
    }

    public FlashcardAddAdapter() {
        // Mặc định tạo 1 thẻ trống
        inputs.add(new FlashcardInput());
    }

    public void setOnCountChangeListener(OnCountChangeListener listener) {
        this.countChangeListener = listener;
        if (listener != null) listener.onCountChange(inputs.size());
    }

    public void setInitialWords(List<Word> words) {
        inputs.clear();
        for (Word word : words) {
            FlashcardInput input = new FlashcardInput();
            input.id = word.id;
            input.term = word.term;
            input.definition = word.definition;
            inputs.add(input);
        }
        if (inputs.isEmpty()) inputs.add(new FlashcardInput());
        notifyDataSetChanged();
        if (countChangeListener != null) countChangeListener.onCountChange(inputs.size());
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
        
        holder.binding.etTerm.setText(input.term);
        holder.binding.etDefinition.setText(input.definition);

        // Sử dụng một phương pháp an toàn hơn để lắng nghe thay đổi
        if (holder.termWatcher != null) holder.binding.etTerm.removeTextChangedListener(holder.termWatcher);
        if (holder.defWatcher != null) holder.binding.etDefinition.removeTextChangedListener(holder.defWatcher);

        holder.termWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                input.term = s.toString();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        holder.defWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                input.definition = s.toString();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        holder.binding.etTerm.addTextChangedListener(holder.termWatcher);
        holder.binding.etDefinition.addTextChangedListener(holder.defWatcher);

        holder.binding.btnRemove.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                inputs.remove(adapterPos);
                notifyItemRemoved(adapterPos);
                if (countChangeListener != null) countChangeListener.onCountChange(inputs.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return inputs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAddFlashcardBinding binding;
        TextWatcher termWatcher;
        TextWatcher defWatcher;
        ViewHolder(ItemAddFlashcardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
