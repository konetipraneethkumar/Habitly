package com.example.habitly.features.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.habitly.databinding.FragmentSocialBinding;
import com.example.habitly.viewmodel.HabitViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.habitly.data.Habit;

import java.util.ArrayList;
import com.example.habitly.utils.CloudSyncManager;
import java.util.List;

public class SocialFragment extends Fragment {

    private FragmentSocialBinding binding;
    private HabitViewModel viewModel;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSocialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        prefs = requireContext().getSharedPreferences("HabitlyPrefs", Context.MODE_PRIVATE);
        
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            binding.layoutLogin.setVisibility(View.GONE);
            binding.layoutSocial.setVisibility(View.VISIBLE);
            setupGlobalLeaderboard();
        } else {
            binding.layoutLogin.setVisibility(View.VISIBLE);
            binding.layoutSocial.setVisibility(View.GONE);
            setupLoginButtons();
        }
    }

    private void setupLoginButtons() {
        binding.btnGoogleLogin.setOnClickListener(v -> {
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", "Social User")
                .apply();
            checkLoginStatus();
        });
    }

    private void setupGlobalLeaderboard() {
        binding.tvFriendCode.setText("GLOBAL-CLOUD");
        CloudSyncManager.initializeDemoData(requireContext());
        
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            int totalExp = 0;
            if (habits != null) {
                for (Habit h : habits) {
                    // 1 completion = 10 EXP
                    totalExp += h.getTotalCompletions() * 10;
                }
            }

            // Sync current user to "Cloud"
            CloudSyncManager.syncUserExp(requireContext(), prefs.getString("user_name", "You"), totalExp);

            // Fetch everyone from "Cloud"
            List<LeaderboardAdapter.Friend> globalData = CloudSyncManager.getGlobalLeaderboard(requireContext());
            
            globalData.sort((f1, f2) -> Integer.compare(f2.exp, f1.exp));
            
            // Highlight current user
            binding.rvSocialLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.rvSocialLeaderboard.setAdapter(new LeaderboardAdapter(globalData, prefs.getString("user_name", "You")));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}