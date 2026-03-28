package com.example.vocabularyapp.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vocabularyapp.base.BaseFragment;
import com.example.vocabularyapp.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private HomeViewModel viewModel;
    private LessonAdapter adapter;

    @Override
    protected FragmentHomeBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        initRecyclerView();
        initObservers();
    }

    @Override
    protected void initView() {
        setupPieChart();
    }

    private void setupPieChart() {
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setExtraOffsets(5, 10, 5, 5);
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.WHITE);
        binding.pieChart.setTransparentCircleRadius(61f);
    }

    private void initRecyclerView() {
        adapter = new LessonAdapter();
        binding.rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLessons.setAdapter(adapter);
        adapter.setOnLessonClickListener(lesson -> {
            Toast.makeText(getContext(), "Bắt đầu học: " + lesson.title, Toast.LENGTH_SHORT).show();
            // Điều hướng tới ExerciseFragment hoặc VocabularyFragment ở đây
        });
    }

    private void initObservers() {
        viewModel.getTotalWordsCount().observe(getViewLifecycleOwner(), total -> {
            viewModel.getMasteredWordsCount().observe(getViewLifecycleOwner(), mastered -> {
                updateChart(total, mastered);
            });
        });

        viewModel.getAllLessons().observe(getViewLifecycleOwner(), lessons -> {
            if (lessons != null) {
                adapter.setLessons(lessons);
            }
        });
    }

    private void updateChart(Integer total, Integer mastered) {
        if (total == null || total == 0) return;
        if (mastered == null) mastered = 0;

        int learning = total - mastered;
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(mastered, "Đã thuộc"));
        entries.add(new PieEntry(learning, "Đang học"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
    }

    @Override
    protected void initData() {
        // ViewModel handles data
    }
}
