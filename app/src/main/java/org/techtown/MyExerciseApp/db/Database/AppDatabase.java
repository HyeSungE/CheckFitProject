package org.techtown.MyExerciseApp.db.Database;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.firebase.auth.FirebaseAuth;

import org.techtown.MyExerciseApp.db.DAO.ExerciseDao;
import org.techtown.MyExerciseApp.db.DAO.ExerciseMemoDao;
import org.techtown.MyExerciseApp.db.DAO.ExerciseRecordDao;
import org.techtown.MyExerciseApp.db.DAO.RoutineDao;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.techtown.MyExerciseApp.db.Entity.ExerciseMemo;
import org.techtown.MyExerciseApp.db.Entity.ExerciseRecord;
import org.techtown.MyExerciseApp.db.Entity.Routine;

import java.io.File;


@Database(entities = {Exercise.class, ExerciseRecord.class, ExerciseMemo.class, Routine.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();
    public abstract ExerciseMemoDao exerciseMemoDao();
    public abstract ExerciseRecordDao exerciseRecordDao();
    public abstract RoutineDao routineDao();

    private File fiie;
    private static AppDatabase instance;
    public AppDatabase(){

    }

    public AppDatabase(File file){

    }

    @SuppressLint("SdCardPath")
    public static AppDatabase getDBInstance(Context context){
        if(instance == null) {
            synchronized (AppDatabase.class){
                instance = Room.databaseBuilder(context,
                                AppDatabase.class,"EXERCISE_"+
                                FirebaseAuth.getInstance().getCurrentUser().getUid()
                                        +".db")
                        .createFromFile(new File("/data/data/org.techtown.MyExerciseApp/database/EXERCISE_"+
                                FirebaseAuth.getInstance().getCurrentUser().getUid()
                                +".db"))
                       // .createFromAsset("database/EXERCISE.db") //데이터 로드
                        .build();
            }
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public static void renewInstance(Context context){
        if(AppDatabase.getDBInstance(context)!=null) AppDatabase.getDBInstance(context).close();
        instance = Room.databaseBuilder(context,
                        AppDatabase.class,"EXERCISE_"+
                                FirebaseAuth.getInstance().getCurrentUser().getUid()
                                +".db")
                .createFromFile(new File("/data/data/org.techtown.MyExerciseApp/databases/EXERCISE_"+
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                        +".db"))
                // .createFromAsset("database/EXERCISE.db") //데이터 로드
                .build();

    }

}