package org.techtown.MyExerciseApp.db.DAO;

import androidx.room.ColumnInfo;

public class CountTotalDayAndTimes {
    @ColumnInfo(name = "COUNT(DISTINCT b)")
    private int getTotalExerciseDay;
    @ColumnInfo(name = "COUNT(*)")
    private int getTotalExerciseTimes;

    public int getGetTotalExerciseDay() {
        return getTotalExerciseDay;
    }

    public void setGetTotalExerciseDay(int getTotalExerciseDay) {
        this.getTotalExerciseDay = getTotalExerciseDay;
    }

    public int getGetTotalExerciseTimes() {
        return getTotalExerciseTimes;
    }

    public void setGetTotalExerciseTimes(int getTotalExerciseTimes) {
        this.getTotalExerciseTimes = getTotalExerciseTimes;
    }
}
