package com.example.vocabularyapp.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

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

    @Override
    protected FragmentHomeBinding inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
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

    @Override
    protected void initData() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        viewModel.getTotalWordsCount().observe(getViewLifecycleOwner(), total -> {
            viewModel.getMasteredWordsCount().observe(getViewLifecycleOwner(), mastered -> {
                updateChart(total, mastered);
            });
        });
    }

    private void updateChart(Integer total, Integer mastered) {
        if (total == null) total = 0;
        if (mastered == null) mastered = 0;

        int learning = total - mastered;
        if (total == 0) {
            // Show some empty state if needed
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(mastered, "Mastered"));
        entries.add(new PieEntry(learning, "Learning"));

        PieDataSet dataSet = new PieDataSet(entries, "Progress");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate(); // refresh
    }
}
