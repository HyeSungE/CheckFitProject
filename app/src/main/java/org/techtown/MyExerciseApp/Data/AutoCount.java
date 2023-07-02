package org.techtown.MyExerciseApp.Data;

public class AutoCount {
    private int set;
    private int reps;

    public AutoCount(int set, int reps) {
        this.set = set;
        this.reps = reps;
    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }
}
