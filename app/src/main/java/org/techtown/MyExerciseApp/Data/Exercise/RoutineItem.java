package org.techtown.MyExerciseApp.Data.Exercise;


import android.os.Parcel;
import android.os.Parcelable;

import org.techtown.MyExerciseApp.db.Entity.Routine;

import java.io.Serializable;
import java.util.ArrayList;

public class RoutineItem implements Serializable, Parcelable {

    private Routine routine;
    private ArrayList<RoutineInfoListItem> routineInfoList;

    public RoutineItem() {}

    public RoutineItem(Routine routine, ArrayList<RoutineInfoListItem> routineInfoList) {
        this.routine = routine;
        this.routineInfoList = routineInfoList;
    }

    protected RoutineItem(Parcel in) {
    }

    public static final Creator<RoutineItem> CREATOR = new Creator<RoutineItem>() {
        @Override
        public RoutineItem createFromParcel(Parcel in) {
            return new RoutineItem(in);
        }

        @Override
        public RoutineItem[] newArray(int size) {
            return new RoutineItem[size];
        }
    };

    public Routine getRoutine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    public ArrayList<RoutineInfoListItem> getRoutineInfoList() {
        return routineInfoList;
    }

    public void setRoutineInfoList(ArrayList<RoutineInfoListItem> routineInfoList) {
        this.routineInfoList = routineInfoList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
