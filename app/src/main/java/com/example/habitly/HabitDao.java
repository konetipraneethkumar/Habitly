package com.example.habitly;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    long insertHabit(Habit habit);

    @Update
    void updateHabit(Habit habit);

    @Delete
    void deleteHabit(Habit habit);

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY name ASC")
    LiveData<List<Habit>> getAllActiveHabits();

    @Query("SELECT * FROM habits WHERE id = :id")
    Habit getHabitByIdSync(int id);

    @Query("SELECT * FROM habits WHERE id = :id")
    LiveData<Habit> getHabitById(int id);

    @Insert
    void insertCompletion(HabitCompletion completion);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedDate = :date LIMIT 1")
    HabitCompletion getCompletionForDate(int habitId, String date);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedDate DESC")
    List<HabitCompletion> getCompletionsForHabitSync(int habitId);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedDate DESC")
    LiveData<List<HabitCompletion>> getCompletionsForHabit(int habitId);

    @Query("SELECT * FROM habit_completions WHERE completedDate = :date")
    LiveData<List<HabitCompletion>> getCompletionsForDateLiveData(String date);

    @Query("SELECT * FROM habit_completions WHERE completedDate = :date")
    List<HabitCompletion> getCompletionsForDate(String date);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedDate BETWEEN :startDate AND :endDate")
    List<HabitCompletion> getCompletionsInRange(int habitId, String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    int getTotalCompletionsForHabitSync(int habitId);

    @Delete
    void deleteCompletion(HabitCompletion completion);
}
