package org.techtown.MyExerciseApp.db.Entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.Serializable;

/*
CREATE TABLE "EXERCISE_TB" (
	"ex_id"	INTEGER NOT NULL,
	"ex_name"	TEXT NOT NULL,
	"ex_bodypart"	TEXT NOT NULL,
	"ex_equipment"	TEXT NOT NULL,
	"ex_weight_unit"	TEXT NOT NULL DEFAULT 'kg',
	"ex_image_url"	TEXT,
	"ex_total_set"	INTEGER NOT NULL DEFAULT 0,
	"ex_total_weight"	REAL NOT NULL DEFAULT 0,
	"ex_total_days"	INTEGER NOT NULL DEFAULT 0,
	"ex_frequency"	INTEGER NOT NULL DEFAULT 0,
	"ex_rest_time"	INTEGER NOT NULL DEFAULT 30,
	PRIMARY KEY("ex_id")
);
 */
@Entity(tableName = "EXERCISE_TB")
public class Exercise implements Serializable {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ex_id") @NonNull private int exId;
    @ColumnInfo(name = "ex_name") @NonNull private String exName;
    @ColumnInfo(name = "ex_bodypart") @NonNull private String exBodypart;
    @ColumnInfo(name = "ex_equipment") @NonNull private String exEquipment;
    @ColumnInfo(name = "ex_weight_unit",defaultValue = "kg") @NotNull private String exWeightUnit;
    @ColumnInfo(name = "ex_image_url") @Nullable private String exImageUrl;
    @ColumnInfo(name = "ex_total_set",defaultValue = "0") private int exTotalSet;
    @ColumnInfo(name = "ex_total_weight",defaultValue = "0")  private double exTotalWeight;
    @ColumnInfo(name = "ex_total_days",defaultValue = "0")  private int exTotalDays;
    @ColumnInfo(name = "ex_frequency",defaultValue = "0")  private int exFrequency;
    @ColumnInfo(name = "ex_rest_time",defaultValue = "30") private int exRestTime;
    @ColumnInfo(name = "ex_favorite",defaultValue = "0")  private int exFavorite;
    @ColumnInfo(name = "ex_type") @NonNull private String exType;

    public int getExId() {
        return exId;
    }

    public void setExId(int exId) {
        this.exId = exId;
    }

    @NonNull
    public String getExName() {
        return exName;
    }

    public void setExName(@NonNull String exName) {
        this.exName = exName;
    }

    @NonNull
    public String getExBodypart() {
        return exBodypart;
    }

    public void setExBodypart(@NonNull String exBodypart) {
        this.exBodypart = exBodypart;
    }

    @NonNull
    public String getExEquipment() {
        return exEquipment;
    }

    public void setExEquipment(@NonNull String exEquipment) {
        this.exEquipment = exEquipment;
    }

    public String getExWeightUnit() {
        return exWeightUnit;
    }

    public void setExWeightUnit(String exWeightUnit) {
        this.exWeightUnit = exWeightUnit;
    }

    public String getExImageUrl() {
        return exImageUrl;
    }

    public void setExImageUrl(String exImageUrl) {
        this.exImageUrl = exImageUrl;
    }

    public int getExTotalSet() {
        return exTotalSet;
    }

    public void setExTotalSet(int exTotalSet) {
        this.exTotalSet = exTotalSet;
    }

    public double getExTotalWeight() {
        return exTotalWeight;
    }

    public void setExTotalWeight(double exTotalWeight) {
        this.exTotalWeight = exTotalWeight;
    }

    public int getExTotalDays() {
        return exTotalDays;
    }

    public void setExTotalDays(int exTotalDays) {
        this.exTotalDays = exTotalDays;
    }

    public int getExFrequency() {
        return exFrequency;
    }

    public void setExFrequency(int exFrequency) {
        this.exFrequency = exFrequency;
    }

    public int getExRestTime() {
        return exRestTime;
    }

    public void setExRestTime(int exRestTime) {
        this.exRestTime = exRestTime;
    }

    public int getExFavorite() {
        return exFavorite;
    }

    public void setExFavorite(int exFavorite) {
        this.exFavorite = exFavorite;
    }

    @NonNull
    public String getExType() {
        return exType;
    }

    public void setExType(@NonNull String exType) {
        this.exType = exType;
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "exId=" + exId +
                ", exName='" + exName + '\'' +
                ", exBodypart='" + exBodypart + '\'' +
                ", exEquipment='" + exEquipment + '\'' +
                ", exWeightUnit='" + exWeightUnit + '\'' +
                ", exImageUrl='" + exImageUrl + '\'' +
                ", exTotalSet=" + exTotalSet +
                ", exTotalWeight=" + exTotalWeight +
                ", exTotalDays=" + exTotalDays +
                ", exFrequency=" + exFrequency +
                ", exRestTime=" + exRestTime +
                ", exFavorite=" + exFavorite +
                ", exType='" + exType + '\'' +
                '}';
    }
}
