package org.techtown.MyExerciseApp.Exercise;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;

import org.techtown.MyExerciseApp.Adapter.Exercise.ExerciseListItemAdapter;
import org.techtown.MyExerciseApp.Interface.SendEventListener;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.MyClass.GetDp;
import org.techtown.MyExerciseApp.MyPage.MypageHomeFragment;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class ExerciseListFragment extends Fragment implements ExerciseMainActivity.onBackPressedListener{

    private Button exerciseListAddBt, customExerciseBt;
    private FrameLayout frameLayout;
    private RecyclerView recyclerView;
    private AppDatabase appDatabase;
    private ExerciseListItemAdapter exerciseListItemAdapter;
    private Context context;
    private Activity mActivity;
    private int count = 0, tabPosition;
    private TextView no_item_message;
    private final SparseBooleanArray[] selectedExerciseArr = new SparseBooleanArray[9];
    private ArrayList<Exercise> selectedExerciseList;
    private ArrayList<Integer> exerciseIdList;
    private TabLayout tabLayout;
    private List<Exercise> exerciseList = new ArrayList<>();
    private SearchView exerciseSearchView;
    private  HashMap<Integer, HashMap<Integer, Integer>> exerciseTempHashMap = new HashMap<>();
    private  HashMap<Integer, HashMap<Integer, Integer>> exerciseSerchHashMap = new HashMap<>();
    private SendEventListener sendEventListener;
    private String from,from_2;
    private BottomSheetDialog customExerciseBottomSheetDialog;
    private LinearLayout exerciseListFragmentBottomLayout;


    public ExerciseListFragment(Context context) {
        this.context = context;
    }

    public ExerciseListFragment() {

    }
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
    }

    @Override
    public void onDetach() {
        mActivity = null;
        context = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_exercise_list, container, false);
        savedInstanceState = getArguments();
        if(savedInstanceState!=null) {
            from = savedInstanceState.getString("from");
            from_2 = savedInstanceState.getString("from_2");
        }
        init(rootView);
        setOnClickListener();
        return rootView;
    }


    public void setExerciseList(String bodypart, int tabPosition) {

        //UI 갱신 (라이브데이터 Observer 이용, 해당 디비값이 변화가생기면 실행됨)
        if (bodypart == null) {
            appDatabase.exerciseDao().getExerciseFavorite().observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
                @Override
                public void onChanged(List<Exercise> data) {
                    exerciseList = data;
                    int count;
                    count = data.size();
                    int i = 0;
                    for (Exercise exercise : exerciseList) {
                        exerciseTempHashMap.get(tabPosition).put(exercise.getExId(), i++);
                    }
                    exerciseListItemAdapter.setTemp(exerciseTempHashMap.get(tabPosition));
                    exerciseListItemAdapter.setItem(exerciseList, tabPosition);

                    if (count == 0) no_item_message.setVisibility(View.VISIBLE);
                }
            });
        } else {
            appDatabase.exerciseDao().getExerciseByBodypart(bodypart).observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
                @Override
                public void onChanged(List<Exercise> data) {
                    exerciseList = data;
                    int i = 0;
                    for (Exercise exercise : exerciseList) {
                        Objects.requireNonNull(exerciseTempHashMap.get(tabPosition)).put(exercise.getExId(), i++);
                    }
                    exerciseListItemAdapter.setTemp(exerciseTempHashMap.get(tabPosition));
                    exerciseListItemAdapter.setItem(exerciseList, tabPosition);

                    no_item_message.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public void searchExercise(String searchString, String bodypart) {
        if (bodypart == null) {
            appDatabase.exerciseDao().searchExerciseFavorite(searchString).observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
                @Override
                public void onChanged(List<Exercise> data) {
                    exerciseList = data;
                    if (data.size() > 0)
                        no_item_message.setVisibility(View.INVISIBLE);
                    else  no_item_message.setVisibility(View.VISIBLE);
                    exerciseListItemAdapter.setItem(exerciseList, 8);

                }
            });
            if (count == 0) no_item_message.setVisibility(View.VISIBLE);
        } else {
            appDatabase.exerciseDao().searchExercise(searchString, bodypart).observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
                @Override
                public void onChanged(List<Exercise> data) {
                    exerciseList = data;
                    if (data.size() > 0)
                        no_item_message.setVisibility(View.INVISIBLE);
                    else  no_item_message.setVisibility(View.VISIBLE);
                    exerciseListItemAdapter.setItem(exerciseList, bodypartToTabPosition(bodypart));
                }
            });

        }

    }

    public void setSelectedExerciseArr() {
        for (int i = 0; i < 9; i++) {
            selectedExerciseArr[i] = new SparseBooleanArray(0);
            exerciseTempHashMap.put(i, new HashMap<>());
        }
    }

    private void init(ViewGroup rootView) {
        exerciseListFragmentBottomLayout = rootView.findViewById(R.id.exercise_list_fragment_bottom_lr);

        exerciseListAddBt = rootView.findViewById(R.id.exerciseList_addBt);

        customExerciseBt = rootView.findViewById(R.id.custom_exercise_bt);

        exerciseSearchView = rootView.findViewById(R.id.exercise_search_view);

        no_item_message = rootView.findViewById(R.id.no_item_message);

        frameLayout = rootView.findViewById(R.id.fragment_exercise_list_frameLayout);

        tabLayout = rootView.findViewById(R.id.main_exercise_list_tab_layout);

        customExerciseBottomSheetDialog = new BottomSheetDialog(rootView.getContext(), R.style.BottomSheetDialog);

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = rootView.findViewById(R.id.exercise_list_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        appDatabase = AppDatabase.getDBInstance(getActivity().getApplicationContext());

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(layoutManager);

        selectedExerciseList = new ArrayList<>();

        setSelectedExerciseArr();

        exerciseListItemAdapter = new ExerciseListItemAdapter(selectedExerciseArr);
        exerciseListItemAdapter.setMode(from);
        exerciseListItemAdapter.setAppdatabase(appDatabase);
        recyclerView.setAdapter(exerciseListItemAdapter);

        exerciseIdList = new ArrayList<>();

        setExerciseList(null, tabPosition);

        if(from.equals("home") || from.equals("routine") || from.equals("mypage")) settingBottomOfExerciseListFragment(60);
        if(from_2!=null && from_2.equals("exerciseMain")) settingBottomOfExerciseListFragment(0);

    }

    private void setOnClickListener() {
        exerciseListAddBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "운동을 추가합니다.\n 이미 추가한 운동은 추가되지 않습니다.", Toast.LENGTH_SHORT).show();
                if (from.equals("exercise")) {
                    sendEventListener.sendExerciseList(selectedExerciseList);

                } else if (from.equals("routine")) {
                    for (Fragment fragment : getParentFragmentManager().getFragments()) {
                        if (fragment instanceof RoutineViewFragment)
                            sendEventListener = (SendEventListener) fragment;
                    }
                    sendEventListener.sendExerciseListToRoutine(selectedExerciseList);
                }
           onBackPressed();
            }
        });
        customExerciseBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Exercise exercise = null;
                showCustomExerciseDialog(context,exercise,-1);
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                exerciseSearchView.clearFocus();
                exerciseSearchView.setQuery("", false);

                if (tabPosition == 0) setExerciseList(null, tabPosition);
                else if (tabPosition == 1) setExerciseList("가슴", tabPosition);
                else if (tabPosition == 2) setExerciseList("어깨", tabPosition);
                else if (tabPosition == 3) setExerciseList("등", tabPosition);
                else if (tabPosition == 4) setExerciseList("하체", tabPosition);
                else if (tabPosition == 5) setExerciseList("팔", tabPosition);
                else if (tabPosition == 6) setExerciseList("유산소", tabPosition);
                else if (tabPosition == 7) setExerciseList("복근", tabPosition);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        exerciseListItemAdapter.setOnItemClickListener(new ExerciseListItemAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(View v, int position) {
                Exercise selectedExercise = exerciseList.get(position);
                Integer exerciseId = selectedExercise.getExId();
                if(!from.equals("home") && !from.equals("mypage")){
                    if (exerciseIdList.contains(exerciseId)) {
                        List<Exercise> removed = new ArrayList<>();
                        for (Exercise exercise : selectedExerciseList) {
                            if (exercise.getExId() == exerciseId) {
                                removed.add(exercise);
                            }
                        }
                        selectedExerciseList.removeAll(removed);

                        exerciseIdList.remove(exerciseId);
                    } else if (!exerciseIdList.contains(exerciseId)) {
                        selectedExerciseList.add(selectedExercise);
                        exerciseIdList.add(exerciseId);
                    }
                }
                else if(from.equals("home")){
                    RelativeLayout r = (RelativeLayout) v.findViewById(R.id.exercise_list_item_rel_layout);
                    r.setBackgroundResource(R.drawable.exercise_list_non_select_border);
                    //v.setBackgroundResource(R.drawable.exercise_list_non_select_border);
                    showViewExerciseAlertDialog(getActivity(),selectedExercise,position);
                }else if(from.equals("mypage")){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selectedExercise",selectedExercise);
                    Fragment mypageHomeFragment = new MypageHomeFragment();
                    mypageHomeFragment.setArguments(bundle);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.tab_content_view,mypageHomeFragment).commit();

                }
            }

            @Override
            public void onInfoClick(Exercise exercise, int position) {
                showViewExerciseAlertDialog(getActivity(),exercise,position);
            }

        });
        exerciseSearchView.setSubmitButtonEnabled(true);
        exerciseSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                int tabPosition = tabLayout.getSelectedTabPosition();
                String bodypart = String.valueOf(tabLayout.getTabAt(tabPosition).getText());

                if (bodypart.equals("즐겨찾기"))
                    bodypart = null;
                if (s.equals("") || s == null || s.isEmpty())
                    setExerciseList(String.valueOf(tabLayout.getTabAt(tabPosition).getText()), tabPosition);
                else
                    searchExercise(s, bodypart);

                return false;
            }
        });
        exerciseSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    exerciseListItemAdapter.clearSearchExerciseList();
                }
            }
        });
    }

    //밑에서 올라오는 운동 커스텀 다이얼로그 bottomSheetDialog
    private void showCustomExerciseDialog(Context context,Exercise e,int position) {
        int bodyPartIndex = -1,equipementIndex=-1,weightUnitIndex=-1;
        int[] typeIndex = {-1,-1};
        View customExerciseView = getLayoutInflater().inflate(R.layout.custom_exercise_dialog, null);
        //Component
        EditText customExerciseName = customExerciseView.findViewById(R.id.custom_exercise_name_et);
        ChipGroup customExerciseBodypart = customExerciseView.findViewById(R.id.custom_exercise_bodypart_group);
        ChipGroup customExerciseEquipment = customExerciseView.findViewById(R.id.custom_exercise_equipment_group);
        ChipGroup customExerciseType = customExerciseView.findViewById(R.id.custom_exercise_type_group);
        Button customExerciseOkBt = customExerciseView.findViewById(R.id.custom_exercise_ok_bt);

        TextView custom_exercise_dialog_tv = customExerciseView.findViewById(R.id.custom_exercise_dialog_tv);
        View custom_exercise_dialog_tv_divider = customExerciseView.findViewById(R.id.custom_exercise_dialog_tv_divider);
        ChipGroup custom_exercise_weight_unit = customExerciseView.findViewById(R.id.custom_exercise_weight_unit);
        custom_exercise_dialog_tv.setVisibility(View.GONE);
        custom_exercise_dialog_tv_divider.setVisibility(View.GONE);
        custom_exercise_weight_unit.setVisibility(View.GONE);
        if(e != null  && e.getExType().contains("무게")){
            custom_exercise_dialog_tv.setVisibility(View.VISIBLE);
            custom_exercise_dialog_tv_divider.setVisibility(View.VISIBLE);
            custom_exercise_weight_unit.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < custom_exercise_weight_unit.getChildCount(); i++) {
            Chip chip = (Chip) custom_exercise_weight_unit.getChildAt(i);
            if(e != null && e.getExType().contains("무게") && chip.getText().toString().contains(e.getExWeightUnit())){
                weightUnitIndex = chip.getId();
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customExerciseEquipment.setBackgroundResource(R.drawable.custom_exercise_normal_border);
                }
            });
        }

        for (int i = 0; i < customExerciseBodypart.getChildCount(); i++) {
            Chip chip = (Chip) customExerciseBodypart.getChildAt(i);
            if(e != null && chip.getText().toString().equals(e.getExBodypart())){
                bodyPartIndex = chip.getId();

            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customExerciseBodypart.setBackgroundResource(R.drawable.custom_exercise_normal_border);
                }
            });
        }
        for (int i = 0; i < customExerciseEquipment.getChildCount(); i++) {
            Chip chip = (Chip) customExerciseEquipment.getChildAt(i);
            if(e != null && chip.getText().toString().equals(e.getExEquipment())){
                equipementIndex = chip.getId();
            }
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customExerciseEquipment.setBackgroundResource(R.drawable.custom_exercise_normal_border);
                }
            });
        }
        int j = 0;
        if(customExerciseType.getCheckedChipIds().size() == 2){
            List<Integer> list = customExerciseType.getCheckedChipIds();
            for (int i = 0; i < 3; i++) {
                Chip chip = (Chip) customExerciseType.getChildAt(i);
                if (!list.contains(chip.getId())) {
                    chip.setCheckable(false);
                }
            }
        }


        for (int i = 0; i < 3; i++) {
            Chip chip = (Chip) customExerciseType.getChildAt(i);

            if(e != null && e.getExType().contains(chip.getText().toString())) {
                typeIndex[j++] = chip.getId();
            }

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(chip.getText().toString().equals("무게") && chip.isChecked()){
                        custom_exercise_dialog_tv.setVisibility(View.VISIBLE);
                        custom_exercise_dialog_tv_divider.setVisibility(View.VISIBLE);
                        custom_exercise_weight_unit.setVisibility(View.VISIBLE);
                    }else if(chip.getText().toString().equals("무게") && !chip.isChecked()){
                        custom_exercise_weight_unit.clearCheck();
                        custom_exercise_dialog_tv.setVisibility(View.GONE);
                        custom_exercise_dialog_tv_divider.setVisibility(View.GONE);
                        custom_exercise_weight_unit.setVisibility(View.GONE);
                    }
                    if (customExerciseType.getCheckedChipIds().size() == 2) {
                        List<Integer> list = customExerciseType.getCheckedChipIds();
                        for (int i = 0; i < 3; i++) {
                            Chip chip = (Chip) customExerciseType.getChildAt(i);
                            if (!list.contains(chip.getId())) {
                                chip.setCheckable(false);
                            }
                        }
                    } else {
                        for (int i = 0; i < 3; i++) {
                            Chip chip = (Chip) customExerciseType.getChildAt(i);
                            chip.setCheckable(true);
                        }
                    }
                }
            });
        }


        customExerciseOkBt.setOnClickListener(new View.OnClickListener() {
            long insertRes;
            int udpRes;
            @Override
            public void onClick(View view) {
                if (customExerciseName.getText().toString().equals("")) {
                    customExerciseName.requestFocus();
                } else if (customExerciseBodypart.getCheckedChipId() == -1) {
                    customExerciseBodypart.setBackgroundResource(R.drawable.custom_exercise_yellow_border);
                } else if (customExerciseEquipment.getCheckedChipId() == -1) {
                    customExerciseEquipment.setBackgroundResource(R.drawable.custom_exercise_yellow_border);
                } else if (customExerciseType.getCheckedChipIds().isEmpty()) {
                    customExerciseType.setBackgroundResource(R.drawable.custom_exercise_yellow_border);
                } else if (custom_exercise_weight_unit.getCheckedChipIds().isEmpty()) {
                    custom_exercise_weight_unit.setBackgroundResource(R.drawable.custom_exercise_yellow_border);
                } else {
                    String exerciseName = customExerciseName.getText().toString();
                    Chip chip = customExerciseBodypart.findViewById(customExerciseBodypart.getCheckedChipId());
                    String bodypart = chip.getText().toString();
                    chip = customExerciseEquipment.findViewById(customExerciseEquipment.getCheckedChipId());
                    String equipment = chip.getText().toString();
                    List<Integer> list = customExerciseType.getCheckedChipIds();
                    String exerciseType = "";
                    for (int i = 0; i < list.size(); i++) {
                        chip = (Chip) customExerciseType.getChildAt(i);
                        exerciseType += chip.getText().toString();
                        if (i != list.size() - 1) exerciseType += "/";
                    }
                    String finalExerciseType = exerciseType;
                    String weightUnit ="";
                    if(e==null) {
                        if(finalExerciseType.contains("무게")){
                            chip = custom_exercise_weight_unit.findViewById(custom_exercise_weight_unit.getCheckedChipId());
                            weightUnit = chip.getText().toString();
                        }
                        if(weightUnit.equals("")){
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    insertRes = appDatabase.exerciseDao().insertExercise(
                                            exerciseName, bodypart, equipment, finalExerciseType);
                                }
                            });
                        }else{
                            String finalWeightUnit = weightUnit;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {

                                    insertRes = appDatabase.exerciseDao().insertExercise(
                                            exerciseName, bodypart, equipment, finalExerciseType, finalWeightUnit);
                                }
                            });
                        }
                        if (insertRes == 0) {
                            showAlertSimpleMessage(getActivity(), "성공", "운동 등록에 성공했습니다.");
                            customExerciseName.setText("");
                            customExerciseBodypart.clearCheck();
                            customExerciseEquipment.clearCheck();
                            customExerciseType.clearCheck();
                            custom_exercise_weight_unit.clearCheck();
                            customExerciseBottomSheetDialog.dismiss();

                        } else if (insertRes == -1) {
                            showAlertSimpleMessage(getActivity(), "실패", "운동 등록에 실패했습니다.");
                        }
                    }
                    else {
                        if(finalExerciseType.contains("무게")){
                            chip = custom_exercise_weight_unit.findViewById(custom_exercise_weight_unit.getCheckedChipId());
                            weightUnit = chip.getText().toString();
                        }
                        if(weightUnit.equals("")){
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    udpRes = appDatabase.exerciseDao().updateCustomExercise(
                                            e.getExId(),exerciseName, bodypart, equipment, finalExerciseType);
                                }
                            });
                        }else{
                            String finalWeightUnit = weightUnit;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    udpRes = appDatabase.exerciseDao().updateCustomExercise(
                                            e.getExId(),exerciseName, bodypart, equipment,
                                            finalExerciseType,finalWeightUnit);
                                }
                            });
                        }
                        if (udpRes == 0) {
                            showAlertSimpleMessage(getActivity(), "성공", "운동 업데이트에 성공했습니다.");
                            customExerciseName.setText("");
                            customExerciseBodypart.clearCheck();
                            customExerciseEquipment.clearCheck();
                            customExerciseType.clearCheck();
                            custom_exercise_weight_unit.clearCheck();
                            exerciseListItemAdapter.notifyItemChanged(position);
                            customExerciseBottomSheetDialog.dismiss();


                        } else if (udpRes == -1) {
                            showAlertSimpleMessage(getActivity(), "실패", "운동 업데이트에 실패했습니다.");
                        }
                    }
                }
            }
        });

        if(e != null){
            customExerciseOkBt.setText("운동 편집하기");
            customExerciseName.setText(e.getExName());
            customExerciseBodypart.check(bodyPartIndex);
            customExerciseEquipment.check(equipementIndex);
            customExerciseType.check(typeIndex[0]);
            if(typeIndex[1]!=-1) customExerciseType.check(typeIndex[1]);
            if(weightUnitIndex != -1) custom_exercise_weight_unit.check(weightUnitIndex);
        }
        customExerciseBottomSheetDialog.setContentView(customExerciseView);
        ((View) customExerciseView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        setupRatio(customExerciseBottomSheetDialog);
        customExerciseBottomSheetDialog.show();
    }

    //지정된 비율로 bottomSheetDialog 사이즈 조절
    private void setupRatio(BottomSheetDialog customExerciseBottomSheetDialog) {
        //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        //id = android.support.design.R.id.design_bottom_sheet for support librares
        FrameLayout bottomSheet = customExerciseBottomSheetDialog
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = getBottomSheetDialogDefaultHeight();
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    //bottomSheetDialog
    private int getBottomSheetDialogDefaultHeight() {
        return getWindowHeight() * 84 / 100;
    }
    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
    //간단한 메시지를 보여주는 다이얼로그
    private void showAlertSimpleMessage(Context context, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title).setMessage(content);

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }
    //운동 라이브러리 프래그먼트에서 하단 운동 만들기 버튼에 margin값 설정
    public void settingBottomOfExerciseListFragment(int margin) {
        //exerciseListAddBt.setVisibility(View.GONE);
        GetDp getDp = new GetDp();
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.2f
        );
        param.setMargins(0, 0, 0, getDp.getDp(requireActivity().getApplicationContext(), margin));
        exerciseListFragmentBottomLayout.setLayoutParams(param);
        if(from.equals("home") || from.equals("mypage")) exerciseListAddBt.setVisibility(View.GONE);
        else exerciseListAddBt.setVisibility(View.VISIBLE);
    }
    private int bodypartToTabPosition(String bodypart) {
        int num = 0;
        switch (bodypart) {
            case "가슴":
                num = 1;
                break;
            case "어깨":
                num = 2;
                break;
            case "등":
                num = 3;
                break;
            case "하체":
                num = 4;
                break;
            case "팔":
                num = 5;
                break;
            case "유산소":
                num = 6;
                break;
            case "복근":
                num = 7;
                break;
        }
        ;
        return num;
    }
    public void removeExIdExerciseIdList(int exId){
        exerciseList.remove(exId);
    }
    private void showViewExerciseAlertDialog(Context context, Exercise exercise, int position) {
        AlertDialog.Builder viewExerciseAlert_builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewExerciseView = layoutInflater.inflate(R.layout.exercise_view_diaglog, null);

        TextView exercise_view_name = viewExerciseView.findViewById(R.id.exercise_view_name);
        exercise_view_name.setText(exercise.getExName());
        TextView exercise_view_bodypart_content_tv = viewExerciseView.findViewById(R.id.exercise_view_bodypart_content_tv);
        exercise_view_bodypart_content_tv.setText(exercise.getExBodypart());
        TextView exercise_view_equipment_content_tv = viewExerciseView.findViewById(R.id.exercise_view_equipment_content_tv);
        exercise_view_equipment_content_tv.setText(exercise.getExEquipment());
        TextView exercise_view_weightUnit_content_tv = viewExerciseView.findViewById(R.id.exercise_view_weightUnit_content_tv);
        exercise_view_weightUnit_content_tv.setText(exercise.getExWeightUnit());
        TextView exercise_view_exType_content_tv = viewExerciseView.findViewById(R.id.exercise_view_exType_content_tv);
        exercise_view_exType_content_tv.setText(exercise.getExType());

        Button exercise_view_edit = viewExerciseView.findViewById(R.id.exercise_view_edit);
        ImageView exercise_view_image = viewExerciseView.findViewById(R.id.exercise_view_image);
        if(exercise.getExImageUrl() == null || exercise.getExImageUrl().equals("") ){
            Glide.with(exercise_view_image.getRootView().getContext())
                    .load(R.drawable.exercise_no_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(exercise_view_image);
        }else{
            Glide.with(exercise_view_image.getRootView().getContext()).asGif()
                    .load(exercise.getExImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(exercise_view_image);
        }

        //다이어로그 보이기
        viewExerciseAlert_builder.setView(viewExerciseView);
        AlertDialog alertDialog_viewExercise = viewExerciseAlert_builder.create();
        alertDialog_viewExercise.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog_viewExercise.show();

        exercise_view_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (exercise_view_edit.getText().toString()){
                    case "편집" :{
                        showCustomExerciseDialog(requireActivity().getApplicationContext(),exercise,position);
                        alertDialog_viewExercise.dismiss();
                        break;
                    }
                    case "저장" :{
                        break;
                    }
                }
            }
        });
        //다이어로그 크기 조절
        WindowManager.LayoutParams params = alertDialog_viewExercise.getWindow().getAttributes();
        DisplayMetrics dm = alertDialog_viewExercise.getContext().getResources().getDisplayMetrics();
        // int widthDp = Math.round(400 * dm.density);
        int heightDp = Math.round(600 * dm.density);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = heightDp;
        alertDialog_viewExercise.getWindow().setAttributes(params);
    }

    @Override
    public void onBackPressed() {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
