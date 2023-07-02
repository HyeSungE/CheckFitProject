package org.techtown.MyExerciseApp.Exercise;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.techtown.MyExerciseApp.Adapter.Exercise.CompleteExerciseListAdapter;
import org.techtown.MyExerciseApp.Data.Exercise.CompleteExerciseItem;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseCompleteBundle;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseTableInfo;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Feed.PostActivity;
import org.techtown.MyExerciseApp.Home.HomeFragment;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.SharedPreferences.ExerciseSharedPreferences;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.techtown.MyExerciseApp.db.Entity.ExerciseRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class ExerciseCompleteShare extends Fragment implements MainActivity.onBackPressedListener{

    private TextView exerciseTimeTv, exerciseCountTv, exerciseWeightTv, exerciseRoundTv,kgtv;
    private Button completeBt, editBt, shareBt, saveRoutineBt;
    private EditText completeMemoEt;
    private RecyclerView completeRv;
    private Spinner completeRoundSpinner;
    private CheckBox summarySet;

    private ExerciseCompleteBundle exerciseCompleteBundle;
    private HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> exerciseCompleteBundle2;
    private ArrayList<Exercise> exerciseCompleteBundle3;
    private CompleteExerciseListAdapter completeExerciseListAdapter;

    private String erStartDate, summaryMode;
    private AppDatabase appDatabase;
    private long insertRes;
    private OnBackPressedCallback onBackPressedCallback;
    private ImageView erExerciseSmile;
    private TableLayout completeSmileTable;
    private AnimationSet vibrateAnimationSet;
    private Handler handler;
    private ArrayList<ExerciseTableInfo> list;
    private String erExerciseSmileTag, routineDialogName, exerciseListString;
    private long insertResult;
    private String fragmentMode = "edit";
    private LinearLayout routineNameDialog;
    private ExerciseSharedPreferences exerciseSharedPreferences;
    private Fragment savePhotoFragment;

    private HashMap<Integer,Double> exercise1rmMap = new HashMap<Integer,Double>();
    private String from,successMsg,failMsg,round;
    private ExerciseRecord er;


    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (fragmentMode.equals("edit")) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().remove(ExerciseCompleteShare.this).commit();
                    fragmentManager.popBackStack();
                } else if (fragmentMode.equals("share")) {
                    //메인으로 이동하시겠습니까 다이어로그 띄우기
                    AlertDialog.Builder goTomMainDialog = new AlertDialog.Builder(getActivity());
                    goTomMainDialog.setMessage("홈으로 이동합니다.").setTitle("운동 완료")
                            .setPositiveButton("이동", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //nothing
                                }
                            });
                    AlertDialog alertDialog_goToMain = goTomMainDialog.create();
                    alertDialog_goToMain.show();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        onBackPressedCallback.remove();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getArguments();
        exerciseCompleteBundle = (ExerciseCompleteBundle) savedInstanceState.getSerializable("exerciseCompleteBundle");
        exerciseCompleteBundle2 = (HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>>) savedInstanceState.getSerializable("exerciseCompleteBundle2");
        exerciseCompleteBundle3 = (ArrayList<Exercise>) savedInstanceState.getSerializable("exerciseCompleteBundle3");
        from = savedInstanceState.getString("from");
        er = (ExerciseRecord)savedInstanceState.getSerializable("er");
        erStartDate = savedInstanceState.getString("startExerciseTime");
        round = savedInstanceState.getString("round");

        appDatabase = AppDatabase.getDBInstance(getActivity().getApplicationContext());
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_exercise_complete_share, container, false);
        exerciseTimeTv = (TextView) rootView.findViewById(R.id.complete_exercise_time_tv);
        exerciseCountTv = (TextView) rootView.findViewById(R.id.complete_exercise_count);
        exerciseRoundTv = (TextView) rootView.findViewById(R.id.complete_exercise_round_tv);
        completeBt = (Button) rootView.findViewById(R.id.ok_or_share_feed_bt);
        completeRv = (RecyclerView) rootView.findViewById(R.id.complete_exercise_rv);
        editBt = (Button) rootView.findViewById(R.id.exercise_share_photo_bt);
        shareBt = (Button) rootView.findViewById(R.id.exercise_share_save_text_bt);
        completeMemoEt = (EditText) rootView.findViewById(R.id.complete_exercise_memo);
        summarySet = (CheckBox) rootView.findViewById(R.id.summary_exercise_set_cb);
        completeRoundSpinner = (Spinner) rootView.findViewById(R.id.complete_exercise_spinner);
        erExerciseSmile = (ImageView) rootView.findViewById(R.id.complete_exercise_smile);
        completeSmileTable = (TableLayout) rootView.findViewById(R.id.complete_smile_table);
        saveRoutineBt = (Button) rootView.findViewById(R.id.complete_sava_routine_bt);
        kgtv =  rootView.findViewById(R.id.kgtv);
        vibrateAnimationSet = (AnimationSet) vibrateAnimationSet(requireActivity());
        savePhotoFragment = new SavePhoto();
        handler = new Handler();
        routineNameDialog = (LinearLayout) View.inflate(requireActivity(), R.layout.routine_name_dialog, null);
        summaryMode = "normal";
        exerciseTimeTv.setText(exerciseCompleteBundle.getExerciseTime());
        ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();

        exerciseSharedPreferences = new ExerciseSharedPreferences(requireActivity().getApplicationContext());

        // - 1 ROUND -
        if(round!=null)  exerciseRoundTv.setText("- " + round + " ROUND -");
        else{
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int round = appDatabase.exerciseRecordDao().getExerciseRound(new GetToday().getToday()) + 1;
                    System.out.println("round++" + round);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            exerciseRoundTv.setText("- " + round + " ROUND -");
                        }
                    });
                    SystemClock.sleep(100);
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }




        HashMap<Integer, ExerciseTableInfo> hashMap = exerciseCompleteBundle.getHashMap();
        list = new ArrayList<>();
        hashMap.forEach((key, value) -> {
            list.add(value);
        });

        double totalMyWeight = 0.0;
        exerciseCountTv.setText(exerciseCompleteBundle.getTotalCount() + " 0.0 kg/0.0 kg");
        for (ExerciseTableInfo exerciseTableInfo : list) {
            String exerciseName = exerciseTableInfo.getExerciseName();
            ArrayList<String> header = exerciseTableInfo.getExerciseTableHeader();
            ArrayList<String> body = exerciseTableInfo.getExerciseTableBody();
            String exType = header.get(1) + "/" + header.get(2);
            // java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
            if (exType.contains("무게(kg)/횟수") || exType.contains("무게(lbs)/횟수")) {
                String weightUnit = header.get(1).substring(3, 5);
                for (int i = 0; i < body.size(); i++) {
                    String str = body.get(i);
                    int strlen = str.split("/").length;
                    if (str.split("/")[strlen - 1].equals("true")) {
                        double weight = Double.parseDouble(str.split("/")[1]);
                        if (weightUnit.equals("lb")) weight *= 0.45;
                        totalMyWeight +=
                                weight * Integer.parseInt(str.split("/")[2]);
                    }
                }
            }
            String getTotalWeight = exerciseCompleteBundle.getTotalWeight().trim();
            exerciseCountTv.setText(exerciseCompleteBundle.getTotalCount() + " " + totalMyWeight + " kg/" +
                    getTotalWeight+"");
            if(round!=null) kgtv.setVisibility(View.VISIBLE);
        }

        completeExerciseListAdapter = new CompleteExerciseListAdapter();
        completeExerciseListAdapter.setLifecycleOwner(this);
        completeRv.setAdapter(completeExerciseListAdapter);
        completeExerciseListAdapter.setItem(list);
        completeRv.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        completeRv.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        completeRv.setLayoutManager(layoutManager);


        //세트요약 체크박스
        summarySet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (summarySet.isChecked()) {
                    completeExerciseListAdapter.changeSummaryMode("summary");
                    summaryMode = "summary";
                } else {
                    completeExerciseListAdapter.changeSummaryMode("normal");
                    summaryMode = "normal";
                }
            }
        });
        //수정하기 버튼
        if(from!=null && from.equals("home")){
            editBt.setVisibility(View.GONE);
        }
        editBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(ExerciseCompleteShare.this).commit();
                fragmentManager.popBackStack();
            }
        });
        erExerciseSmile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeSmileTable.setVisibility(View.VISIBLE);

            }
        });
        //확인버튼
        completeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //운동을 완료했다는 메시지 띄우기
                //운동 기록을 데이터베이스에 인서트
                int index = 2;
                String erDate = new GetToday().getTodayTime();
                //if(from!=null && from.equals("from")) erDate = er.getErDate();
                String erRound = exerciseRoundTv.getText().toString().split(" ")[1];
                if(round != null) index =1;
                String erTotalWeight = exerciseCountTv.getText().toString().split(" ")[index]
                        .split("kg")[0];
                String ertTotalTime = exerciseTimeTv.getText().toString();
                int totalSet = 0;
                ArrayList<RelativeLayout> arrayList = completeExerciseListAdapter.getRelativeLayoutArrayList();
                for (RelativeLayout relativeLayout : arrayList) {
                    RelativeLayout header = (RelativeLayout) relativeLayout.getChildAt(0);
                    TextView setTextView = (TextView) header.getChildAt(1);
                    totalSet += Integer.parseInt(setTextView.getText().toString().split(" ")[0]);
                }
                String erTotalSet = String.valueOf(totalSet);
                String erTotalMemo = completeMemoEt.getText().toString();
                erExerciseSmileTag =
                        erExerciseSmile.getTag() == null ? "smile1" : erExerciseSmile.getTag().toString();

                Gson gson = new Gson();
                String erExerciseList = gson.toJson(hashMap);
                if (erStartDate == null) {
                    erStartDate = getToday();
                }
                //AsyncTask.execute(() ->  );
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(from!=null && from.equals("home")){
                            insertRes = appDatabase.exerciseRecordDao().updateExerciseRecord(
                                    er.getErDate(), er.getErStartDate(), er.getErRound(),
                                    Integer.parseInt(erTotalSet), Double.parseDouble(erTotalWeight),
                                    ertTotalTime, erExerciseSmileTag, erTotalMemo, erExerciseList,er.getErId());
                            uploadDB();
                        }else{
                            //yyyy-MM-dd HH:mm:ss
                            //erdate= "2023-05-15 09:55:44";
                            //erStartDate = "2023-05-15 08:55:44";
                            insertRes = appDatabase.exerciseRecordDao().insertExerciseRecord(
                                    erDate, erStartDate, Integer.parseInt(erRound),
                                    Integer.parseInt(erTotalSet), Double.parseDouble(erTotalWeight),
                                    ertTotalTime, erExerciseSmileTag, erTotalMemo, erExerciseList);
                           // uploadDB();
                        }


                         Double weight1Rm,weightMax,volMax;  weight1Rm = 0.0; weightMax =0.0; volMax=0.0;
                         int weightMaxReps = 0;
                        for(Integer key : hashMap.keySet()){
                            int tableTotalSet = 0;
                            double tableTotalWeight = 0.0;
                            ExerciseTableInfo exerciseTableInfo = hashMap.get(key);
                            assert exerciseTableInfo != null;
                            ArrayList<String> header = exerciseTableInfo.getExerciseTableHeader();
                            String weightUnit = header.get(1).contains("kg") ? "kg" : "lbs";
                            ArrayList<String> body = exerciseTableInfo.getExerciseTableBody();
                            Double tempVol = 0.0;
                            for(String row : body) {
                                if(row.contains("true")){
                                    tableTotalSet +=1;
                                    if(header.get(1).contains("무게") && header.get(2).contains("횟수")){
                                        Double weight = Double.valueOf(row.split("/")[1]);
                                        if(weightUnit.equals("lbs")) weight = weight * 0.45;
                                        int reps = Integer.parseInt(row.split("/")[2]);
                                        tableTotalWeight += weight * reps;
                                        System.out.println("weight"+ weight);
                                        if(weightMax < weight) {
                                            weightMax = weight;
                                            weightMaxReps = reps;
                                            weight1Rm = weightMax / (1.0278 - (0.0278 * weightMaxReps));
                                            System.out.println("weight1Rm"+ weight1Rm);
                                        }
                                        tempVol += weight * reps;
                                    }
                                }
                            }
                            if(volMax <= tempVol) volMax = tempVol;
                            System.out.println("weight1Rm"+ weight1Rm);
                            if(header.get(1).contains("무게") && header.get(2).contains("횟수")&&weight1Rm>0){
                                exercise1rmMap.put(key,weight1Rm);
                            }
                            appDatabase.exerciseDao().updateCompleteExercise(key,tableTotalSet,tableTotalWeight);
                        }
                         update1Rm(FirebaseAuth.getInstance().getCurrentUser().getUid(),exercise1rmMap);
                         exerciseSharedPreferences.clearPref();

                    }
                });
                if (insertRes == 0) {
                    fragmentMode = "share";
                    if(from!=null && from.equals("home")){
                        showAlertSimpleMessage(getActivity(), "성공!",  "운동 기록 수정을 완료했습니다 !");
                    }else{
                        showAlertSimpleMessage(getActivity(), "성공!",  "운동 기록을 완료했습니다 !");

                    }
                    uploadDB();
                } else if (insertRes == -1) {
                    fragmentMode = "edit";
                    if(from!=null && from.equals("home")){
                        showAlertSimpleMessage(getActivity(), "실패!",  "운동 기록 수정을 실패했습니다 !");
                    }else{
                        showAlertSimpleMessage(getActivity(), "실패", "운동 기록을 실패했습니다 !");
                    }

                }
                if (fragmentMode.equals("share") ) settingShareMode(fragmentMode);
            }

        });
        //루틴 저장 버튼
        saveRoutineBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
                HashMap<Integer, RoutineInfoListItem> riHashMap = new LinkedHashMap<>();
                ArrayList<Exercise> arr = exerciseCompleteBundle3;
                HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> exerciseList = exerciseCompleteBundle2;
                for (Exercise exercise : arr) {
                    RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem();
                    routineInfoListItem.setExercise(exercise);
                    routineInfoListItem.setRoutineInfoListItemDetailedArrayList(exerciseList.get(exercise.getExId()));
                    riHashMap.put(exercise.getExId(), routineInfoListItem);
                    hashMap.put(new Gson().toJson(exercise, Exercise.class),
                            routineInfoListItem.getRoutineInfoListItemDetailedArrayList());
                }

                routineDialogName = "";

                EditText routineNameEt = (EditText) routineNameDialog.findViewById(R.id.routine_name_dg_name_et);
                routineNameEt.setText(new GetToday().getTodayTime() + " 운동루틴");
                new AlertDialog.Builder(requireContext())
                        .setView(routineNameDialog)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                routineDialogName = routineNameEt.getText().toString();
                                if (routineDialogName.equals("")) {
                                    showAlertSimpleMessage.show(getContext(), "알림", "루틴에 이름이 없습니다 !");
                                } else if (exerciseList.size() == 0) {
                                    showAlertSimpleMessage.show(getContext(), "알림", "루틴에 운동을 넣어주세요 !");
                                } else {
                                    exerciseListString = new Gson().toJson(hashMap);
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            insertResult = appDatabase.routineDao().insertRoutine(
                                                    routineDialogName, "", new GetToday().getTodayTime(), exerciseListString);
                                        }
                                    });
                                    if (insertResult == 0) {
                                        showAlertSimpleMessage.show(getActivity(), "성공", "운동 등록에 성공했습니다.");

                                    } else if (insertResult == -1) {
                                        showAlertSimpleMessage.show(getActivity(), "실패", " 등록에 실패했습니다.");
                                    }
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        //운동 메모 스마일 아이콘 테이블
        for (int i = 0; i < completeSmileTable.getChildCount(); i++) {
            TableRow tableRow = (TableRow) completeSmileTable.getChildAt(i);
            for (int j = 0; j < tableRow.getChildCount(); j++) {
                ImageView imageView = (ImageView) tableRow.getChildAt(j);
                imageView.setTag("smile" + (i * 3 + j + 1));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView.startAnimation(vibrateAnimationSet);

                        erExerciseSmile.setTag(imageView.getTag().toString());
                        handler.postDelayed(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                erExerciseSmile.setImageDrawable(imageView.getDrawable());
                                completeSmileTable.setVisibility(View.GONE);
                            }
                        }, 500);
                    }
                });
            }
        }
        settingShareMode(fragmentMode);

        if(from!=null && from.equals("home")){
            completeMemoEt.setText(er.getErTotalMemo());
            Glide.with(this).
                    load(getResources().getIdentifier(er.getErSmileTag(), "drawable", getActivity().getPackageName()))
                    .into(erExerciseSmile);
        }

        return rootView;
    }

    private void update1Rm(String uid, HashMap<Integer, Double> exercise1rmMap) {
        if(exercise1rmMap!=null && !exercise1rmMap.isEmpty()){
            for(int key : exercise1rmMap.keySet()) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ONERM").child(String.valueOf(key));
                Double _1rm = exercise1rmMap.get(key);
                databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double db1rm = snapshot.getValue(Double.class);
                        if (db1rm !=null) {
                            if(db1rm < _1rm) databaseReference.child(uid).setValue(_1rm);
                        }else databaseReference.child(uid).setValue(_1rm);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        }
    }

    private String getToday() {
        long now;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        now = System.currentTimeMillis();
        date = new Date(now);
        return simpleDateFormat.format(date);
    }

    //간단한 메시지를 보여주는 다이얼로그
    private void showAlertSimpleMessage(Context context, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title).setMessage(content);

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    private String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + resourceId).toString();
    }

    private AnimationSet vibrateAnimationSet(Context context) {
        AnimationSet animationSet = new AnimationSet(true);
        Animation left1 = AnimationUtils.loadAnimation(context, R.anim.vibrate_left);
        Animation right1 = AnimationUtils.loadAnimation(context, R.anim.vibrate_right);
        left1.setDuration(100);
        right1.setDuration(100);
        right1.setStartOffset(100);
        animationSet.addAnimation(left1);
        animationSet.addAnimation(right1);

        Animation left2 = AnimationUtils.loadAnimation(context, R.anim.vibrate_left);
        Animation right2 = AnimationUtils.loadAnimation(context, R.anim.vibrate_right);
        left2.setDuration(100);
        left2.setStartOffset(200);
        right2.setDuration(100);
        right2.setStartOffset(300);
        animationSet.addAnimation(left2);
        animationSet.addAnimation(right2);
        return animationSet;
    }

    public boolean replaceFragment(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commit();
        return true;
    }

    public boolean replaceFragment_addToBack(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).addToBackStack(null).commit();
        return true;
    }

    private void settingShareMode(String mode) {
        if (mode.equals("share")) {
            if(round!=null) kgtv.setVisibility(View.VISIBLE);
            shareBt.setVisibility(View.VISIBLE); completeMemoEt.setEnabled(false);
            editBt.setVisibility(View.VISIBLE);
            editBt.setText("사진공유");shareBt.setText("텍스트공유");completeBt.setText("피드공유");
            erExerciseSmile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            editBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<RelativeLayout> arrayList = completeExerciseListAdapter.getRelativeLayoutArrayList();
                    ArrayList<CompleteExerciseItem> completeExerciseItemArrayList = new ArrayList<CompleteExerciseItem>();
                    String[] totalInfo = new String[]{
                            exerciseRoundTv.getText().toString(),
                            exerciseTimeTv.getText().toString(),
                            exerciseCountTv.getText().toString(),
                            getURLForResource(getResources().getIdentifier(erExerciseSmileTag, "drawable", getActivity().getPackageName())),
                    };
                    for (RelativeLayout rLayout : arrayList) {
                        CompleteExerciseItem completeExerciseItem = new CompleteExerciseItem();
                        RelativeLayout exerciseNameRL = (RelativeLayout) rLayout.getChildAt(0);
                        TableLayout exerciseTable = (TableLayout) rLayout.getChildAt(2);

                        completeExerciseItem.setCompExerciseName(
                                ((TextView) exerciseNameRL.getChildAt(0)).getText().toString()
                        );
                        completeExerciseItem.setCompExerciseTotalSet(
                                ((TextView) exerciseNameRL.getChildAt(1)).getText().toString()
                        );

                        completeExerciseItem.setCompExerciseWeight(
                                ((TextView) exerciseNameRL.getChildAt(2)).getText().toString()
                        );

                        ArrayList<String> header = new ArrayList<>();
                        TableRow headerRow = (TableRow) exerciseTable.getChildAt(0);
                        for (int i = 0; i < headerRow.getChildCount(); i++) {
                            header.add(((TextView) headerRow.getChildAt(i)).getText().toString());
                        }
                        completeExerciseItem.setHeader(header);

                        ArrayList<String> body = new ArrayList<>();
                        for (int i = 1; i < exerciseTable.getChildCount(); i++) {
                            TableRow exerciseRow = (TableRow) exerciseTable.getChildAt(i);
                            String exerciseRowString = "";
                            for (int j = 0; j < exerciseRow.getChildCount(); j++) {
                                exerciseRowString += ((TextView) exerciseRow.getChildAt(j)).getText().toString() + "/";
                            }
                            body.add(exerciseRowString);
                        }
                        completeExerciseItem.setBody(body);

                        completeExerciseItemArrayList.add(completeExerciseItem);
                    }
                    Bundle savePhotoBundle = new Bundle();
                    savePhotoBundle.putSerializable("completeExerciseItemArrayListBundle", completeExerciseItemArrayList);
                    savePhotoBundle.putSerializable("totalInfoBundle", totalInfo);
                    savePhotoFragment.setArguments(savePhotoBundle);
                    replaceFragment_addToBack(R.id.exercie_main_fr_layout, savePhotoFragment);

                }
            });
            shareBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String clipString = getToday().split(" ")[0] + " 오늘의 " +
                            exerciseRoundTv.getText().toString().split("-")[1].trim() + " 운동 기록\n" +
                            "총 운동 시간 : " + exerciseTimeTv.getText().toString() + "\n" +
                            "운동 종목 수 : " + exerciseCountTv.getText().toString().split(" ")[0] + " 종목\n" +
                            "운동량 : " + exerciseCountTv.getText().toString().split(" ")[2] + "\n\n";

                    for (int i = 0; i < completeRv.getChildCount(); i++) {
                        int set = 1;
                        RecyclerView.ViewHolder viewHolder = completeRv.findViewHolderForAdapterPosition(i);
                        String exerciseName = ((TextView) viewHolder.itemView.findViewById(R.id.comp_exercise_name)).getText().toString();
                        String exerciseWeight = ((TextView) viewHolder.itemView.findViewById(R.id.comp_exercise_weight)).getText().toString();
                        String exerciseTotalSet = ((TextView) viewHolder.itemView.findViewById(R.id.comp_exercise_total_set)).getText().toString();
                        exerciseWeight = "TOTAL : " + exerciseWeight.split(" ")[2];
                        String row = exerciseName + "   " + exerciseWeight + "\n";
                        String summaryRow = exerciseName + "  " + exerciseTotalSet + "   " + exerciseWeight + "\n";
                        ExerciseTableInfo exerciseTableInfo = list.get(i);
                        ArrayList<String> bodyInfo = exerciseTableInfo.getExerciseTableBody();
                        TableLayout tableLayout = (TableLayout) viewHolder.itemView.findViewById(R.id.comp_table_layout);
                        TableRow header = (TableRow) tableLayout.getChildAt(0);
                        int headerChild = header.getChildCount();
                        String type1 = ((TextView) header.getChildAt(1)).getText().toString();
                        String type2 = "";
                        if (headerChild == 4)
                            type2 = ((TextView) header.getChildAt(2)).getText().toString();
                        String type1Unit = "";
                        String type2Unit = "";
                        if (type1.contains("kg")) type1Unit = "kg";
                        else if (type1.contains("lbs")) type1Unit = "lbs";
                        else if (type1.contains("횟수")) type1Unit = "회";

                        if (type2.contains("횟수")) type2Unit = "회";
                        System.out.println(type1 + type2 + type1Unit + type2Unit + headerChild);
                        for (int j = 0; j < bodyInfo.size(); j++) {
                            String record = bodyInfo.get(j);
                            System.out.println("reord++"+record);
                            if (record.split("/")[record.split("/").length - 1].equals("true")) {
                                String tmp = "";
                                if (headerChild == 3) {
                                    tmp = set + "세트 : " + record.split("/")[1] + type1Unit;
                                } else if (headerChild == 4) {
                                    tmp = set + "세트 : " + record.split("/")[1] + type1Unit +
                                            " X " + record.split("/")[2] + type2Unit;
                                }
                                row += tmp + "\n";
                                set += 1;
                            }
                        }
                        if (summaryMode.equals("summary")) {
                            clipString += summaryRow + "\n";
                        } else if (summaryMode.equals("normal")) {
                            clipString += row + "\n";
                        }
                    }
                    ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("textSave", clipString);
                    clipboardManager.setPrimaryClip(clipData);
                    System.out.println(clipString);
                    Toast.makeText(getActivity(), "운동 기록을 클립보드에 복사했습니다.", Toast.LENGTH_SHORT).show();
                }

            });
            completeBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(requireActivity(), PostActivity.class);
                    String routineString;
                    HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
                    HashMap<Integer, RoutineInfoListItem> riHashMap = new LinkedHashMap<>();
                    ArrayList<Exercise> arr = exerciseCompleteBundle3;
                    HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> exerciseList = exerciseCompleteBundle2;
                    for (Exercise exercise : arr) {
                        RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem();
                        routineInfoListItem.setExercise(exercise);
                        routineInfoListItem.setRoutineInfoListItemDetailedArrayList(exerciseList.get(exercise.getExId()));
                        riHashMap.put(exercise.getExId(), routineInfoListItem);
                        hashMap.put(new Gson().toJson(exercise, Exercise.class),
                                routineInfoListItem.getRoutineInfoListItemDetailedArrayList());
                    }
                    routineString = new Gson().toJson(hashMap);
                    Bundle bundle = new Bundle();
                    bundle.putString("routine",routineString);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    private void uploadDB() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String DB_NAME = "EXERCISE_" + user.getUid() + ".db";
        String DB_PATH = "/data/data/org.techtown.MyExerciseApp/database/";
        String TAG = "DataBaseHelper";
        File dbFile = new File(DB_PATH + DB_NAME);
        if (dbFile.exists()) {
            try {
                File folder = new File(DB_PATH);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                InputStream inputStream = dbFile.toURL().openStream();
                StorageReference dbRef = FirebaseStorage.getInstance().getReference().child("databases/EXERCISE_" + user.getUid() + ".db");
                UploadTask uploadTask = dbRef.putStream(inputStream);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                          //  Toast.makeText(getContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                            Log.d("uploadSuccess", "Successfully uploaded");
                        }else{
                            Log.d("uploadFail", "Fail uploaded");
                        }
                    }
                });


                inputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            }
            Log.d(TAG, "Database is copied.");
        }
        AppDatabase.renewInstance(requireActivity().getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tab_content_view,new HomeFragment()).commit();
    }
}


