package com.example.habitly.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.habitly.features.social.LeaderboardAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simulates a Cloud Backend (Firebase/Firestore) using SharedPreferences.
 * Stores data for ALL users in a single global preference file.
 */
public class CloudSyncManager {
    private static final String PREF_NAME = "HabitlyCloudDB";
    
    // Demo Account Credentials
    public static final String[][] DEMO_USERS = {
        {"zen@habitly.com", "zen123", "ZenMaster", "2450"},
        {"king@habitly.com", "king123", "ConsistencyKing", "1980"},
        {"hero@habitly.com", "hero123", "HabitHero", "1520"},
        {"bird@habitly.com", "bird123", "EarlyBird", "890"},
        {"steady@habitly.com", "steady123", "SteadyStepper", "450"}
    };

    public static void initializeDemoData(Context context) {
        SharedPreferences cloud = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (cloud.getAll().isEmpty()) {
            SharedPreferences.Editor editor = cloud.edit();
            for (String[] user : DEMO_USERS) {
                editor.putInt(user[2], Integer.parseInt(user[3])); // Name -> EXP
            }
            editor.apply();
        }
    }

    public static void syncUserExp(Context context, String name, int exp) {
        SharedPreferences cloud = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        cloud.edit().putInt(name, exp).apply();
    }

    /**
     * Simulates full data backup (Habits + Stats)
     */
    public static void backupDataToCloud(Context context, String email, String jsonData) {
        SharedPreferences cloud = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        cloud.edit().putString("backup_" + email, jsonData).apply();
    }

    /**
     * Simulates data restoration
     */
    public static String restoreDataFromCloud(Context context, String email) {
        SharedPreferences cloud = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return cloud.getString("backup_" + email, null);
    }

    public static List<LeaderboardAdapter.Friend> getGlobalLeaderboard(Context context) {
        SharedPreferences cloud = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = cloud.getAll();
        List<LeaderboardAdapter.Friend> leaderboard = new ArrayList<>();
        
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                leaderboard.add(new LeaderboardAdapter.Friend(entry.getKey(), (Integer) entry.getValue()));
            }
        }
        return leaderboard;
    }

    public static String[] authenticate(String email, String password) {
        for (String[] user : DEMO_USERS) {
            if (user[0].equalsIgnoreCase(email) && user[1].equals(password)) {
                return user; // Returns {email, password, name, initialExp}
            }
        }
        return null;
    }
}
