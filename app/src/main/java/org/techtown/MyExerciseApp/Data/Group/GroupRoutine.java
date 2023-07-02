package org.techtown.MyExerciseApp.Data.Group;

import java.io.Serializable;

public class GroupRoutine implements Serializable {
    String groupRoutineName;
    String groupRoutineDate;
    String groupRoutineExercise;

    public GroupRoutine() {}

    public GroupRoutine(String groupRoutineName, String groupRoutineDate, String groupRoutineExercise) {
        this.groupRoutineName = groupRoutineName;
        this.groupRoutineDate = groupRoutineDate;
        this.groupRoutineExercise = groupRoutineExercise;
    }

    public String getGroupRoutineDate() {
        return groupRoutineDate;
    }

    public void setGroupRoutineDate(String groupRoutineDate) {
        this.groupRoutineDate = groupRoutineDate;
    }

    public String getGroupRoutineExercise() {
        return groupRoutineExercise;
    }

    public void setGroupRoutineExercise(String groupRoutineExercise) {
        this.groupRoutineExercise = groupRoutineExercise;
    }

    public String getGroupRoutineName() {
        return groupRoutineName;
    }

    public void setGroupRoutineName(String groupRoutineName) {
        this.groupRoutineName = groupRoutineName;
    }
}
