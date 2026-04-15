package com.example.habitly.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.habitly.data.AppDatabase;
import com.example.habitly.data.Habit;
import com.example.habitly.data.HabitCompletion;
import com.example.habitly.data.HabitRepository;

import java.util.List;

public class HabitViewModel extends AndroidViewModel {
    private final HabitRepository repository;
    private final LiveData<List<Habit>> allHabits;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRepository(application);
        allHabits = repository.getAllActiveHabits();
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public LiveData<Habit> getHabitById(int id) {
        return repository.getHabitById(id);
    }

    public void insertHabit(Habit habit) {
        repository.insertHabit(habit);
    }

    public void updateHabit(Habit habit) {
        repository.updateHabit(habit);
    }

    public void deleteHabit(Habit habit) {
        repository.deleteHabit(habit);
    }

    public void toggleHabitCompletion(int habitId, String todayDate) {
        repository.toggleCompletion(habitId, todayDate);
    }

    public LiveData<List<HabitCompletion>> getCompletionsForHabit(int habitId) {
        return repository.getCompletionsForHabit(habitId);
    }

    public LiveData<List<HabitCompletion>> getAllCompletionsInRange(String startDate, String endDate) {
        return repository.getAllCompletionsInRange(startDate, endDate);
    }

    public LiveData<List<HabitCompletion>> getCompletionsForDate(String date) {
        return AppDatabase.getInstance(getApplication()).habitDao().getCompletionsForDateLiveData(date);
    }
}