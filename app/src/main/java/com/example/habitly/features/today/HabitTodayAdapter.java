package com.example.habitly.features.today;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitly.data.Habit;
import com.example.habitly.data.HabitCompletion;
import com.example.habitly.databinding.ItemHabitTodayBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitTodayAdapter extends RecyclerView.Adapter<HabitTodayAdapter.HabitViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private Set<Integer> completedHabitIds = new HashSet<>();
    private final OnHabitClickListener listener;

    public interface OnHabitClickListener {
        void onHabitClick(int habitId);
    }

    public HabitTodayAdapter(OnHabitClickListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits, List<HabitCompletion> completions) {
        this.habits = habits;
        this.completedHabitIds.clear();
        for (HabitCompletion c : completions) {
            completedHabitIds.add(c.getHabitId());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitTodayBinding binding = ItemHabitTodayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(habits.get(position));
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitTodayBinding binding;

        public HabitViewHolder(ItemHabitTodayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Habit habit) {
            binding.tvHabitEmoji.setText(habit.getIconEmoji());
            binding.tvHabitName.setText(habit.getName());
            binding.viewColorBar.setBackgroundColor(Color.parseColor(habit.getColorHex()));

            boolean isCompleted = completedHabitIds.contains(habit.getId());
            binding.cbHabitDone.setChecked(isCompleted);

            if (isCompleted) {
                binding.tvHabitName.setPaintFlags(binding.tvHabitName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvHabitName.setTextColor(Color.GRAY);
                binding.getRoot().setAlpha(0.6f);
            } else {
                binding.tvHabitName.setPaintFlags(binding.tvHabitName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvHabitName.setTextColor(Color.BLACK);
                binding.getRoot().setAlpha(1.0f);
            }

            binding.cbHabitDone.setOnClickListener(v -> {
                // Animate
                binding.getRoot().animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).withEndAction(() -> {
                    binding.getRoot().animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                }).start();
                listener.onHabitClick(habit.getId());
            });
        }
    }
}