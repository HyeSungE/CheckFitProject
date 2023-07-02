package org.techtown.MyExerciseApp.Exercise;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.techtown.MyExerciseApp.Adapter.Exercise.RoutineInfoListAdapter;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineItem;
import org.techtown.MyExerciseApp.Data.FcmPush;
import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.Data.Group.GroupRoutine;
import org.techtown.MyExerciseApp.Interface.SendEventListener;
import org.techtown.MyExerciseApp.MyClass.GetDp;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


public class RoutineViewFragment extends Fragment implements SendEventListener, ExerciseMainActivity.onBackPressedListener{
    private TextView routineViewNameTv;
    private EditText routineViewNameEt;
    private ImageButton backBt, saveBt;
    private Button selectRoutineBt, addExerciseRoutineBt;
    private AppDatabase appDatabase;
    private RecyclerView routineViewInfoRecyclerView;
    private RoutineInfoListAdapter routineInfoListAdapter;
    private ArrayList<RoutineInfoListItem> routineInfoListItemArrayList;
    private ExerciseListFragment exerciseListFragment;
    private String mode, exerciseListString,from,from_2,groupView;
    private final ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();
    private FragmentManager fragmentManager;
    private final Gson gson = new Gson();
    private long insertResult;
    private RoutineItem routineItem;
    private SendEventListener sendEventListener;
    private String routineDialogName,groupRoutineName;
    private Activity mActivity;
    private LinearLayout routineViewButtons;
    private Group selectedGroup;
    private boolean isGroup;
    //클릭한 루틴의 운동들을 보여주는 프래그먼트이다. 해당 루틴의 정보를 받고 그에 맞는 운동리스트를 보여준다

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
        try {
            sendEventListener = (SendEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException((context + " must implements SendEvent"));
        }
        fragmentManager = requireActivity().getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_routine_view, container, false);
        routineItem = new RoutineItem();
        routineInfoListItemArrayList = new ArrayList<>();

            savedInstanceState = getArguments();
            if(savedInstanceState != null){
                from = savedInstanceState.getString("from");
                mode = savedInstanceState.getString("mode");
                from_2 = savedInstanceState.getString("from_2");
                groupView = savedInstanceState.getString("groupView");
                isGroup = savedInstanceState.getBoolean("isGroup");
                selectedGroup = (Group) savedInstanceState.getSerializable("group");
                groupRoutineName = savedInstanceState.getString("groupRoutineName");
                if (mode.equals("select")) {
                    routineItem = (RoutineItem) savedInstanceState.getSerializable("selectRoutine");
                    routineInfoListItemArrayList = routineItem.getRoutineInfoList();
                }
                else if(mode.equals("custom")) routineItem.setRoutineInfoList(routineInfoListItemArrayList);
            }



        LinearLayout routineNameDialog = (LinearLayout) View.inflate(requireActivity(), R.layout.routine_name_dialog, null);

        appDatabase = AppDatabase.getDBInstance(requireActivity().getApplicationContext());

        routineViewNameTv = rootView.findViewById(R.id.routine_view_name_tv);
        String str ="";
        if(routineItem.getRoutine()!=null) str = routineItem.getRoutine().getRoName();
        routineViewNameTv.setText(str);
        if(groupRoutineName!=null && !groupRoutineName.isEmpty())  routineViewNameTv.setText(groupRoutineName);
        routineViewNameEt = rootView.findViewById(R.id.routine_view_name_et);

        backBt = rootView.findViewById(R.id.routine_view_back);
        addExerciseRoutineBt = rootView.findViewById(R.id.routine_view_add_exercise);
        selectRoutineBt = rootView.findViewById(R.id.routine_view_select_routine);
        if(selectedGroup!=null && isGroup) selectRoutineBt.setEnabled(isGroup);
        saveBt = rootView.findViewById(R.id.routine_view_save_routine);

        routineViewButtons = (LinearLayout) rootView.findViewById(R.id.routine_view_buttons);
        if (mode.equals("custom")) {
            routineViewNameTv.setVisibility(View.INVISIBLE);
            routineViewNameEt.setVisibility(View.VISIBLE);
            selectRoutineBt.setText("루틴 저장");
            if(from.equals("home")) {
                settingBottomOfRoutineViewFragment(requireActivity().getApplicationContext(),60);}
            else if(from.equals("group")){
                settingBottomOfRoutineViewFragment(requireActivity().getApplicationContext(),60);
                selectRoutineBt.setText("그룹 운동 작성");
            }
        } else {
            routineViewNameTv.setVisibility(View.VISIBLE);
            routineViewNameEt.setVisibility(View.INVISIBLE);
            if(from.equals("home")) {
                settingBottomOfRoutineViewFragment(requireActivity().getApplicationContext(),60);
                selectRoutineBt.setVisibility(View.GONE);
            } else if(from.equals("group")){
                settingBottomOfRoutineViewFragment(requireActivity().getApplicationContext(),60);
                addExerciseRoutineBt.setVisibility(View.GONE);
                selectRoutineBt.setText("그룹 운동 하기");
            }
        }
        routineViewNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(from.equals("home")) {
                    saveBt.setVisibility(View.VISIBLE);
                    routineViewNameTv.setVisibility(View.INVISIBLE);
                    routineViewNameEt.setVisibility(View.VISIBLE);
                    routineViewNameEt.setText(routineViewNameTv.getText().toString());
                }
            }
        });

        routineViewInfoRecyclerView = rootView.findViewById(R.id.routine_view_info_recycler_view);
        routineViewInfoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        routineViewInfoRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        routineViewInfoRecyclerView.setLayoutManager(layoutManager);
        routineInfoListAdapter = new RoutineInfoListAdapter();
        if(groupView!=null) routineInfoListAdapter.setMode("GroupRoutineView");
        routineViewInfoRecyclerView.setAdapter(routineInfoListAdapter);
        routineInfoListAdapter.setItem(routineInfoListItemArrayList);
        routineInfoListAdapter.setRoutineInfoListClickListener(routineInfoListClickListener);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.routine_view_back: { //이전
                        requireActivity().getSupportFragmentManager().beginTransaction().remove(RoutineViewFragment.this).commit();
                        requireActivity().getSupportFragmentManager().popBackStack();
                        break;
                    }
                    case R.id.routine_view_add_exercise: { //운동추가
                        exerciseListFragment = new ExerciseListFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("from", "routine");
                        if(from_2!=null) bundle.putString("from_2", from_2);
                        System.out.println(from_2);
                        exerciseListFragment.setArguments(bundle);
                        if(from.equals("group")){
                            fragmentManager.beginTransaction().
                                    add(R.id.tab_content_view, exerciseListFragment).
                                    addToBackStack(null).commit();
                        }else{
                            fragmentManager.beginTransaction().
                                    add(R.id.fragment_routine_list_frameLayout, exerciseListFragment).
                                    addToBackStack(null).commit();
                        }
                        break;
                    }
                    case R.id.routine_view_select_routine: {
                        //선택한 루틴을 운동 목록에 추가 or 커스텀모드이면 루틴 저장
                        switch (mode) {
                            case "select": {
                                if(from.equals("group")){
                                    System.out.println("789"+routineItem.getRoutineInfoList().get(0).getExercise().getExName());
                                    ArrayList<RoutineInfoListItem> exerciseList = routineItem.getRoutineInfoList();
                                    if (exerciseList.size() == 0) {
                                        showAlertSimpleMessage.show(getContext(), "알림", "루틴에 운동을 넣어주세요 !");
                                    }else{
                                        //sendEventListener.sendRoutine(exerciseList);  //ExerciseMainActivity로 이동
                                        System.out.println("exerc" + exerciseList.get(0).getExercise().getExName());
                                        Intent intent = new Intent(requireActivity(), ExerciseMainActivity.class);
                                        intent.putExtra("groupRoutine",exerciseList);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                }else{
                                    List<RoutineInfoListItem> exerciseList = routineInfoListAdapter.getItem();
                                    HashMap<Integer, RoutineInfoListItem> riHashMap = routineInfoListAdapter.getRiHashMap();
                                    for (int exId : riHashMap.keySet()) {
                                        RoutineInfoListItem r = riHashMap.get(exId);
                                        System.out.println("select routine exercise name++" + r.getExercise().getExName());
                                        ArrayList<RoutineInfoListItemDetailed> arr = r.getRoutineInfoListItemDetailedArrayList();
                                    }
                                    if (exerciseList.size() == 0) {
                                        showAlertSimpleMessage.show(getContext(), "알림", "루틴에 운동을 넣어주세요 !");
                                    } else {
                                        /*exerciseList 업데이트는 RoutineListFragment참조*/
                                        int roId = routineItem.getRoutine().getRoId(); //Bundle로 넘어온 Routine을 이용
                                        AsyncTask.execute(new Runnable() {  //루틴의 최근 이용 날짜와 이용 횟수 업데이트
                                            @Override
                                            public void run() {
                                                appDatabase.routineDao().updateUsedRoutine(roId, new GetToday().getToday());
                                            }
                                        });
                                        //Bundle에 해당 운동리스트를 저장하고 ExerciseMain으로 이동
                                        sendEventListener.sendRoutine(exerciseList);  //ExerciseMainActivity로 이동
                                        for (int i = 0; i < fragmentManager.getFragments().size() - 1; i++) {
                                            fragmentManager.beginTransaction().remove(fragmentManager.getFragments().get(i)).commit();
                                        }
                                        exitRoutineViewFrag();
                                    }
                                }

                                break;
                            }
                            case "custom": {
                                HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
                                String routineName = routineViewNameEt.getText().toString();
                                HashMap<Integer, RoutineInfoListItem> exerciseList = routineInfoListAdapter.getRiHashMap();
                                for (int exId : exerciseList.keySet()) {
                                    RoutineInfoListItem routineInfoListItem = exerciseList.get(exId);
                                    Exercise exercise = routineInfoListItem.getExercise();
                                    hashMap.put(gson.toJson(exercise, Exercise.class),
                                            routineInfoListItem.getRoutineInfoListItemDetailedArrayList());
                                }
                                if (routineName.isEmpty()) {
                                    showAlertSimpleMessage.show(getContext(), "알림", "루틴에 이름이 없습니다 !");
                                    routineViewNameEt.requestFocus();
                                } else if (exerciseList.size() == 0) {
                                    showAlertSimpleMessage.show(getContext(), "알림", "루틴에 운동을 넣어주세요 !");
                                } else {
                                    exerciseListString = gson.toJson(hashMap);
                                    if(from.equals("group")){
                                        String today = new GetToday().getToday();
                                        String path = "GROUP/"+selectedGroup.getGroupUid()+"/groupRoutines/"+today;
                                        GroupRoutine groupRoutine = new GroupRoutine(routineName,today,exerciseListString);
                                        FirebaseDatabase.getInstance().getReference(path).setValue(groupRoutine).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                String fcmPath = "GROUP/"+selectedGroup.getGroupUid()+"/groupMembers";
                                                FirebaseDatabase.getInstance().getReference(fcmPath).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.hasChildren()){
                                                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                                String memberUid = dataSnapshot.getValue(String.class);
                                                                String fcmMessage = "[" + selectedGroup.getGroupName() + "] 에서 운동이 도착했습니다 ! :)";
                                                                FcmPush.getFcmInstance().sendMessage(memberUid, "알림", fcmMessage);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                            }
                                        });
                                    }else{
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                insertResult = appDatabase.routineDao().insertRoutine(
                                                        routineName, "", new GetToday().getTodayTime(), exerciseListString);
                                            }
                                        });
                                    }
                                    if (insertResult == 0) {
                                        showAlertSimpleMessage.show(getActivity(), "성공", "루틴 등록에 성공했습니다.");
                                        routineViewNameEt.setText("");
                                        exerciseList.clear();
                                        exerciseListString = null;

                                        exitRoutineViewFrag();
                                    } else if (insertResult == -1) {
                                        showAlertSimpleMessage.show(getActivity(), "실패", " 등록에 실패했습니다.");
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case R.id.routine_view_save_routine: { // 운동 추가 갔다오면 루틴 저장버튼 나타남
                        HashMap<String, ArrayList<RoutineInfoListItemDetailed>> hashMap = new LinkedHashMap<>();
                        HashMap<Integer, RoutineInfoListItem> exerciseList = routineInfoListAdapter.getRiHashMap();
                        for (int exId : exerciseList.keySet()) {
                            RoutineInfoListItem routineInfoListItem = exerciseList.get(exId);
                            Exercise exercise = routineInfoListItem.getExercise();
                            hashMap.put(gson.toJson(exercise, Exercise.class),
                                    routineInfoListItem.getRoutineInfoListItemDetailedArrayList());
                        }


                        AlertDialog.Builder howSaveRoutine_builder = new AlertDialog.Builder(requireActivity());
                        howSaveRoutine_builder.setMessage("루틴을 저장하겠습니다.")
                                .setPositiveButton("덮어쓰기", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        routineDialogName = routineViewNameTv.getText().toString();
                                        if(!routineViewNameEt.getText().toString().isEmpty() &&
                                                !routineDialogName.equals(routineViewNameEt.getText().toString())) {
                                            routineDialogName = routineViewNameEt.getText().toString();
                                        }
                                        if (routineDialogName.equals("")) {
                                            showAlertSimpleMessage.show(getContext(), "알림", "루틴에 이름이 없습니다 !");
                                        } else if (exerciseList.size() == 0) {
                                            showAlertSimpleMessage.show(getContext(), "알림", "루틴에 운동을 넣어주세요 !");
                                        } else {
                                            exerciseListString = gson.toJson(hashMap);
                                            AsyncTask.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    insertResult = appDatabase.routineDao().updateSameRoutine(
                                                            routineItem.getRoutine().getRoId(), exerciseListString,routineDialogName);
                                                }
                                            });
                                            if (insertResult == 0) {
                                                showAlertSimpleMessage.show(getActivity(), "성공", "루틴 업데이트에 성공했습니다.");
                                                requireActivity().getSupportFragmentManager().beginTransaction().remove(RoutineViewFragment.this).commit();
                                                requireActivity().getSupportFragmentManager().popBackStack();
                                            } else if (insertResult == -1) {
                                                showAlertSimpleMessage.show(getActivity(), "실패", " 루틴 업데이트에 실패했습니다.");
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("새로저장", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        routineDialogName = "";
                                        EditText routineNameEt = (EditText) routineNameDialog.findViewById(R.id.routine_name_dg_name_et);
                                        routineNameEt.setText(new GetToday().getTodayTime() + " 운동루틴");
                                        new AlertDialog.Builder(requireContext())
                                                .setView(routineNameDialog)
                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        routineDialogName = routineNameEt.getText().toString();
                                                        routineViewNameEt.setText(routineDialogName);
                                                        if (routineDialogName.equals("")) {
                                                            showAlertSimpleMessage.show(getContext(), "알림", "루틴에 이름이 없습니다 !");
                                                        } else if (exerciseList.size() == 0) {
                                                            showAlertSimpleMessage.show(getContext(), "알림", "루틴에 운동을 넣어주세요 !");
                                                        } else {
                                                            exerciseListString = gson.toJson(hashMap);
                                                            AsyncTask.execute(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    insertResult = appDatabase.routineDao().insertRoutine(
                                                                            routineDialogName, "", new GetToday().getTodayTime(), exerciseListString);
                                                                }
                                                            });
                                                            if (insertResult == 0) {
                                                                showAlertSimpleMessage.show(getActivity(), "성공", "운동 등록에 성공했습니다.");
                                                                routineViewNameEt.setText("");
                                                                exerciseList.clear();
                                                                exerciseListString = null;
                                                                //routineInfoListAdapter.setItem(list);
                                                                exitRoutineViewFrag();

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
                                                })
                                                .show();

                                    }
                                });
                        AlertDialog alertDialog_howSaveRoutine = howSaveRoutine_builder.create();
                        alertDialog_howSaveRoutine.setOnShowListener(new DialogInterface.OnShowListener() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void onShow(DialogInterface arg0) {
                                alertDialog_howSaveRoutine.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                                alertDialog_howSaveRoutine.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            }
                        });
                        alertDialog_howSaveRoutine.show();


                        break;
                    }

                }
            }
        };
        backBt.setOnClickListener(onClickListener);
        selectRoutineBt.setOnClickListener(onClickListener);
        addExerciseRoutineBt.setOnClickListener(onClickListener);
        saveBt.setOnClickListener(onClickListener);
        return rootView;
    }

    public boolean replaceFragment_addToBack(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).addToBackStack(null).commit();
        return true;
    }

    private void exitRoutineViewFrag() {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(RoutineViewFragment.this).commit();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    @Override // nothing to do here
    public void sendExerciseList(ArrayList<Exercise> exerciseArrayList) {
    }

    @Override
    public void sendExerciseListToRoutine(ArrayList<Exercise> exerciseArrayList) {
        List<RoutineInfoListItem> adapterList = routineInfoListAdapter.getItem();
        Set<Integer> set = new HashSet<>();
        for (RoutineInfoListItem routineInfoListItem : adapterList) {
            Exercise exercise = routineInfoListItem.getExercise();
            set.add(exercise.getExId());
        }
        int adapterSize = adapterList.size();
        for (Exercise exercise : exerciseArrayList) {
            if (!set.contains(exercise.getExId())) {
                RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem();
                routineInfoListItem.setExercise(exercise);
                routineInfoListItem.setRoutineInfoListItemDetailedArrayList(new ArrayList<>());
                routineInfoListAdapter.addItem(routineInfoListItem, adapterSize++);
            }
        }

        if (!mode.equals("custom") || !from.equals("group")) {
            saveBt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void sendRoutine(List<RoutineInfoListItem> routineInfoListItemArrayList) {

    }

    private void setSaveBtVisibility(boolean b) {
        if (b) saveBt.setVisibility(View.VISIBLE);
        else saveBt.setVisibility(View.INVISIBLE);

    }

    private final RoutineInfoListAdapter.RoutineInfoListClickListener routineInfoListClickListener =
            new RoutineInfoListAdapter.RoutineInfoListClickListener() {
        @Override
        public void setSaveBtVisible(boolean b) {
            if (mode.equals("select")) {
                setSaveBtVisibility(b);
                if(from.equals("group")) setSaveBtVisibility(false);
            }

        }
    };

    //운동 라이브러리 프래그먼트에서 하단 운동 만들기 버튼에 margin값 설정
    public void settingBottomOfRoutineViewFragment(Context context,int margin) {

        GetDp getDp = new GetDp();
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        param.setMargins(0,0,0,getDp.getDp(context,margin));
        routineViewButtons.setLayoutParams(param);
    }

    @Override
    public void onBackPressed() {

    }
}