package org.techtown.MyExerciseApp.db.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/*
CREATE TABLE "EXERCISE_RECORD_TB" (
	"er_id"	INTEGER NOT NULL,
	"er_date"	INTEGER NOT NULL,
	"er_round"	INTEGER NOT NULL,
	"er_start_time"	TEXT NOT NULL,
	"er_end_time"	TEXT NOT NULL,
	"er_total_set"	INTEGER,
	"er_total_weight"	REAL,
	"er_total_time"	INTEGER,
	PRIMARY KEY("er_id")
);
 */
@Entity(tableName = "EXERCISE_RECORD_TB")
public class ExerciseRecord implements Serializable {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "er_id") @NonNull private int erId;
    @ColumnInfo(name = "er_date") @NonNull private String erDate;
    @ColumnInfo(name = "er_start_date") @NonNull private String erStartDate;
    @ColumnInfo(name = "er_round") @NonNull private int erRound;
    @ColumnInfo(name = "er_total_set",defaultValue = "0") @NonNull private int erTotalSet;
    @ColumnInfo(name = "er_total_weight",defaultValue = "0") @NonNull private double erTotalWeight;
    @ColumnInfo(name = "er_total_time") @NonNull private String erTotalTime;
    @ColumnInfo(name = "er_smile_tag") private String erSmileTag;
    @ColumnInfo(name = "er_total_memo") private String erTotalMemo;
    @ColumnInfo(name = "er_exercise_list") private String erExerciseList;

    public int getErId() {
        return erId;
    }

    public void setErId(int erId) {
        this.erId = erId;
    }

    public String getErDate() {
        return erDate;
    }

    public void setErDate(String erDate) {
        this.erDate = erDate;
    }

    @NonNull
    public String getErStartDate() {
        return erStartDate;
    }

    public void setErStartDate(@NonNull String erStartDate) {
        this.erStartDate = erStartDate;
    }

    public int getErRound() {
        return erRound;
    }

    public void setErRound(int erRound) {
        this.erRound = erRound;
    }

    public int getErTotalSet() {
        return erTotalSet;
    }

    public void setErTotalSet(int erTotalSet) {
        this.erTotalSet = erTotalSet;
    }

    public double getErTotalWeight() {
        return erTotalWeight;
    }

    public void setErTotalWeight(double erTotalWeight) {
        this.erTotalWeight = erTotalWeight;
    }

    public String getErTotalTime() {
        return erTotalTime;
    }

    public void setErTotalTime(String erTotalTime) {
        this.erTotalTime = erTotalTime;
    }

    public String getErSmileTag() {
        return erSmileTag;
    }

    public void setErSmileTag(String erSmileTag) {
        this.erSmileTag = erSmileTag;
    }

    @NonNull
    public String getErTotalMemo() {
        return erTotalMemo;
    }

    public void setErTotalMemo(@NonNull String erTotalMemo) {
        this.erTotalMemo = erTotalMemo;
    }

    public String getErExerciseList() {
        return erExerciseList;
    }

    public void setErExerciseList(String erExerciseList) {
        this.erExerciseList = erExerciseList;
    }

}
