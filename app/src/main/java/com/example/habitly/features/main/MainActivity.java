package com.example.habitly.features.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.view.View;

import com.example.habitly.R;
import com.example.habitly.databinding.ActivityMainBinding;
import com.example.habitly.features.habits.AddEditHabitBottomSheet;
import com.example.habitly.notifications.NotificationHelper;
import com.example.habitly.utils.ThemeManager;

/**
 * Main Activity for Habitly App.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        android.content.SharedPreferences prefs = getSharedPreferences("HabitlyPrefs", MODE_PRIVATE);
        int themeModeIndex = prefs.getInt("theme_mode_index", ThemeManager.THEME_SYSTEM);
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateTimelyBackground(themeModeIndex);

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.navigation_today || destination.getId() == R.id.navigation_habits) {
                    binding.fabAddHabit.show();
                } else {
                    binding.fabAddHabit.hide();
                }
            });
        }

        binding.fabAddHabit.setOnClickListener(v -> {
            AddEditHabitBottomSheet bottomSheet = new AddEditHabitBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddHabitBottomSheet");
        });
    }

    private void updateTimelyBackground(int themeModeIndex) {
        if (themeModeIndex == ThemeManager.THEME_AUTO_TIMELY) {
            binding.ivTimelyBackground.setVisibility(View.VISIBLE);
            binding.vThemeOverlay.setVisibility(View.VISIBLE);
            binding.ivTimelyBackground.setImageResource(ThemeManager.getTimelyBackgroundRes());
            binding.ivTimelyBackground.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            
            // Adjust overlay alpha based on time for readability
            ThemeManager.TimePeriod period = ThemeManager.getCurrentTimePeriod();
            float alpha = (period == ThemeManager.TimePeriod.MORNING || period == ThemeManager.TimePeriod.AFTERNOON) ? 0.20f : 0.40f;
            binding.vThemeOverlay.setAlpha(alpha);
            
            // Make layout background transparent to see the image
            binding.getRoot().setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } else {
            binding.ivTimelyBackground.setVisibility(View.GONE);
            binding.vThemeOverlay.setVisibility(View.GONE);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityResultLauncher<String> requestPermissionLauncher =
                        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            // Permission granted or denied
                        });
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}