package com.example.habitly.features.today;

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
import com.example.habitly.databinding.FragmentTodayBinding;
import com.example.habitly.viewmodel.HabitViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private FragmentTodayBinding binding;
    private HabitViewModel viewModel;
    private HabitTodayAdapter adapter;
    private List<Habit> currentHabits = new ArrayList<>();

    private final String[] quotes = {
            "Small steps lead to big changes. 🌱",
            "Consistency is the key to success. 🔑",
            "Your habits define your future. ✨",
            "Do something today that your future self will thank you for. 🙌",
            "Motivation is what gets you started. Habit is what keeps you going. 🏃",
            "Quality is not an act, it is a habit. 💎",
            "Success is the sum of small efforts, repeated day in and day out. 📈",
            "The secret of your future is hidden in your daily routine. 🕵️",
            "First we make our habits, then our habits make us. 🛠️",
            "It’s not what we do once in a while that shapes our lives, but what we do consistently. 🔄"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTodayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        setupUI();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupUI() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) greeting = "Good Morning";
        else if (hour < 17) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        String userName = requireContext().getSharedPreferences("HabitlyPrefs", 0).getString("user_name", "User");
        binding.tvGreeting.setText(greeting + ", " + userName + "! 👋");

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        binding.tvDate.setText(sdf.format(new Date()));

        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        binding.tvQuote.setText("\"" + quotes[dayOfYear % quotes.length] + "\"");
    }

    private void setupRecyclerView() {
        adapter = new HabitTodayAdapter((habitId) -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            viewModel.toggleHabitCompletion(habitId, today);
        });
        binding.rvTodayHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTodayHabits.setAdapter(adapter);
    }

    private void observeViewModel() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            currentHabits = habits;
            updateUIState();
        });

        viewModel.getCompletionsForDate(today).observe(getViewLifecycleOwner(), completions -> {
            if (currentHabits != null && completions != null) {
                adapter.setHabits(currentHabits, completions);
                updateProgress(completions.size(), currentHabits.size());
            }
        });
    }

    private void updateUIState() {
        if (currentHabits == null || currentHabits.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.rvTodayHabits.setVisibility(View.GONE);
            updateProgress(0, 0);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.rvTodayHabits.setVisibility(View.VISIBLE);
        }
    }

    private void updateProgress(int completed, int total) {
        if (total == 0) {
            binding.progressToday.setProgress(0);
            binding.tvProgressPercent.setText("0/0 (0%)");
            return;
        }
        int percent = (int) (((float) completed / total) * 100);
        binding.progressToday.setProgress(percent);
        binding.tvProgressPercent.setText(completed + "/" + total + " (" + percent + "%)");
    }
}