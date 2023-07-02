package org.techtown.MyExerciseApp.Data.Exercise;

import java.io.Serializable;

public class RoutineInfoListItemDetailed implements Serializable {
    private int set;
    private String val1;
    private String val2;
    private boolean check;

    public RoutineInfoListItemDetailed(int set, String val1, String val2) {
        this.set = set;
        this.val1 = val1;
        this.val2 = val2;
        this.check = false;
    }
    public RoutineInfoListItemDetailed(int set, String val1) {
        this.set = set;
        this.val1 = val1;
        this.check = false;

    }

    public RoutineInfoListItemDetailed() {

    }

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public String getVal1() {
        return val1;
    }

    public void setVal1(String val1) {
        this.val1 = val1;
    }

    public String getVal2() {
        return val2;
    }

    public void setVal2(String val2) {
        this.val2 = val2;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
