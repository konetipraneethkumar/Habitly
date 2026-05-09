package com.example.habitly.features.habits;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitly.data.Habit;
import com.example.habitly.databinding.LayoutAddEditHabitBinding;
import com.example.habitly.viewmodel.HabitViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.Locale;

public class AddEditHabitBottomSheet extends BottomSheetDialogFragment {

    private LayoutAddEditHabitBinding binding;
    private HabitViewModel viewModel;
    private int habitId = -1;
    private String selectedEmoji = "📚";
    private String selectedColor = "#FF6B6B";
    private String selectedTime = null;

    private final String[] emojis = {"📚", "🏃", "💧", "🧘", "🎨", "💪", "🍎", "😴", "📝", "🎵", "🌿", "🧠", "💻", "🚴", "🏋️", "🛡️", "✍️", "🎯", "🌅", "🤝"};
    private final String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FECA57", "#FF9FF3", "#54A0FF", "#5F27CD"};

    public static AddEditHabitBottomSheet newInstance(int habitId) {
        AddEditHabitBottomSheet fragment = new AddEditHabitBottomSheet();
        Bundle args = new Bundle();
        args.putInt("habit_id", habitId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutAddEditHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        if (getArguments() != null) {
            habitId = getArguments().getInt("habit_id", -1);
        }

        setupEmojiPicker();
        setupColorPicker();
        setupTimePicker();
        setupSaveButton();

        if (habitId != -1) {
            loadHabitData();
        }
    }

    private void setupEmojiPicker() {
        for (String emoji : emojis) {
            Chip chip = new Chip(requireContext());
            chip.setText(emoji);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setTextSize(24);
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedEmoji = emoji;
            });
            binding.cgEmojis.addView(chip);
            if (emoji.equals(selectedEmoji)) chip.setChecked(true);
        }
    }

    private void setupColorPicker() {
        binding.llColors.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        for (String color : colors) {
            MaterialCardView card = new MaterialCardView(requireContext());
            int size = (int) (44 * density);
            int margin = (int) (6 * density);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(lp);
            card.setRadius(size / 2f);
            card.setCardBackgroundColor(Color.parseColor(color));
            
            boolean isSelected = selectedColor.equalsIgnoreCase(color);
            card.setStrokeWidth(isSelected ? (int) (3 * density) : 0);
            card.setStrokeColor(Color.WHITE);
            card.setCardElevation(isSelected ? 4 * density : 0);

            card.setOnClickListener(v -> {
                selectedColor = color;
                setupColorPicker();
            });
            binding.llColors.addView(card);
        }
    }

    private void setupTimePicker() {
        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showTimePicker();
            } else {
                selectedTime = null;
                binding.tvReminderTime.setVisibility(View.GONE);
            }
        });
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            binding.tvReminderTime.setText("Reminder set for " + selectedTime);
            binding.tvReminderTime.setVisibility(View.VISIBLE);
        }, 8, 0, true);
        timePicker.setOnCancelListener(dialog -> binding.switchReminder.setChecked(false));
        timePicker.show();
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etHabitName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (habitId == -1) {
                Habit newHabit = new Habit(name, selectedEmoji, selectedColor, selectedTime);
                viewModel.insertHabit(newHabit);
            } else {
                viewModel.getHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                    if (habit != null) {
                        habit.setName(name);
                        habit.setIconEmoji(selectedEmoji);
                        habit.setColorHex(selectedColor);
                        habit.setReminderTime(selectedTime);
                        viewModel.updateHabit(habit);
                    }
                });
            }
            dismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            viewModel.getHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                if (habit != null) {
                    viewModel.deleteHabit(habit);
                    dismiss();
                }
            });
        });
    }

    private void loadHabitData() {
        binding.tvSheetTitle.setText("Edit Habit");
        binding.btnSave.setText("Update");
        binding.btnDelete.setVisibility(View.VISIBLE);

        viewModel.getHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
            if (habit != null) {
                binding.etHabitName.setText(habit.getName());
                selectedEmoji = habit.getIconEmoji();
                selectedColor = habit.getColorHex();
                selectedTime = habit.getReminderTime();

                for (int i = 0; i < binding.cgEmojis.getChildCount(); i++) {
                    Chip chip = (Chip) binding.cgEmojis.getChildAt(i);
                    if (chip.getText().toString().equals(selectedEmoji)) chip.setChecked(true);
                }

                setupColorPicker();

                if (selectedTime != null) {
                    binding.switchReminder.setChecked(true);
                    binding.tvReminderTime.setText("Reminder set for " + selectedTime);
                    binding.tvReminderTime.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}