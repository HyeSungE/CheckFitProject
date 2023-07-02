package org.techtown.MyExerciseApp.db.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ROUTINE_TB")
public class Routine {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "ro_id") @NonNull private int roId;
    @ColumnInfo(name = "ro_name") @NonNull private String roName;
    @ColumnInfo(name = "ro_info") private String roInfo;
    @ColumnInfo(name = "ro_creation_time") @NonNull private String roCreationTime;
    @ColumnInfo(name = "ro_latest_time",defaultValue = "-") @NonNull private String roLatestTime;
    @ColumnInfo(name = "ro_used_count",defaultValue = "0")  @NonNull private int roUsedCount;
    @ColumnInfo(name = "ro_exercise_list") @NonNull private String roExerciseList;
    @ColumnInfo(name = "ro_favorite",defaultValue = "0") @NonNull private int roFavorite;

    public int getRoId() {
        return roId;
    }

    public void setRoId(int roId) {
        this.roId = roId;
    }

    @NonNull
    public String getRoName() {
        return roName;
    }

    public void setRoName(@NonNull String roName) {
        this.roName = roName;
    }

    public String getRoInfo() {
        return roInfo;
    }

    public void setRoInfo(String roInfo) {
        this.roInfo = roInfo;
    }

    @NonNull
    public String getRoCreationTime() {
        return roCreationTime;
    }

    public void setRoCreationTime(@NonNull String roCreationTime) {
        this.roCreationTime = roCreationTime;
    }

    @NonNull
    public String getRoExerciseList() {
        return roExerciseList;
    }

    public void setRoExerciseList(@NonNull String roExerciseList) {
        this.roExerciseList = roExerciseList;
    }

    public int getRoFavorite() {
        return roFavorite;
    }

    public void setRoFavorite(int roFavorite) {
        this.roFavorite = roFavorite;
    }

    public int getRoUsedCount() {
        return roUsedCount;
    }

    public void setRoUsedCount(int roUsedCount) {
        this.roUsedCount = roUsedCount;
    }

    public String getRoLatestTime() {
        return roLatestTime;
    }

    public void setRoLatestTime(String roLatestTime) {
        this.roLatestTime = roLatestTime;
    }
}
