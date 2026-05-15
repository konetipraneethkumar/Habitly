package com.example.habitly.features.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habitly.R;
import com.example.habitly.data.AppDatabase;
import com.example.habitly.data.Habit;
import com.example.habitly.data.HabitCompletion;
import com.example.habitly.databinding.FragmentProfileBinding;
import com.example.habitly.utils.CloudSyncManager;
import com.example.habitly.utils.ThemeManager;
import com.example.habitly.viewmodel.HabitViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

        setupToolbar();
        setupProfile();
        setupCloudSync();
        observeStats();
    }

    private void setupCloudSync() {
        binding.btnBackupCloud.setOnClickListener(v -> backupData());
        binding.btnRestoreCloud.setOnClickListener(v -> restoreData());
    }

    private void backupData() {
        String email = prefs.getString("user_email", null);
        if (email == null) {
            Toast.makeText(getContext(), R.string.login_to_backup, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                String jsonData = new Gson().toJson(habits);
                CloudSyncManager.backupDataToCloud(requireContext(), email, jsonData);
                Toast.makeText(getContext(), R.string.sync_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreData() {
        String email = prefs.getString("user_email", null);
        if (email == null) {
            Toast.makeText(getContext(), R.string.login_to_restore, Toast.LENGTH_SHORT).show();
            return;
        }

        String jsonData = CloudSyncManager.restoreDataFromCloud(requireContext(), email);
        if (jsonData == null) {
            Toast.makeText(getContext(), R.string.no_backup_found, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_title)
                .setMessage(R.string.restore_message)
                .setPositiveButton(R.string.restore_button, (dialog, which) -> {
                    List<Habit> restoredHabits = new Gson().fromJson(jsonData, new TypeToken<List<Habit>>(){}.getType());
                    if (restoredHabits != null) {
                        for (Habit h : restoredHabits) {
                            viewModel.insertHabit(h);
                        }
                        Toast.makeText(getContext(), R.string.restore_complete, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setupToolbar() {
        if (getActivity() == null) return;
        View toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar instanceof androidx.appcompat.widget.Toolbar) {
            androidx.appcompat.widget.Toolbar materialToolbar = (androidx.appcompat.widget.Toolbar) toolbar;
            materialToolbar.getMenu().clear();
            materialToolbar.inflateMenu(R.menu.menu_settings);
            materialToolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_account_settings) {
            showAccountSettingsDialog();
            return true;
        } else if (id == R.id.menu_theme) {
            showThemeDialog();
            return true;
        } else if (id == R.id.menu_reminders) {
            Toast.makeText(getContext(), "Use the switch below for global reminders", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_export) {
            exportDataAsCsv();
            return true;
        } else if (id == R.id.menu_reset) {
            showResetDialog();
            return true;
        } else if (id == R.id.menu_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.menu_feedback) {
            sendFeedback();
            return true;
        } else if (id == R.id.menu_logout) {
            showLogoutDialog();
            return true;
        }
        return false;
    }

    private void showAccountSettingsDialog() {
        Context context = getContext();
        if (context == null) return;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_account_settings, null);
        EditText etName = view.findViewById(R.id.et_account_name);
        EditText etEmail = view.findViewById(R.id.et_account_email);
        
        etName.setText(prefs.getString("user_name", "User"));
        etEmail.setText(prefs.getString("user_email", "user@example.com"));

        new AlertDialog.Builder(context)
                .setTitle(R.string.menu_account_settings)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = etName.getText().toString();
                    String newEmail = etEmail.getText().toString();
                    prefs.edit()
                            .putString("user_name", newName)
                            .putString("user_email", newEmail)
                            .apply();
                    binding.etDisplayName.setText(newName);
                    Toast.makeText(getContext(), R.string.account_updated, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        view.findViewById(R.id.btn_change_password).setOnClickListener(v -> Toast.makeText(getContext(), R.string.password_reset_sent, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_delete_account).setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle(R.string.delete_account_confirm)
                .setMessage(R.string.delete_account_message)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    prefs.edit().clear().apply();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getInstance(context).clearAllTables();
                        if (getActivity() != null) getActivity().runOnUiThread(() -> getActivity().finish());
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show());
    }

    private void showLogoutDialog() {
        Context context = getContext();
        if (context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(R.string.menu_logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> {
                    prefs.edit().putBoolean("is_logged_in", false).apply();
                    // In a real app, you'd navigate to LoginActivity
                    Toast.makeText(getContext(), R.string.logged_out, Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) getActivity().finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showThemeDialog() {
        Context context = getContext();
        if (context == null) return;

        String[] themes = {"Light", "Dark", "System Default", "Auto Timely"};
        int checkedItem = prefs.getInt("theme_mode_index", 2);

        new AlertDialog.Builder(context)
                .setTitle(R.string.menu_theme)
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    if (which == 3) {
                        dialog.dismiss();
                        showAutoTimelySubOptions();
                    } else {
                        int mode;
                        switch (which) {
                            case 0: mode = AppCompatDelegate.MODE_NIGHT_NO; break;
                            case 1: mode = AppCompatDelegate.MODE_NIGHT_YES; break;
                            default: mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
                        }
                        
                        // Save first
                        prefs.edit()
                                .putInt("theme_mode", mode)
                                .putInt("theme_mode_index", which)
                                .apply();
                        
                        // Apply mode
                        AppCompatDelegate.setDefaultNightMode(mode);
                        
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showAutoTimelySubOptions() {
        Context context = getContext();
        if (context == null) return;

        String[] options = {"Dynamic (Light day / Dark night)", "Always Light", "Always Dark"};
        int checkedSub = prefs.getInt("auto_timely_sub_mode", 0);

        new AlertDialog.Builder(context)
                .setTitle(R.string.auto_timely_style)
                .setSingleChoiceItems(options, checkedSub, (dialog, which) -> {
                    prefs.edit()
                            .putInt("theme_mode_index", 3)
                            .putInt("auto_timely_sub_mode", which)
                            .apply();
                    
                    ThemeManager.applyTheme(requireContext());
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.back, (dialog, which) -> showThemeDialog())
                .show();
    }

    private void exportDataAsCsv() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<Habit> habits = db.habitDao().getAllActiveHabitsSync();
            
            StringBuilder csv = new StringBuilder("Habit Name,Streak,Total Completions,Date Created\n");
            for (Habit h : habits) {
                csv.append(h.getName()).append(",")
                   .append(h.getStreakCount()).append(",")
                   .append(h.getTotalCompletions()).append(",")
                   .append(new java.util.Date(h.getCreatedAt())).append("\n");
            }

            try {
                File file = new File(requireContext().getExternalFilesDir(null), "habit_data.csv");
                FileOutputStream out = new FileOutputStream(file);
                out.write(csv.toString().getBytes());
                out.close();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.example.habitly.fileprovider", file);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/csv");
                        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Export Data"));
                    });
                }
            } catch (IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Export failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showAboutDialog() {
        Context context = getContext();
        if (context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.about_title, "Habitly"))
                .setMessage(getString(R.string.about_message, "Habitly", "1.0"))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback@habitflow.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "HabitFlow Feedback");
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditNameDialog() {
        EditText editText = new EditText(requireContext());
        editText.setText(prefs.getString("user_name", "User"));
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.menu_edit_display_name)
                .setView(editText)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = editText.getText().toString();
                    prefs.edit().putString("user_name", newName).apply();
                    binding.etDisplayName.setText(newName);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showResetDialog() {
        Context context = getContext();
        if (context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(R.string.reset_data_title)
                .setMessage(R.string.reset_data_message)
                .setPositiveButton(R.string.menu_reset, (dialog, which) -> AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase.getInstance(context).clearAllTables();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), R.string.all_progress_reset, Toast.LENGTH_SHORT).show());
                    }
                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setupProfile() {
        String name = prefs.getString("user_name", "User");
        binding.etDisplayName.setText(name);
        binding.tvMemberSince.setText(getString(R.string.member_since, "April 2024"));

        binding.btnResetData.setOnClickListener(v -> showResetDialog());
        
        binding.switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(getContext(), R.string.notifications_enabled, Toast.LENGTH_SHORT).show();
                // Logic to re-schedule all reminders could go here
            } else {
                Toast.makeText(getContext(), R.string.notifications_disabled, Toast.LENGTH_SHORT).show();
                // Logic to cancel all reminders could go here
            }
        });
    }

    private void observeStats() {
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                binding.tvTotalHabitStats.setText(String.valueOf(habits.size()));
                int bestStreak = 0;
                int currentStreak = 0; // Simplified for UI demonstration
                for (Habit h : habits) {
                    if (h.getStreakCount() > bestStreak) bestStreak = h.getStreakCount();
                    if (h.getStreakCount() > 0) currentStreak = h.getStreakCount(); // Just an example
                }
                binding.tvBestStreak.setText(String.valueOf(bestStreak));
                binding.tvCurrentStreak.setText(String.valueOf(currentStreak));
            }
        });
    }
}