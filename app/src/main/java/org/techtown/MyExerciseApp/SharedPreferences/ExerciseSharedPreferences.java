package org.techtown.MyExerciseApp.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class ExerciseSharedPreferences {
    private static final String EXERCISE = "exercise";
    private static final String CHRONOMETER_BASE_KEY = "chronometerBase";
    private Context context;
    public ExerciseSharedPreferences(Context context) {
        this.context = context;
    }

    public boolean hasKey(String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }
    public String getSelectedExerciseListPref() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        return sharedPreferences.getString("selectedExerciseList","selectedExerciseListEmpty");
    }

    public void setSelectedExerciseListPref(String exerciseListString) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("selectedExerciseList", exerciseListString);

        editor.apply();

    }

    public Long getPauseOffSetPref() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);
        return sharedPreferences.getLong(CHRONOMETER_BASE_KEY, 0);
    }

    public void setPauseOffSetPref(Long pauseOffset) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(CHRONOMETER_BASE_KEY, pauseOffset);

        editor.apply();

    }

    public boolean getStartBtVisPref() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);
        return sharedPreferences.getBoolean("startBtVis", true);
    }

    public void setStartBtVisPref(boolean startBtVis) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("startBtVis", startBtVis);

        editor.apply();

    }




    public long getDefaultRestTimePref() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        return sharedPreferences.getLong("DefaultRestTime",1 * 60000);
    }

    public void setDefaultRestTimePref(long DefaultRestTime) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong("DefaultRestTime", DefaultRestTime);

        editor.apply();

    }


    public void clearPref() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(EXERCISE, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        editor.apply();

    }
}
