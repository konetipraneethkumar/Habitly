package com.example.habitly;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private final HabitDao habitDao;
    private final LiveData<List<Habit>> allActiveHabits;
    private final ExecutorService executorService;
    private final android.content.Context applicationContext;

    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        habitDao = db.habitDao();
        allActiveHabits = habitDao.getAllActiveHabits();
        executorService = Executors.newSingleThreadExecutor();
        applicationContext = application.getApplicationContext();
    }

    public LiveData<List<Habit>> getAllActiveHabits() {
        return allActiveHabits;
    }

    public LiveData<Habit> getHabitById(int id) {
        return habitDao.getHabitById(id);
    }

    public void insertHabit(Habit habit) {
        executorService.execute(() -> {
            long id = habitDao.insertHabit(habit);
            if (habit.getReminderTime() != null) {
                // Schedule reminder for new habit
                String[] timeParts = habit.getReminderTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                NotificationHelper.scheduleReminder(applicationContext, (int) id, hour, minute);
            }
        });
    }

    public void updateHabit(Habit habit) {
        executorService.execute(() -> {
            habitDao.updateHabit(habit);
            if (habit.getReminderTime() != null) {
                String[] timeParts = habit.getReminderTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                NotificationHelper.scheduleReminder(applicationContext, habit.getId(), hour, minute);
            } else {
                NotificationHelper.cancelReminder(applicationContext, habit.getId());
            }
        });
    }

    public void deleteHabit(Habit habit) {
        executorService.execute(() -> {
            habitDao.deleteHabit(habit);
            NotificationHelper.cancelReminder(applicationContext, habit.getId());
        });
    }

    public void insertCompletion(HabitCompletion completion) {
        executorService.execute(() -> {
            habitDao.insertCompletion(completion);
            updateStreakSync(completion.getHabitId());
        });
    }

    public void deleteCompletion(HabitCompletion completion) {
        executorService.execute(() -> {
            habitDao.deleteCompletion(completion);
            updateStreakSync(completion.getHabitId());
        });
    }

    public void toggleCompletion(int habitId, String date) {
        executorService.execute(() -> {
            HabitCompletion existing = habitDao.getCompletionForDate(habitId, date);
            if (existing != null) {
                habitDao.deleteCompletion(existing);
            } else {
                habitDao.insertCompletion(new HabitCompletion(habitId, date, System.currentTimeMillis()));
            }
            updateStreakSync(habitId);
        });
    }

    public LiveData<List<HabitCompletion>> getCompletionsForHabit(int habitId) {
        return habitDao.getCompletionsForHabit(habitId);
    }

    private void updateStreakSync(int habitId) {
        Habit habit = habitDao.getHabitByIdSync(habitId);
        if (habit == null) return;

        List<HabitCompletion> completions = habitDao.getCompletionsForHabitSync(habitId);
        if (completions == null || completions.isEmpty()) {
            habit.setStreakCount(0);
            habit.setTotalCompletions(0);
            habitDao.updateHabit(habit);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String today = sdf.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        String yesterday = sdf.format(cal.getTime());

        int currentStreak = 0;
        int missedDays = 0;
        
        // Create a set of completed dates for O(1) lookup
        java.util.Set<String> completedDates = new java.util.HashSet<>();
        for (HabitCompletion c : completions) {
            completedDates.add(c.getCompletedDate());
        }

        // Start checking from today backwards
        cal = Calendar.getInstance();
        for (int i = 0; i < 365; i++) {
            String date = sdf.format(cal.getTime());
            if (completedDates.contains(date)) {
                currentStreak++;
                missedDays = 0;
            } else {
                if (i > 0) { // Don't break if today isn't done yet
                    missedDays++;
                    if (missedDays >= 2) break; // Smart streak: allow 1 day gap
                }
            }
            cal.add(Calendar.DATE, -1);
        }

        habit.setStreakCount(currentStreak);
        if (currentStreak > habit.getLongestStreak()) {
            habit.setLongestStreak(currentStreak);
        }
        
        habit.setTotalCompletions(completions.size());
        habitDao.updateHabit(habit);
    }
}
