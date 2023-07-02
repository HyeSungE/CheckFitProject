package org.techtown.MyExerciseApp.Data.Exercise;

import java.io.Serializable;
import java.util.ArrayList;

public class ExerciseTableInfo implements Serializable {
    private String exerciseName;
    private ArrayList<String> exerciseTableHeader;
    private ArrayList<String> exerciseTableBody;
    int index;

    public ExerciseTableInfo() {}

    public ExerciseTableInfo(String exerciseName) {
        this.exerciseName = exerciseName;
        this.exerciseTableHeader = new ArrayList<>();
        this.exerciseTableBody = new ArrayList<>();
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public ArrayList<String> getExerciseTableHeader() {
        return exerciseTableHeader;
    }

    public void setExerciseTableHeader(ArrayList<String> exerciseTableHeader) {
        this.exerciseTableHeader = exerciseTableHeader;
    }

    public ArrayList<String> getExerciseTableBody() {
        return exerciseTableBody;
    }

    public void setExerciseTableBody(ArrayList<String> exerciseTableBody) {
        this.exerciseTableBody = exerciseTableBody;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
