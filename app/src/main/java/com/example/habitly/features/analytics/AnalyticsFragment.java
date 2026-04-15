package com.example.habitly.features.analytics;

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

import com.example.habitly.data.Habit;
import com.example.habitly.databinding.FragmentAnalyticsBinding;
import com.example.habitly.features.habits.HabitConsistencyAdapter;
import com.example.habitly.viewmodel.HabitViewModel;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -6);
        String startDate = sdf.format(cal.getTime());
        String endDate = sdf.format(Calendar.getInstance().getTime());

        viewModel.getAllCompletionsInRange(startDate, endDate).observe(getViewLifecycleOwner(), completions -> {
            if (completions == null) return;

            List<BarEntry> entries = new ArrayList<>();
            String[] labels = new String[7];
            Calendar innerCal = Calendar.getInstance();
            innerCal.add(Calendar.DATE, -6);

            for (int i = 0; i < 7; i++) {
                String dateStr = sdf.format(innerCal.getTime());
                labels[i] = new SimpleDateFormat("EEE", Locale.getDefault()).format(innerCal.getTime());
                
                int count = 0;
                for (com.example.habitly.data.HabitCompletion c : completions) {
                    if (c.getCompletedDate().equals(dateStr)) {
                        count++;
                    }
                }
                
                entries.add(new BarEntry(i, (float) count));
                innerCal.add(Calendar.DATE, 1);
            }

            BarDataSet dataSet = new BarDataSet(entries, "Completions");
            dataSet.setColor(Color.parseColor("#5C6BC0"));
            dataSet.setDrawValues(false);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.6f);

            binding.barChart.setData(barData);
            binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            binding.barChart.invalidate();
        });

        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }
}