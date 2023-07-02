package org.techtown.MyExerciseApp.Data.Exercise;

import androidx.annotation.NonNull;

import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Stream;

public class RoutineInfoListItem implements Serializable {

    private Exercise exercise;
    private ArrayList<RoutineInfoListItemDetailed> routineInfoListItemDetailedArrayList;

    public RoutineInfoListItem() {
    }
    public RoutineInfoListItem(Exercise exercise, ArrayList<RoutineInfoListItemDetailed> routineInfoListItemDetailedArrayList) {
        this.exercise = exercise;
        this.routineInfoListItemDetailedArrayList = routineInfoListItemDetailedArrayList;

    }
    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public ArrayList<RoutineInfoListItemDetailed> getRoutineInfoListItemDetailedArrayList() {
        return routineInfoListItemDetailedArrayList;
    }

    public void setRoutineInfoListItemDetailedArrayList(ArrayList<RoutineInfoListItemDetailed> routineInfoListItemDetailedArrayList) {
        this.routineInfoListItemDetailedArrayList = routineInfoListItemDetailedArrayList;
    }



}
