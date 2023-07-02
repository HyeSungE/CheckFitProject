package org.techtown.MyExerciseApp.db.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.techtown.MyExerciseApp.db.Entity.ExerciseMemo;

import java.util.List;

@Dao
public interface ExerciseMemoDao {
    @Query("SELECT * FROM EXERCISE_MEMO_TB WHERE DATE(erim_write_time) <= date('now','localtime') AND ex_id = :ex_id ORDER BY DATE(erim_write_time) DESC")
    LiveData<List<ExerciseMemo>> getExerciseMemoList(int ex_id);

    @Query("SELECT COUNT(*) FROM EXERCISE_MEMO_TB WHERE erim_write_time = :erim_write_time AND ex_id = :ex_id")
    int hasTodayMemo(String erim_write_time, int ex_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMemo(ExerciseMemo exerciseMemo);

    @Query("DELETE FROM EXERCISE_MEMO_TB WHERE erim_write_time = :erim_write_time AND ex_id = :ex_id")
    void deleteMemo(String erim_write_time, int ex_id);

}
