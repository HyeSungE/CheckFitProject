package org.techtown.MyExerciseApp.Home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.techtown.MyExerciseApp.Exercise.ExerciseMainActivity;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.SharedPreferences.ExerciseSharedPreferences;
import org.techtown.MyExerciseApp.db.DAO.CountTotalDayAndTimes;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HomeTodayExerciseFragment extends Fragment {

    private MainActivity mainActivity;
    private ImageButton exerciseStartBt, exerciseEditBt;
    private TextView todayTv, exerciseTotalDayTv, exerciseConsecutiveDayTv;
    private AppDatabase appDatabase;
    private int totalDay, totalTimes,totalTimes_1;
    private List<String> erDateList;
    private ExerciseSharedPreferences exerciseSharedPreferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        appDatabase = AppDatabase.getDBInstance(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home_today_exercise, container, false);
        exerciseStartBt = rootView.findViewById(R.id.exercise_start_bt);

        todayTv = rootView.findViewById(R.id.today_tv);
        exerciseTotalDayTv = rootView.findViewById(R.id.exercise_total_day_tv);
        exerciseConsecutiveDayTv = rootView.findViewById(R.id.exercise_consecutive_day_tv);
        //오늘 날짜
        todayTv.setText(getToday());

        exerciseSharedPreferences = new ExerciseSharedPreferences(requireActivity().getApplicationContext());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CountTotalDayAndTimes countTotalDayAndTimes
                        = appDatabase.exerciseRecordDao().countTotalDayAndTime();
                totalDay = countTotalDayAndTimes.getGetTotalExerciseDay();
                totalTimes = countTotalDayAndTimes.getGetTotalExerciseTimes();
                totalTimes_1 = appDatabase.exerciseRecordDao().getCountRecord();
                erDateList = appDatabase.exerciseRecordDao().getExerciseStartDate();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        exerciseTotalDayTv.setText(new String(Character.toChars(0x1F4E3)) + " ⚟ " +
                                totalDay + "일 출석하여 총 " + totalTimes_1 + "번 운동했습니다. " //메가폰  : U+1F4E3 -> 0x1F4E3
                        );
                        exerciseConsecutiveDayTv.setText(new String(Character.toChars(0x1F525))
                                + countConsecutiveTimes(erDateList) + "일 동안 운동하고 있어요" +
                                new String(Character.toChars(0x1F525)));
                    }
                });
                SystemClock.sleep(100);

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();


        exerciseStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!exerciseSharedPreferences.getSelectedExerciseListPref().equals("selectedExerciseListEmpty")) {
                    AlertDialog.Builder doExerciseStart_builder = new AlertDialog.Builder(requireActivity());
                    doExerciseStart_builder.setTitle("알림").setMessage("운동을 다시 시작하시겠습니까 ?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    exerciseSharedPreferences.clearPref();
                                    goToExerciseMainActivity();
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    goToExerciseMainActivity();
                                }
                            });
                    AlertDialog alertDialog_doExerciseStart = doExerciseStart_builder.create();
                    alertDialog_doExerciseStart.show();
                }else{
                    Intent intent = new Intent(mainActivity.getApplicationContext(), ExerciseMainActivity.class);
                    startActivity(intent);

                }


            }
        });
        System.out.println("HomeTodayExerciseFragment 실행");
        return rootView;
    }

    private void goToExerciseMainActivity() {
        Intent intent = new Intent(mainActivity.getApplicationContext(), ExerciseMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private int countConsecutiveTimes(List<String> erDateList) {
        if(erDateList == null || erDateList.size()==0) return 0;
        int consecutiveDay = 1;
        long now = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        String today = simpleDateFormat.format(new Date(now));
        String yesterday = simpleDateFormat.format(new Date(now - 24 * 60 * 60 * 1000));

        String startDay = erDateList.get(0);

        if (startDay.equals(today) || startDay.equals(yesterday)) {
            for (int i = 0; i < erDateList.size() - 1; i++) {
                try {
                    Date firstDate = dateFormat.parse(erDateList.get(i));
                    Date SecondDate = dateFormat.parse(erDateList.get(i + 1));
                    long diffDate = (firstDate.getTime() - SecondDate.getTime()) / (24 * 60 * 60 * 1000);
                    if (diffDate == 1) consecutiveDay += 1;
                    else break;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return consecutiveDay;
    }

    //오늘의 날짜 스트링으로 반환
    private String getToday() {
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }

}