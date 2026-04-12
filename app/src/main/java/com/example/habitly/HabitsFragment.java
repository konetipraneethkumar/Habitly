package com.example.habitly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitly.databinding.FragmentHabitsBinding;
import com.google.android.material.snackbar.Snackbar;

public class HabitsFragment extends Fragment {

    private FragmentHabitsBinding binding;
    private HabitViewModel viewModel;
    private HabitListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
        setupSwipeToDelete();
    }

    private void setupRecyclerView() {
        adapter = new HabitListAdapter(new HabitListAdapter.OnHabitActionListener() {
            @Override
            public void onEdit(Habit habit) {
                AddEditHabitBottomSheet bottomSheet = AddEditHabitBottomSheet.newInstance(habit.getId());
                bottomSheet.show(getParentFragmentManager(), "EditHabitBottomSheet");
            }

            @Override
            public void onDelete(Habit habit) {
                deleteHabitWithUndo(habit);
            }
        });
        binding.rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHabits.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits == null || habits.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvHabits.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvHabits.setVisibility(View.VISIBLE);
                adapter.setHabits(habits);
            }
        });
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Habit habit = viewModel.getAllHabits().getValue().get(position);
                deleteHabitWithUndo(habit);
            }
        }).attachToRecyclerView(binding.rvHabits);
    }

    private void deleteHabitWithUndo(Habit habit) {
        viewModel.deleteHabit(habit);
        Snackbar.make(binding.getRoot(), "Habit deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> viewModel.insertHabit(habit))
                .show();
    }
}