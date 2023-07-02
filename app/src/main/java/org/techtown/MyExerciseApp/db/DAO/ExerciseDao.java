package org.techtown.MyExerciseApp.db.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM EXERCISE_TB")
    LiveData<List<Exercise>> getExerciseList();

    @Query("SELECT * FROM EXERCISE_TB WHERE ex_id =:exId")
    Exercise getExercise(int exId);

    @Query("select count(*) from EXERCISE_TB")
    int countExerciseList();

    @Query("SELECT * FROM EXERCISE_TB where ex_bodypart = :exBodypart")
    LiveData<List<Exercise>> getExerciseByBodypart(String exBodypart);

    @Query("SELECT * FROM EXERCISE_TB where ex_favorite = 1")
    LiveData<List<Exercise>> getExerciseFavorite();

    @Query("SELECT * FROM (SELECT * FROM EXERCISE_TB where ex_bodypart = :exBodypart) where ex_name like '%'||:searchString||'%' " +
            "OR ex_bodypart like '%'||:searchString||'%' " +
            "OR ex_equipment like '%'||:searchString||'%'")
    LiveData<List<Exercise>> searchExercise(String searchString,String exBodypart);

    @Query("SELECT * FROM (SELECT * FROM EXERCISE_TB where ex_favorite = 1) where ex_name like '%'||:searchString||'%' " +
            "OR ex_bodypart like '%'||:searchString||'%' " +
            "OR ex_equipment like '%'||:searchString||'%'")
    LiveData<List<Exercise>> searchExerciseFavorite(String searchString);

    @Query("UPDATE EXERCISE_TB SET ex_rest_time =:exRestTime,ex_weight_unit = :exWeightUnit WHERE ex_id = :exId")
    int updateEditExercise(int exRestTime,String exWeightUnit,int exId);

    @Query("INSERT INTO EXERCISE_TB(ex_name,ex_bodypart,ex_equipment,ex_type) " +
            "values (:exName, :exBodypart, :exEquipment, :exType)")
    long insertExercise(String exName,String exBodypart,String exEquipment,String exType);

    @Query("INSERT INTO EXERCISE_TB(ex_name,ex_bodypart,ex_equipment,ex_type,ex_weight_unit) " +
            "values (:exName, :exBodypart, :exEquipment, :exType, :exWeightUnit)")
    long insertExercise(String exName,String exBodypart,String exEquipment,String exType,String exWeightUnit);

    @Query("UPDATE EXERCISE_TB SET ex_name =:exName,ex_bodypart=:exBodypart,ex_equipment=:exEquipment,ex_type=:exType WHERE ex_id = :exId")
    int updateCustomExercise(int exId,String exName,String exBodypart,String exEquipment,String exType);

    @Query("UPDATE EXERCISE_TB SET ex_name =:exName,ex_bodypart=:exBodypart,ex_equipment=:exEquipment,ex_type=:exType, ex_weight_unit=:exWeightUnit WHERE ex_id = :exId")
    int updateCustomExercise(int exId,String exName,String exBodypart,String exEquipment,String exType,String exWeightUnit);

    @Query("UPDATE EXERCISE_TB SET ex_total_set = ex_total_set + :totalSet,ex_total_weight = ex_total_weight + :totalWeight" +
            ",ex_frequency =ex_frequency +1 ,ex_total_days=ex_total_days + 1 WHERE ex_id = :exId")
    int updateCompleteExercise(int exId,int totalSet,double totalWeight);

    @Query("UPDATE EXERCISE_TB SET ex_favorite =:favorite WHERE ex_id = :exId")
    int updateFavorite(int exId,int favorite);

    @Query("DELETE FROM EXERCISE_TB WHERE ex_id = :exId")
    void deleteExercise(int exId);
}
