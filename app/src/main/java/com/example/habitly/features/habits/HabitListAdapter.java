package com.example.habitly.features.habits;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitly.data.Habit;
import com.example.habitly.databinding.ItemHabitListBinding;

import java.util.ArrayList;
import java.util.List;

public class HabitListAdapter extends RecyclerView.Adapter<HabitListAdapter.HabitViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private final OnHabitActionListener listener;

    public interface OnHabitActionListener {
        void onEdit(Habit habit);
        void onDelete(Habit habit);
    }

    public HabitListAdapter(OnHabitActionListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitListBinding binding = ItemHabitListBinding.inflate(
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
        private final ItemHabitListBinding binding;

        public HabitViewHolder(ItemHabitListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Habit habit) {
            binding.tvHabitEmoji.setText(habit.getIconEmoji());
            binding.tvHabitName.setText(habit.getName());
            binding.tvStreak.setText("🔥 " + habit.getStreakCount() + " day streak");
            
            // Apply icon background color if needed, or use the color bar
            // For this layout let's just use text colors.

            binding.btnEdit.setOnClickListener(v -> listener.onEdit(habit));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(habit));
            
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onEdit(habit);
                return true;
            });
        }
    }
}