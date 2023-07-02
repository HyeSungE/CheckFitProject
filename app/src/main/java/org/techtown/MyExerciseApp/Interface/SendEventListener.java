package org.techtown.MyExerciseApp.Interface;

import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.ArrayList;
import java.util.List;

public interface SendEventListener {
    public void sendExerciseList(ArrayList<Exercise> exerciseArrayList);
    public void sendExerciseListToRoutine(ArrayList<Exercise> exerciseArrayList);
    public void sendRoutine(List<RoutineInfoListItem> routineInfoListItemArrayList);
}
