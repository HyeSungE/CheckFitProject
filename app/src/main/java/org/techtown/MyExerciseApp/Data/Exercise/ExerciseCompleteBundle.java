package org.techtown.MyExerciseApp.Data.Exercise;

import java.io.Serializable;
import java.util.HashMap;

public class ExerciseCompleteBundle implements Serializable {
    private HashMap<Integer, ExerciseTableInfo> hashMap;
    private long offset;
    private String exerciseTime;
    private String totalWeight;
    private String totalCount;

    public ExerciseCompleteBundle(HashMap<Integer, ExerciseTableInfo> hashMap, long offset, String exerciseTime, String totalWeight, String totalCount) {
        this.hashMap = hashMap;
        this.offset = offset;
        this.exerciseTime = exerciseTime;
        this.totalWeight = totalWeight;
        this.totalCount = totalCount;
    }

    public HashMap<Integer, ExerciseTableInfo> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<Integer, ExerciseTableInfo> hashMap) {
        this.hashMap = hashMap;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getExerciseTime() {
        return exerciseTime;
    }

    public void setExerciseTime(String exerciseTime) {
        this.exerciseTime = exerciseTime;
    }

    public String getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(String totalWeight) {
        this.totalWeight = totalWeight;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }
}
