package com.example.habitly;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habitly.databinding.FragmentAnalyticsBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private HabitViewModel viewModel;
    private HabitConsistencyAdapter consistencyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        setupRecyclerView();
        observeStats();
        setupBarChart();
    }

    private void setupRecyclerView() {
        consistencyAdapter = new HabitConsistencyAdapter();
        binding.rvHabitConsistency.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHabitConsistency.setAdapter(consistencyAdapter);
    }

    private void observeStats() {
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                binding.tvTotalHabits.setText(String.valueOf(habits.size()));
                int totalCompletions = 0;
                int bestStreak = 0;
                for (Habit h : habits) {
                    totalCompletions += h.getTotalCompletions();
                    if (h.getStreakCount() > bestStreak) bestStreak = h.getStreakCount();
                }
                binding.tvTotalCompletions.setText(String.valueOf(totalCompletions));
                binding.tvBestStreak.setText(String.valueOf(bestStreak));
                
                consistencyAdapter.setHabits(habits);
            }
        });
    }

    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        String[] labels = new String[7];
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -6);

        for (int i = 0; i < 7; i++) {
            labels[i] = new SimpleDateFormat("EEE", Locale.getDefault()).format(cal.getTime());
            // Mock data for now as calculating this from Room requires complex queries
            entries.add(new BarEntry(i, (float) (Math.random() * 5)));
            cal.add(Calendar.DATE, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Completions");
        dataSet.setColor(Color.parseColor("#5C6BC0"));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        binding.barChart.setData(barData);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        binding.barChart.invalidate();
    }
}