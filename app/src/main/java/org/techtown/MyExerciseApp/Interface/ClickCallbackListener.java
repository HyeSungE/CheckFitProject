package org.techtown.MyExerciseApp.Interface;

import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.List;

public interface ClickCallbackListener {
    void callBackRestTime(long restTime,boolean checked);
    void callRestTimerCancel ();
    void callExerciseCount(List<Exercise> item);
    void callExerciseTotalWeight(int pos, double totalWeight);
    void callTotalExerciseTimerStart();
    void callBackStartTime(String startTime);
}
