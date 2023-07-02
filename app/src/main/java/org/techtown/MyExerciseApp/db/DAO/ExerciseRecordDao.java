package org.techtown.MyExerciseApp.db.DAO;

import androidx.room.Dao;
import androidx.room.Query;

import org.techtown.MyExerciseApp.db.Entity.ExerciseRecord;

import java.util.List;

@Dao
public interface ExerciseRecordDao {

    @Query("SELECT COUNT(DISTINCT b),COUNT(*) FROM (SELECT substr(er_date,0,10) as b  from EXERCISE_RECORD_TB),EXERCISE_RECORD_TB")
    CountTotalDayAndTimes countTotalDayAndTime();
    @Query("SELECT COUNT(*) FROM EXERCISE_RECORD_TB")
    int getCountRecord();
    @Query("SELECT er_date FROM EXERCISE_RECORD_TB where er_round = 1 ORDER BY er_date DESC")
    List<String> getExerciseStartDate();
    @Query("INSERT INTO EXERCISE_RECORD_TB(" +
            "er_date,er_start_date,er_round,er_total_set,er_total_weight,er_total_time,er_smile_tag,er_total_memo,er_exercise_list) " +
            "values (:erDate,:erStartDate, :erRound ,:erTotalSet,:erTotalWeight,:erTotalTime,:erSmileTag,:erTotalMemo,:erExerciseList)")
    long insertExerciseRecord(String erDate,String erStartDate,int erRound,int
            erTotalSet,double erTotalWeight,String erTotalTime,String erSmileTag,String erTotalMemo,String erExerciseList);

    @Query("UPDATE EXERCISE_RECORD_TB SET er_date =:erDate" +
            ",er_start_date=:erStartDate" +
            ",er_round=:erRound" +
            ",er_total_set=:erTotalSet" +
            ",er_total_weight=:erTotalWeight" +
            ",er_total_time=:erTotalTime" +
            ",er_smile_tag=:erSmileTag" +
            ",er_total_memo=:erTotalMemo" +
            ",er_exercise_list=:erExerciseList WHERE er_id =:erId")
    int updateExerciseRecord(String erDate,String erStartDate,int erRound,int
            erTotalSet,double erTotalWeight,String erTotalTime,String erSmileTag,String erTotalMemo,String erExerciseList,int erId);


    @Query("SELECT COUNT(*) FROM EXERCISE_RECORD_TB where er_date like :erDate||'%'")
    int getExerciseRound(String erDate);

    @Query("SELECT * FROM EXERCISE_RECORD_TB ")
    List<ExerciseRecord> getAllExerciseRecord();

    @Query("SELECT * FROM EXERCISE_RECORD_TB WHERE er_date between DATE(:stDate) AND DATE(:edDate)")
    List<ExerciseRecord> getWeekExerciseRecord(String stDate,String edDate);

    @Query("SELECT * FROM EXERCISE_RECORD_TB where er_exercise_list like '%'||:exerciseName||'%'")
    List<ExerciseRecord> getSelectedExerciseRecord(String exerciseName);


}
