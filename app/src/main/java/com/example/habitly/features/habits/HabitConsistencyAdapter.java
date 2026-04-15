package com.example.habitly.features.habits;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitly.data.Habit;
import com.example.habitly.databinding.ItemHabitConsistencyBinding;

import java.util.ArrayList;
import java.util.List;

public class HabitConsistencyAdapter extends RecyclerView.Adapter<HabitConsistencyAdapter.ViewHolder> {

    private List<Habit> habits = new ArrayList<>();

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitConsistencyBinding binding = ItemHabitConsistencyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(habits.get(position));
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitConsistencyBinding binding;

        public ViewHolder(ItemHabitConsistencyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Habit habit) {
            binding.tvHabitName.setText(habit.getIconEmoji() + " " + habit.getName());
            
            // Simplified consistency calculation: completions / days since creation (max 7)
            long diff = System.currentTimeMillis() - habit.getCreatedAt();
            long days = (diff / (1000 * 60 * 60 * 24)) + 1;
            if (days > 7) days = 7;
            
            // In a real app we'd query actual completions for last 7 days.
            // For now, let's mock the grid based on streak and total.
            binding.llGrid.removeAllViews();
            int completions = 0;
            for (int i = 0; i < 7; i++) {
                View square = new View(binding.getRoot().getContext());
                int size = (int) (16 * binding.getRoot().getContext().getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMargins(4, 0, 4, 0);
                square.setLayoutParams(lp);
                
                // Mock: fill squares based on streak
                if (i >= 7 - habit.getStreakCount()) {
                    square.setBackgroundColor(Color.parseColor(habit.getColorHex()));
                    completions++;
                } else {
                    square.setBackgroundColor(Color.LTGRAY);
                }
                binding.llGrid.addView(square);
            }
            
            int percent = (int) ((completions / 7f) * 100);
            binding.tvConsistencyPercent.setText(percent + "%");
        }
    }
}