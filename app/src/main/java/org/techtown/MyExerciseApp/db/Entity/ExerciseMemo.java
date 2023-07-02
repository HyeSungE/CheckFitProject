package org.techtown.MyExerciseApp.db.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

/*
CREATE TABLE "EXERCISE_MEMO_TB" (
	"erim_id"	INTEGER NOT NULL,
	"erim_title"	TEXT,
	"erim_content"	TEXT,
	"erim_write_time"	TEXT,
	PRIMARY KEY("erim_id")
);
 */

@Entity(tableName = "EXERCISE_MEMO_TB",primaryKeys = {"ex_id","erim_write_time"},foreignKeys = {
        @ForeignKey(entity = Exercise.class, parentColumns = "ex_id", childColumns = "ex_id"),
})

public class ExerciseMemo {
    @ColumnInfo(name = "ex_id") @NonNull private int exId;
    @ColumnInfo(name = "erim_title") private String erimTitle;
    @ColumnInfo(name = "erim_content") private String erimContent;
    @ColumnInfo(name = "erim_write_time") @NonNull private String erimWriteTime;


    public ExerciseMemo(int exId, String erimTitle, String erimContent, @NonNull String erimWriteTime) {
        this.exId = exId;
        this.erimTitle = erimTitle;
        this.erimContent = erimContent;
        this.erimWriteTime = erimWriteTime;
    }

    public int getExId() {
        return exId;
    }

    public void setExId(int exId) {
        this.exId = exId;
    }

    public String getErimTitle() {
        return erimTitle;
    }

    public void setErimTitle(String erimTitle) {
        this.erimTitle = erimTitle;
    }

    public String getErimContent() {
        return erimContent;
    }

    public void setErimContent(String erimContent) {
        this.erimContent = erimContent;
    }

    public String getErimWriteTime() {
        return erimWriteTime;
    }

    public void setErimWriteTime(String erimWriteTime) {
        this.erimWriteTime = erimWriteTime;
    }

}
