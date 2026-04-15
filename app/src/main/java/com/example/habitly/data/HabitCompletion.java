package com.example.habitly.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_completions",
        foreignKeys = @ForeignKey(entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("habitId")})
public class HabitCompletion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int habitId;
    private String completedDate; // yyyy-MM-dd
    private long timestamp;

    public HabitCompletion(int habitId, String completedDate, long timestamp) {
        this.habitId = habitId;
        this.completedDate = completedDate;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHabitId() { return habitId; }
    public void setHabitId(int habitId) { this.habitId = habitId; }
    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}