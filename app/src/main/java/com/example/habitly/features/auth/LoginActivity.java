package com.example.habitly.features.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habitly.R;
import com.example.habitly.databinding.ActivityLoginBinding;
import com.example.habitly.features.main.MainActivity;
import com.example.habitly.utils.CloudSyncManager;
import com.example.habitly.utils.ThemeManager;
import com.bumptech.glide.Glide;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences prefs = getSharedPreferences("HabitlyPrefs", MODE_PRIVATE);
        int themeModeIndex = prefs.getInt("theme_mode_index", ThemeManager.THEME_SYSTEM);
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        
        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateTimelyBackground(themeModeIndex);
        setupListeners();
    }

    private void updateTimelyBackground(int themeModeIndex) {
        if (themeModeIndex == ThemeManager.THEME_AUTO_TIMELY) {
            binding.ivLoginBg.setVisibility(View.VISIBLE);
            binding.vLoginOverlay.setVisibility(View.VISIBLE);
            
            Glide.with(this)
                    .load(ThemeManager.getTimelyBackgroundRes())
                    .centerCrop()
                    .into(binding.ivLoginBg);
            
            ThemeManager.TimePeriod period = ThemeManager.getCurrentTimePeriod();
            float alpha = (period == ThemeManager.TimePeriod.MORNING || period == ThemeManager.TimePeriod.AFTERNOON) ? 0.60f : 0.80f;
            binding.vLoginOverlay.setAlpha(alpha);
        } else {
            // For other themes, use a solid color or a default image
            binding.ivLoginBg.setVisibility(View.GONE);
            binding.vLoginOverlay.setVisibility(View.GONE);
            binding.getRoot().setBackgroundResource(R.color.md_theme_light_primary); // Default fallback
        }
    }

    private void setupListeners() {
        binding.btnPrimaryAuth.setOnClickListener(v -> handleAuth());
        
        binding.tvSwitchAuth.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUiMode();
        });

        binding.btnGoogleAuth.setOnClickListener(v -> {
            // Fast & Free simulated Google Auth
            performSuccessfulLogin("Google User");
        });
    }

    private void updateUiMode() {
        if (isLoginMode) {
            binding.tvAuthTitle.setText(R.string.login_title);
            binding.btnPrimaryAuth.setText(R.string.login_button);
            binding.tvSwitchAuth.setText(R.string.no_account);
        } else {
            binding.tvAuthTitle.setText(R.string.signup_button);
            binding.btnPrimaryAuth.setText(R.string.signup_button);
            binding.tvSwitchAuth.setText(R.string.has_account);
        }
    }

    private void handleAuth() {
        if (binding.etEmail.getText() == null || binding.etPassword.getText() == null) return;

        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check against Cloud/Demo accounts
        String[] user = CloudSyncManager.authenticate(email, password);
        if (user != null) {
            performSuccessfulLogin(user[2]); // user[2] is the Name
        } else {
            // New account creation simulation
            performSuccessfulLogin(email.split("@")[0]);
        }
    }

    private void performSuccessfulLogin(String name) {
        SharedPreferences prefs = getSharedPreferences("HabitlyPrefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", name)
                .apply();

        Toast.makeText(this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}