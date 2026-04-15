package com.example.habitly.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.habitly.R;
import java.util.Calendar;

public class ThemeManager {
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    public static final int THEME_AUTO_TIMELY = 3;

    public enum TimePeriod {
        MORNING, AFTERNOON, EVENING, NIGHT
    }

    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("HabitlyPrefs", Context.MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode_index", THEME_SYSTEM);

        if (themeMode == THEME_AUTO_TIMELY) {
            int autoSubMode = prefs.getInt("auto_timely_sub_mode", 0); // 0: Auto (Light/Dark), 1: Light, 2: Dark
            if (autoSubMode == 0) {
                TimePeriod period = getCurrentTimePeriod();
                if (period == TimePeriod.MORNING || period == TimePeriod.AFTERNOON) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            } else if (autoSubMode == 1) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else if (themeMode == THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (themeMode == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static TimePeriod getCurrentTimePeriod() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) return TimePeriod.MORNING;      // 5 AM - 11 AM
        if (hour >= 11 && hour < 16) return TimePeriod.AFTERNOON;  // 11 AM - 4 PM
        if (hour >= 16 && hour < 20) return TimePeriod.EVENING;    // 4 PM - 8 PM
        return TimePeriod.NIGHT;                                   // 8 PM - 5 AM
    }

    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("HabitlyPrefs", Context.MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode_index", THEME_SYSTEM);
        
        if (themeMode == THEME_LIGHT) return false;
        if (themeMode == THEME_DARK) return true;
        
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getDynamicBackgroundColor(Context context) {
        if (isDarkTheme(context)) {
            return Color.parseColor("#121212");
        } else {
            return Color.parseColor("#FCFCFF");
        }
    }
    
    public static int getTimelyBackgroundRes() {
        TimePeriod period = getCurrentTimePeriod();
        switch (period) {
            case MORNING: return R.drawable.morning_theme;
            case AFTERNOON: return R.drawable.afternoon_theme;
            case EVENING: return R.drawable.evening_theme;
            case NIGHT: default: return R.drawable.night_theme;
        }
    }
}
