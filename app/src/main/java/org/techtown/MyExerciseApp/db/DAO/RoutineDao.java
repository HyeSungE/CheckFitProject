package org.techtown.MyExerciseApp.db.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.techtown.MyExerciseApp.db.Entity.Routine;

import java.util.List;

@Dao
public interface RoutineDao {

    @Query("SELECT * FROM ROUTINE_TB ORDER BY ro_creation_time DESC")
    LiveData<List<Routine>> getRoutineList();

    @Query("SELECT * FROM ROUTINE_TB where ro_favorite = 1")
    LiveData<List<Routine>> getRoutineFavorite();

    @Query("SELECT * FROM (SELECT * FROM ROUTINE_TB ORDER BY ro_latest_time DESC) where ro_latest_time != '-'")
    LiveData<List<Routine>> getRoutineLatest();

    @Query("INSERT INTO ROUTINE_TB(" +
            "ro_name,ro_info,ro_creation_time,ro_exercise_list) " +
            "values (:roName,:roInfo, :roCreationTime ,:roExerciseList)")
    long insertRoutine(String roName,String roInfo,String roCreationTime,String roExerciseList);

    @Query("UPDATE ROUTINE_TB SET ro_used_count = ro_used_count + 1 , ro_latest_time = :today WHERE ro_id = :roId")
    int updateUsedRoutine(int roId, String today);

    @Query("UPDATE ROUTINE_TB SET ro_exercise_list = :roExerciseList , ro_name = :roName WHERE ro_id = :roId")
    int updateSameRoutine(int roId, String roExerciseList,String roName);

    @Query("SELECT * FROM ROUTINE_TB where ro_name like '%'||:searchString||'%'")
    LiveData<List<Routine>> searchRoutine(String searchString);

    @Query("SELECT * FROM (SELECT * FROM ROUTINE_TB where ro_favorite = 1) where ro_name like '%'||:searchString||'%'")
    LiveData<List<Routine>> searchRoutineFavorite(String searchString);

    @Query("SELECT * FROM  (SELECT * FROM ROUTINE_TB ORDER BY ro_latest_time DESC) where ro_name like '%'||:searchString||'%' and ro_latest_time != '-'" )
    LiveData<List<Routine>> searchRoutineLatest(String searchString);

    @Query("UPDATE ROUTINE_TB SET ro_favorite =:favorite WHERE ro_id = :roId")
    int updateFavorite(int roId,int favorite);

    @Query("DELETE FROM ROUTINE_TB  WHERE ro_id = :roId")
    int deleteRoutine(int roId);
}
