package com.example.habitly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habitly.databinding.FragmentProfileBinding;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private HabitViewModel viewModel;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        prefs = requireContext().getSharedPreferences("HabitlyPrefs", Context.MODE_PRIVATE);

        setupProfile();
        setupLeaderboard();
        observeStats();
    }

    private void setupProfile() {
        String name = prefs.getString("user_name", "User");
        binding.etDisplayName.setText(name);

        binding.etDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                prefs.edit().putString("user_name", s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnResetData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset All Data")
                    .setMessage("Are you sure you want to delete all habits and progress? This cannot be undone.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        // Clear Room DB
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            AppDatabase.getInstance(requireContext()).clearAllTables();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupLeaderboard() {
        List<LeaderboardAdapter.Friend> friends = new ArrayList<>();
        friends.add(new LeaderboardAdapter.Friend("Alex", 24));
        friends.add(new LeaderboardAdapter.Friend("Sarah", 19));
        friends.add(new LeaderboardAdapter.Friend("You", 0)); // Will update
        friends.add(new LeaderboardAdapter.Friend("Mike", 12));
        friends.add(new LeaderboardAdapter.Friend("Emma", 8));

        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            int maxStreak = 0;
            if (habits != null) {
                for (Habit h : habits) {
                    if (h.getStreakCount() > maxStreak) maxStreak = h.getStreakCount();
                }
            }
            
            // Update "You" streak in list
            for (LeaderboardAdapter.Friend f : friends) {
                if (f.name.equals("You")) f.streak = maxStreak;
            }
            
            // Sort friends by streak
            friends.sort((f1, f2) -> Integer.compare(f2.streak, f1.streak));
            binding.rvLeaderboard.setAdapter(new LeaderboardAdapter(friends));
        });
    }

    private void observeStats() {
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                binding.tvTotalHabitStats.setText("Total Habits created: " + habits.size());
            }
        });
    }
}