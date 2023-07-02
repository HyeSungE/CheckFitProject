package org.techtown.MyExerciseApp.Exercise;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.manager.SupportRequestManagerFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.text.StringEscapeUtils;
import org.techtown.MyExerciseApp.Adapter.Exercise.ExerciseCardListItemAdapter;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseCompleteBundle;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseTableInfo;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Interface.ClickCallbackListener;
import org.techtown.MyExerciseApp.Interface.SendEventListener;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.SharedPreferences.ExerciseSharedPreferences;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExerciseMainActivity extends AppCompatActivity implements SendEventListener {

    private Fragment fragment_exercise_list, fragment_routine_list, fragment_exercise_complete_share;
    private ExerciseMainActivity exerciseMainActivity;
    private MainActivity mainActivity;
    private AppDatabase appDatabase;
    private ArrayList<Exercise> exerciseArrayList = new ArrayList<>();
    private ProgressBar restTimeProgressBar;
    private TextView restTimeTextView, totalExerciseTimeTv, totalExerciseCountTv, totalExerciseWeightTv, exerciseTimeTextView;
    private Button addExerciseBt, addRoutineBt, exerciseCompleteBt, totalExerciseTimeStartBt;
    private long timeCountInMilliSeconds = 1 * 60000;
    private long BASIC_REST_TIME = 1 * 60000;
    private long exerciseRestTime;
    private RecyclerView exerciseCardRecyclerView;
    private CountDownTimer countDownTimer;
    private AlertDialog.Builder exerciseCompleteAlert_builder, editRestTimerAlert_builder,
            startTotalExerciseTimerAlert_builder, editTotalExerciseTimerAlert_builder;
    private ExerciseCardListItemAdapter exerciseCardListItemAdapter;
    private HashMap<Integer, Double> totalExerciseWeightHs;
    private Chronometer totalExerciseTimer;
    private TableRow totalExerciseTimeRow;
    private FrameLayout totalExerciseTimeFrl;
    private boolean timerState = true;
    private Long pauseOffset = 0L;
    private long totalExerciseTime = 0;
    private boolean doStartTotalExerciseTimerByCheckbox = true;
    private boolean backPressedHandled = false;

    private static final String SHARED_PREFS_NAME = "TimePrefs";
    private static final String CHRONOMETER_BASE_KEY = "chronometerBase";
    private static final String IS_CHRONOMETER_RUNNING_KEY = "isChronometerRunning";
    private ExerciseSharedPreferences exerciseSharedPreferences;

    private SoundPool soundPool;
    private int restTimerSuccessSound;

    private String startExerciseTime = null;

    ArrayList<RoutineInfoListItem> groupRoutine = null;

    private final ClickCallbackListener clickCallbackListener = new ClickCallbackListener() {
        @Override
        public void callBackRestTime(long restTime, boolean checked) {
            exerciseRestTime = restTime == 0 ? BASIC_REST_TIME : restTime; //쉬는 시간을 설정
            restTimeTextView.setText(hmsTimeFormatter(exerciseRestTime));
            try {
                startStop(checked);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void callRestTimerCancel() {
            setTimerValues(BASIC_REST_TIME);
            setProgressBarValues(timeCountInMilliSeconds);
            stopCountDownTimer();
        }

        @Override
        public void callExerciseCount(List<Exercise> items) {
            totalExerciseCountTv.setText(items.size() + " 종목");
        }

        //운동 메인 총 설정 운동량
        @Override
        public void callExerciseTotalWeight(int pos, double totalWeight) {
            double tempTotalWeight = Double.parseDouble(((String) totalExerciseWeightTv.getText()).split(" ")[0]);
            double changedVal = 0.0, oldVal = 0.0, newVal = totalWeight;
            if (totalExerciseWeightHs.get(pos) != null) {
                oldVal = totalExerciseWeightHs.get(pos);
                changedVal = newVal - oldVal;
                totalExerciseWeightHs.put(pos, totalWeight);
                tempTotalWeight = tempTotalWeight + changedVal > 0 ? tempTotalWeight + changedVal : 0;
            } else {
                totalExerciseWeightHs.put(pos, totalWeight);
                tempTotalWeight = tempTotalWeight + totalWeight > 0 ? tempTotalWeight + totalWeight : 0;
            }
            totalExerciseWeightTv.setText(tempTotalWeight + " kg");
        }

        @Override
        public void callTotalExerciseTimerStart() {
            if (doStartTotalExerciseTimerByCheckbox) {
                startTotalExerciseTimer();
                doStartTotalExerciseTimerByCheckbox = false;
            }
        }

        @Override
        public void callBackStartTime(String startTime) {
            if (startExerciseTime == null && totalExerciseTimer == null) {
                startExerciseTime = startTime;
            }
        }
    };

    // 클래스 레벨에서 dialogTotalExerciseTimer 선언
    private Chronometer dialogTotalExerciseTimer;
    //Listener역할을 할 Interface 생성
    public interface onBackPressedListener {
        public void onBackPressed();
    }
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if(!getSupportFragmentManager().getFragments().isEmpty()){
            for(Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof SupportRequestManagerFragment) continue;
                else if (fragment instanceof RoutineListFragment) {
                    ((RoutineListFragment) fragment).onBackPressed();   backPressedHandled = true;
                } else if (fragment instanceof ExerciseListFragment) {
                    ((ExerciseListFragment) fragment).onBackPressed();   backPressedHandled = true;
                } else if (fragment instanceof RoutineViewFragment) {
                    ((RoutineViewFragment) fragment).onBackPressed();   backPressedHandled = true;
                }
            }
        }

        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, "메인으로 이동합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ExerciseMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_main);
        init();
        setOnClickListener();
        createNotificationChannel();

        Intent intent = getIntent();
        if (intent != null) {
            groupRoutine = (ArrayList<RoutineInfoListItem>) intent.getSerializableExtra("groupRoutine");
            if (groupRoutine != null) {
                sendRoutine(groupRoutine);
            }
        }
        System.out.println("ExerciseMainActivity 실행");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("ExerciseMainActivity onPause");
        if (!exerciseSharedPreferences.getSelectedExerciseListPref().equals("selectedExerciseListEmpty")) {
            exerciseSharedPreferences.clearPref();
        }
        HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
        HashMap<Integer, RoutineInfoListItem> riHashMap = new LinkedHashMap<>();
        ArrayList<Exercise> list = new ArrayList<>();
        list.addAll(exerciseCardListItemAdapter.getItems());
        ArrayList<Exercise> arr = list;
        HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> exerciseList = exerciseCardListItemAdapter.getHashMap2();
        for (Exercise exercise : arr) {
            RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem();
            routineInfoListItem.setExercise(exercise);
            routineInfoListItem.setRoutineInfoListItemDetailedArrayList(exerciseList.get(exercise.getExId()));
            riHashMap.put(exercise.getExId(), routineInfoListItem);
            hashMap.put(new Gson().toJson(exercise, Exercise.class),
                    routineInfoListItem.getRoutineInfoListItemDetailedArrayList());
        }
        String exerciseListString = new Gson().toJson(hashMap);
        exerciseSharedPreferences.setSelectedExerciseListPref(exerciseListString);

        if (timerState) {
            totalExerciseTimer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - totalExerciseTimer.getBase();
        }
        Button startBt = (Button)findViewById(R.id.total_exercise_time_start_bt);
        if(startBt.getVisibility()==View.VISIBLE){
            exerciseSharedPreferences.setStartBtVisPref(true);
        }else{
            exerciseSharedPreferences.setStartBtVisPref(false);
        }
        exerciseSharedPreferences.setPauseOffSetPref(totalExerciseTimer.getBase());
        //기본 쉬는시간
        exerciseSharedPreferences.setDefaultRestTimePref(BASIC_REST_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("ExerciseMainActivity onResume");
        List<Exercise> list = new ArrayList<Exercise>();
        if (!exerciseSharedPreferences.getSelectedExerciseListPref().equals("selectedExerciseListEmpty")) {
            String routineString = exerciseSharedPreferences.getSelectedExerciseListPref();
            List<RoutineInfoListItem> goPostRoutineViewList = new ArrayList<RoutineInfoListItem>();
            HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
            HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> exerciseList = new HashMap<>();
            Type type = new TypeToken<HashMap<String, ArrayList<RoutineInfoListItemDetailed>>>() {
            }.getType();
            hashMap = new Gson().fromJson(routineString, type);
            for (String key : hashMap.keySet()) {
                RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem();
                routineInfoListItem.setExercise(new Gson().fromJson(key, Exercise.class));
                routineInfoListItem.setRoutineInfoListItemDetailedArrayList((ArrayList<RoutineInfoListItemDetailed>) hashMap.get(key));
                goPostRoutineViewList.add(routineInfoListItem);
            }

            for (RoutineInfoListItem routineInfoListItem : goPostRoutineViewList) {
                this.exerciseArrayList.add(routineInfoListItem.getExercise());
            }
            exerciseCardListItemAdapter.setRoutine(goPostRoutineViewList, exerciseCardListItemAdapter.getHashMap2());
            totalExerciseCountTv.setText(exerciseCardListItemAdapter.getItemCount() + " 종목");
        }

       //totalExerciseTimer
        if (exerciseSharedPreferences.hasKey(CHRONOMETER_BASE_KEY)) {
            totalExerciseTimeStartBt.setVisibility(View.GONE);
            long savedBase = exerciseSharedPreferences.getPauseOffSetPref();
            totalExerciseTimer.setBase(savedBase + pauseOffset);
            totalExerciseTimer.start();
            timerState = true;
        }
        //기본 쉬는시간
        BASIC_REST_TIME = exerciseSharedPreferences.getDefaultRestTimePref();

        Button startBt = (Button)findViewById(R.id.total_exercise_time_start_bt);
        boolean startBtVis = exerciseSharedPreferences.getStartBtVisPref();
        if(startBtVis){
            startBt.setVisibility(View.VISIBLE);
        }else{
            startBt.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    private void startStop(boolean checked) throws InterruptedException { //타이머가 시작하고 멈추는 기능

        if (checked) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            setTimerValues(exerciseRestTime);
            setProgressBarValues(timeCountInMilliSeconds);
            startCountDownTimer();
        } else {
            stopCountDownTimer();
        }
    }

    private void setTimerValues(long exerciseRestTime) {
        /* 타이머에 시간이 설정 되어있는지 체크 */
        timeCountInMilliSeconds = exerciseRestTime;
    }

    private void startCountDownTimer() { //카운트다운 시작 기능
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds + 150, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                restTimeTextView.setText(hmsTimeFormatter(millisUntilFinished));
                restTimeProgressBar.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                restTimeTextView.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                setProgressBarValues(timeCountInMilliSeconds);
                soundPool.play(restTimerSuccessSound, 1F, 1F, 0, 0, 1F);

                Intent goToExerciseMainIntent = new Intent(getApplicationContext(), ExerciseMainActivity.class);

                PendingIntent goToExerciseMainPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, goToExerciseMainIntent, 0);

                NotificationCompat.Builder notification_builder_rest = new NotificationCompat.Builder(getApplicationContext(),
                        "rest")
                        .setSmallIcon(R.drawable.do_exercises_icon)
                        .setContentTitle("쉬는시간 끝")
                        .setContentText("쉬는시간이 끝났어요 ! 다시 운동하러 가겠습니다 !")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(goToExerciseMainPendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notification_manager_rest = NotificationManagerCompat.from(getApplicationContext());
                notification_manager_rest.notify(327, notification_builder_rest.build());
            }
        };
        countDownTimer.start();
    }

    private void stopCountDownTimer() {// 카운트 다운 정지 및 초기화
        if (countDownTimer != null) countDownTimer.cancel();
        restTimeTextView.setText(hmsTimeFormatter(timeCountInMilliSeconds));
        setProgressBarValues(timeCountInMilliSeconds);
    }

    private void setProgressBarValues(long timeCountInMilliSeconds) { //원형 프로그레스 바에 값 세팅
        restTimeProgressBar.setMax((int) timeCountInMilliSeconds / 1000);
        restTimeProgressBar.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    private String hmsTimeFormatter(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds))
        );
    }

    private void init() {

        appDatabase = AppDatabase.getDBInstance(this.getApplicationContext());
        exerciseCompleteAlert_builder = new AlertDialog.Builder(this);
        editRestTimerAlert_builder = new AlertDialog.Builder(this);
        startTotalExerciseTimerAlert_builder = new AlertDialog.Builder(this);
        editTotalExerciseTimerAlert_builder = new AlertDialog.Builder(this);
        exerciseSharedPreferences = new ExerciseSharedPreferences(this);

        fragment_routine_list = new RoutineListFragment();
        restTimeProgressBar = findViewById(R.id.rest_time_progress_bar);
        restTimeTextView = findViewById(R.id.rest_time_text_view);
        restTimeTextView.setText(hmsTimeFormatter(BASIC_REST_TIME));
        addExerciseBt = findViewById(R.id.add_exercise_bt);
        addRoutineBt = findViewById(R.id.add_routine_bt);
        exerciseCompleteBt = findViewById(R.id.exercise_complete_bt);

        totalExerciseCountTv = findViewById(R.id.total_exercise_count);
        totalExerciseWeightTv = findViewById(R.id.total_exercise_weight);
        totalExerciseTimeStartBt = findViewById(R.id.total_exercise_time_start_bt);
        totalExerciseTimeFrl = findViewById(R.id.total_exercise_time_frl);
        totalExerciseTimeRow = findViewById(R.id.total_exercise_time_row);
        exerciseTimeTextView = findViewById(R.id.exercise_time_text_view);

        exerciseCardListItemAdapter = new ExerciseCardListItemAdapter();
        exerciseCardListItemAdapter.setLifecycleOwner(this);
        exerciseCardListItemAdapter.setCallbackListener(clickCallbackListener);
        exerciseCardRecyclerView = findViewById(R.id.exercise_card_recycler_view);
        exerciseCardRecyclerView.setAdapter(exerciseCardListItemAdapter);
        exerciseCardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseCardRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        exerciseCardRecyclerView.setLayoutManager(layoutManager);
        totalExerciseTimer = findViewById(R.id.dialog_total_exercise_time);
        totalExerciseTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                totalExerciseTime = SystemClock.elapsedRealtime() - cArg.getBase();
                cArg.setText(convertTimeToString(totalExerciseTime));
            }
        });
        totalExerciseWeightHs = new HashMap<>();

        soundPool = new SoundPool.Builder().build();

        restTimerSuccessSound = soundPool.load(this, R.raw.rest_timer_success_sound, 1);


    }

    private void setOnClickListener() {
        //운동 추가
        addExerciseBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment_exercise_list = new ExerciseListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from", "exercise");
                //bundle.putString("from","notHome");
                fragment_exercise_list.setArguments(bundle);
                replaceFragment_addToBack(R.id.exercie_main_fr_layout, fragment_exercise_list);
            }
        });
        //루틴 추가
        addRoutineBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("mode", "select");
                bundle.putString("from", "notHome");
                bundle.putString("from_2", "exerciseMain");

                fragment_routine_list.setArguments(bundle);
                replaceFragment_addToBack(R.id.exercie_main_fr_layout, fragment_routine_list);
            }
        });
        //운동 완료
        exerciseCompleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exerciseCardListItemAdapter.getItems().size() <= 0) {
                    showAlertSimpleMessage(ExerciseMainActivity.this, "알림", "운동을 1개 이상 추가해주세요 !");
                } else {
                    exerciseCompleteAlert_builder.setMessage("정말로 운동을 완료 하시겠습니까?").setTitle("운동 완료")
                            .setPositiveButton("완료", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    goToExerciseCompleteShare();
                                }
                            })
                            .setNegativeButton("돌아가기", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //nothing
                                }
                            });
                    AlertDialog alertDialog_exerciseComplete = exerciseCompleteAlert_builder.create();

                    alertDialog_exerciseComplete.show();
                }

            }
        });
        //쉬는시간 타이머
        restTimeProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //타이머 다이어로그 올리기
                //전체 휴식시간 설정 : a분 b초
                //소리 설정 : 소리를 낼 것인지 안 낼 것인지
                //각 운동에 설정된 휴식시간으로 설정 : 각 운동에 설정된 휴식 시간이 있으면 그 휴식 시간으로 없으면 기본 1분(60초)
                // 운동 완료를 체크할 때마다 타이머 초기화하고 시작

                View editRestTimeView = getLayoutInflater().inflate(R.layout.edit_rest_timer_dialog, null);
                EditText editRestTimeMinute = editRestTimeView.findViewById(R.id.edit_rest_time_minute);
                ToggleButton edit_rest_time_sound_alarm = editRestTimeView.findViewById(R.id.edit_rest_time_sound_alarm);

                EditText editRestTimeSecond = editRestTimeView.findViewById(R.id.edit_rest_time_second);

                Button editRestTimeCompleteBt = editRestTimeView.findViewById(R.id.edit_exercise_complete_bt);
                /* 1000 = 1초 // 60초 = 60 000  ex) 69856 / 60000 = 1 9856 9856 / 1000 = 9 */
                editRestTimeMinute.setText(String.valueOf((int) (BASIC_REST_TIME / 60000)), TextView.BufferType.EDITABLE);
                editRestTimeSecond.setText(String.valueOf(((int) (BASIC_REST_TIME % 60000)) / 1000), TextView.BufferType.EDITABLE);
                editRestTimerAlert_builder.setView(editRestTimeView);
                AlertDialog alertDialog_editRestTime = editRestTimerAlert_builder.create();
                alertDialog_editRestTime.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                alertDialog_editRestTime.show();


                edit_rest_time_sound_alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            soundPool.setVolume(restTimerSuccessSound, 1, 1);
                            Toast.makeText(ExerciseMainActivity.this, "휴시시간 타이머 완료 소리가 들립니다", Toast.LENGTH_SHORT).show();
                        } else {
                            soundPool.setVolume(restTimerSuccessSound, 0, 0);
                            Toast.makeText(ExerciseMainActivity.this, "휴시시간 타이머 완료 소리가 안들립니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                editRestTimeCompleteBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String minute = String.valueOf(editRestTimeMinute.getText());
                        String second = String.valueOf(editRestTimeSecond.getText());
                        if (minute.isEmpty()) minute = "0";
                        if (second.isEmpty()) second = "0";

                        BASIC_REST_TIME = Integer.parseInt(minute) * 60000 + Integer.parseInt(second) * 1000;
                        restTimeTextView.setText(hmsTimeFormatter(BASIC_REST_TIME));

                        if (edit_rest_time_sound_alarm.isChecked()) {
                            soundPool.setVolume(restTimerSuccessSound, 1, 1);
                            Toast.makeText(ExerciseMainActivity.this, "휴시시간 타이머 완료 소리가 들립니다", Toast.LENGTH_SHORT).show();
                        } else {
                            soundPool.setVolume(restTimerSuccessSound, 0, 0);
                            Toast.makeText(ExerciseMainActivity.this, "휴시시간 타이머 완료 소리가 안들립니다", Toast.LENGTH_SHORT).show();
                        }

                        alertDialog_editRestTime.hide();

                    }
                });

            }
        });
        //운동 타이머
        totalExerciseTimeStartBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //운동 타이머 시작
                startTotalExerciseTimer();
            }
        });

        //운동 타이머 설정
        totalExerciseTimeRow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                long elapsedMillis;
                totalExerciseTimer.stop();
                View editTotalExerciseTimerView = getLayoutInflater().inflate(R.layout.edit_total_exercise_timer_dialog, null);
                Chronometer dialogTotalExerciseTimer = editTotalExerciseTimerView.findViewById(R.id.dialog_total_exercise_time);
                if (pauseOffset == null || pauseOffset == 0) {
                    elapsedMillis = totalExerciseTimer.getBase();
                } else {
                    elapsedMillis = SystemClock.elapsedRealtime() - pauseOffset;
                }
                dialogTotalExerciseTimer.setBase(elapsedMillis);
                setChronometerFormat(dialogTotalExerciseTimer, SystemClock.elapsedRealtime() - elapsedMillis);

                dialogTotalExerciseTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                        setChronometerFormat(chronometer, time);
                    }
                });
                if (timerState) dialogTotalExerciseTimer.start();
                else dialogTotalExerciseTimer.stop();

                ImageButton startBt = editTotalExerciseTimerView.findViewById(R.id.edit_total_exercise_timer_start_bt);
                ImageButton resetBt = editTotalExerciseTimerView.findViewById(R.id.edit_total_exercise_timer_reset_bt);
                EditText upDownMinute = editTotalExerciseTimerView.findViewById(R.id.edit_total_exercise_timer_up_down_minute);
                Button upMinuteBt = editTotalExerciseTimerView.findViewById(R.id.edit_total_exercise_timer_up_bt);
                Button downMinuteBt = editTotalExerciseTimerView.findViewById(R.id.edit_total_exercise_timer_down_bt);
                Button okBt = editTotalExerciseTimerView.findViewById(R.id.edit_total_exercise_timer_ok_bt);

                editTotalExerciseTimerAlert_builder.setView(editTotalExerciseTimerView);
                AlertDialog alertDialog_editTotalExerciseTimer = editTotalExerciseTimerAlert_builder.create();
                alertDialog_editTotalExerciseTimer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                alertDialog_editTotalExerciseTimer.show();


                View.OnClickListener clickEvent = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String minuteStr = upDownMinute.getText().toString();
                        long time, elapsedMillis;
                        int minute;

                        switch (view.getId()) {
                            case R.id.edit_total_exercise_timer_start_bt:
                                if (timerState) {
                                    //일시정지 상태 - 재생 이미지로 바꿈
                                    dialogTotalExerciseTimer.stop();
                                    pauseOffset = SystemClock.elapsedRealtime() - dialogTotalExerciseTimer.getBase();
                                    startBt.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                                    setResetButton(resetBt, true);
                                    timerState = false;
                                } else {
                                    //재생 상태 - 일시정지 이미지로 바꿈
                                    dialogTotalExerciseTimer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                                    dialogTotalExerciseTimer.start();
                                    startBt.setImageResource(R.drawable.ic_baseline_pause_24);
                                    setResetButton(resetBt, false);
                                    timerState = true;
                                    pauseOffset = 0L;
                                }
                                break;
                            case R.id.edit_total_exercise_timer_reset_bt:
                                dialogTotalExerciseTimer.setBase(SystemClock.elapsedRealtime());
                                setChronometerFormat(dialogTotalExerciseTimer, -1000);
                                time = 0;
                                pauseOffset = 0L;
                                dialogTotalExerciseTimer.stop();
                                timerState = false;

                                break;
                            case R.id.edit_total_exercise_timer_up_bt:
                                if (!minuteStr.equals("")) {
                                    minute = Integer.parseInt(minuteStr);
                                    elapsedMillis = SystemClock.elapsedRealtime() - dialogTotalExerciseTimer.getBase();
                                    time = SystemClock.elapsedRealtime() - elapsedMillis - 1000 * 60 * minute;
                                    dialogTotalExerciseTimer.setBase(time);
                                    setChronometerFormat(dialogTotalExerciseTimer, SystemClock.elapsedRealtime() - time);
                                }
                                break;

                            case R.id.edit_total_exercise_timer_down_bt:
                                if (!minuteStr.equals("")) {
                                    minute = Integer.parseInt(minuteStr);
                                    elapsedMillis = SystemClock.elapsedRealtime() - dialogTotalExerciseTimer.getBase();
                                    time = SystemClock.elapsedRealtime() - elapsedMillis + 1000 * 60 * minute;
                                    if (time >= 218197890) time = SystemClock.elapsedRealtime();
                                    dialogTotalExerciseTimer.setBase(time);
                                    setChronometerFormat(dialogTotalExerciseTimer, SystemClock.elapsedRealtime() - time);
                                }
                                break;
                            case R.id.edit_total_exercise_timer_ok_bt:
                                alertDialog_editTotalExerciseTimer.dismiss();
                                break;
                        }

                    }
                };
                if (timerState) {
                    startBt.setImageResource(R.drawable.ic_baseline_pause_24);
                    setResetButton(resetBt, false);
                } else {
                    startBt.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    setResetButton(resetBt, true);
                }

                startBt.setOnClickListener(clickEvent);
                resetBt.setOnClickListener(clickEvent);
                upMinuteBt.setOnClickListener(clickEvent);
                downMinuteBt.setOnClickListener(clickEvent);
                okBt.setOnClickListener(clickEvent);

                //다이어로그 크기 조절 : show() 이후에 !
                WindowManager.LayoutParams params = alertDialog_editTotalExerciseTimer.getWindow().getAttributes();
                DisplayMetrics dm = alertDialog_editTotalExerciseTimer.getContext().getResources().getDisplayMetrics();
                int widthDp = Math.round(270 * dm.density);
                int heightDp = Math.round(290 * dm.density);
                params.width = widthDp;
                params.height = heightDp;
                alertDialog_editTotalExerciseTimer.getWindow().setAttributes(params);

                //다이어로그 종료 시, 이벤트 설정
                alertDialog_editTotalExerciseTimer.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        dialogTotalExerciseTimer.stop();
                        if (timerState) {
                            long time = SystemClock.elapsedRealtime() - dialogTotalExerciseTimer.getBase();
                            totalExerciseTimer.setBase(SystemClock.elapsedRealtime() - time);
                            totalExerciseTimer.start();
                        } else {

                            totalExerciseTimer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                            setChronometerFormat(totalExerciseTimer, SystemClock.elapsedRealtime() - totalExerciseTimer.getBase());
                            totalExerciseTimer.stop();
                        }
                    }
                });
            }
        });
    }
    private void startTotalExerciseTimer() {
        if (totalExerciseTimeStartBt.getVisibility() == View.VISIBLE) {
            startTotalExerciseTimerAlert_builder.setMessage("운동 타이머를 시작하시겠습니까 ?").setTitle("운동 타이머")
                    .setPositiveButton("시작", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 시작 시, 운동 시작 버튼 사라지고, 운동 타이머 시작
                            totalExerciseTimeStartBt.setVisibility(View.INVISIBLE);
                            //운동 타이머 시작
                            //totalExerciseTimer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                            totalExerciseTimer.setBase(SystemClock.elapsedRealtime());
                            totalExerciseTimer.start();
                            startExerciseTime = getToday();
                            exerciseTimeTextView.setBackgroundResource(R.drawable.start_right_side_line);
                            totalExerciseTimer.setBackgroundResource(R.drawable.start_left_side_line);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            AlertDialog alertDialog_startToTalExerciseTimer = startTotalExerciseTimerAlert_builder.create();
            alertDialog_startToTalExerciseTimer.setCancelable(false);
            alertDialog_startToTalExerciseTimer.show();
        }

    }
    private String convertTimeToString(long time) {
        int hours = (int) (time / (60 * 60 * 1000));
        int minutes = (int) ((time % (60 * 60 * 1000)) / (60 * 1000));
        int seconds = (int) ((time % (60 * 1000)) / 1000);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
    //운동 타이머 리셋 버튼 셋팅
    private void setResetButton(ImageButton resetButton, boolean enabled) {
        resetButton.setEnabled(enabled);
        if (enabled) {
            resetButton.setBackgroundResource(R.drawable.circle_button_background);
        } else {
            resetButton.setBackgroundResource(R.drawable.circle_button_background_disabled);
        }

    }
    public boolean replaceFragment(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commit();
        return true;
    }
    public boolean replaceFragment_addToBack(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(viewId, fragment).addToBackStack(null).commit();
        return true;
    }
    @Override
    public void sendExerciseList(ArrayList<Exercise> selectedExerciseArrayList) {
        if (this.exerciseArrayList.isEmpty()) {
            this.exerciseArrayList = new ArrayList<>();
            this.exerciseArrayList.addAll(selectedExerciseArrayList);
            exerciseCardListItemAdapter.setItem(exerciseArrayList);
        } else {
            int exerciseArrayListSize = exerciseArrayList.size();
            for(int i = 0; i < selectedExerciseArrayList.size(); i++) {
                exerciseCardListItemAdapter.addItem(selectedExerciseArrayList.get(i), exerciseArrayListSize);
                exerciseArrayListSize+= 1;
            }
        }
        //운동 개수 추가
        totalExerciseCountTv.setText(exerciseCardListItemAdapter.getItemCount() + " 종목");
    }
    @Override
    public void sendRoutine(List<RoutineInfoListItem> routineInfoListItemArrayList) {
        //운동 개수 추가
        for (RoutineInfoListItem routineInfoListItem : routineInfoListItemArrayList) {
            if (this.exerciseArrayList.isEmpty()) {
                exerciseArrayList.add(routineInfoListItem.getExercise());
            } else {
                Iterator<Exercise> iterator = this.exerciseArrayList.iterator();
                boolean isExerciseExists = false;

                while (iterator.hasNext()) {
                    Exercise exercise = iterator.next();

                    if (exercise.getExName().equals(routineInfoListItem.getExercise().getExName())) {
                        isExerciseExists = true;
                        break;
                    }
                }

                if (!isExerciseExists) {
                    exerciseArrayList.add(routineInfoListItem.getExercise());
                }
            }
        }
        exerciseCardListItemAdapter.setRoutine(routineInfoListItemArrayList,exerciseCardListItemAdapter.getHashMap2());
        totalExerciseCountTv.setText(exerciseCardListItemAdapter.getItemCount() + " 종목");
    }
    @Override
    public void sendExerciseListToRoutine(ArrayList<Exercise> exerciseArrayList) {}

    //간단한 알림 메시지 다이어로그
    private void showAlertSimpleMessage(Context context, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(content);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private String getToday(){
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) { //fragment 로 전달
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
    private String removeQuotesAndUnescape(String uncleanJson) {
        String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");
        return StringEscapeUtils.unescapeJava(noQuotes);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "restTimer";
            String description = "restTimerSuccessNotification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("rest", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    private void goToExerciseCompleteShare() {
        fragment_exercise_complete_share = new ExerciseCompleteShare();
        //총 운동시간, 총 운동량, 설정 운동량, 총 운동 종목 수, 운동 리스트
        HashMap<Integer,ExerciseTableInfo> hashMap = exerciseCardListItemAdapter.getHashMap();
        //타이머 정지
        if(totalExerciseTimer!=null)totalExerciseTimer.stop();
        if(countDownTimer != null) countDownTimer.cancel();
        long totalExerciseTimerPauseOffset = SystemClock.elapsedRealtime() -  totalExerciseTimer.getBase();
        restTimeTextView.setText(hmsTimeFormatter(BASIC_REST_TIME));
        setProgressBarValues(timeCountInMilliSeconds);
        ExerciseCompleteBundle exerciseCompleteBundle = new ExerciseCompleteBundle(hashMap,totalExerciseTimerPauseOffset,
                totalExerciseTimer.getText().toString(),totalExerciseWeightTv.getText().toString(),totalExerciseCountTv.getText().toString());

        Bundle bundle = new Bundle();
        bundle.putSerializable("exerciseCompleteBundle", exerciseCompleteBundle);
        bundle.putSerializable("exerciseCompleteBundle2", exerciseCardListItemAdapter.getHashMap2());
        ArrayList<Exercise> list = new ArrayList<>();
        list.addAll(exerciseCardListItemAdapter.getItems());
        bundle.putSerializable("exerciseCompleteBundle3", list);
        bundle.putString("startExerciseTime",startExerciseTime);
        fragment_exercise_complete_share.setArguments(bundle);
        replaceFragment_addToBack(R.id.exercie_main_fr_layout, fragment_exercise_complete_share);
    }
    private void setChronometerFormat(Chronometer chronometer, long time) {
        time = time + 1000;
        int h = (int) (time / 3600000);
        if (h < 0) h = 0;
        int m = (int) (time - h * 3600000) / 60000;
        if (m < 0) m = 0;
        int s = (int) (time - h * 3600000 - m * 60000) / 1000;
        if (s < 0) s = 0;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";
        chronometer.setText(hh + ":" + mm + ":" + ss);
    }

}