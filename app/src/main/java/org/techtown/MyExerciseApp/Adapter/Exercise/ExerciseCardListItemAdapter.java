package org.techtown.MyExerciseApp.Adapter.Exercise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.MyExerciseApp.AutoCounter.PushUpCountdownDialogFragment;
import org.techtown.MyExerciseApp.AutoCounter.RunningCountdownDialogFragment;
import org.techtown.MyExerciseApp.AutoCounter.SquatCountdownDialogFragment;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseTableInfo;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Interface.ClickCallbackListener;
import org.techtown.MyExerciseApp.MyClass.GetDp;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.techtown.MyExerciseApp.db.Entity.ExerciseMemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class ExerciseCardListItemAdapter extends RecyclerView.Adapter<ExerciseCardListItemAdapter.ViewHolder> {
    String className = "ExerciseCardListItemAdapter";
    private List<Exercise> items = new ArrayList<>();
    private final HashMap<Integer, ExerciseTableInfo> hashMap = new LinkedHashMap<>();
    private HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> hashMap2 = new LinkedHashMap<>();
    private List<RoutineInfoListItem> routineInfoListItemArrayList;
    private HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> routineHashMap;
    private final ArrayList<ExerciseMemo> arrayListExerciseMemo = new ArrayList<>();
    private ExerciseMemoListItemAdapter exerciseMemoListItemAdapter;
    private LifecycleOwner lifecycleOwner;
    private Context mContext;
    private int tabPosition;
    private final HashMap<Integer, Integer> temp = new HashMap<>();
    private ClickCallbackListener clickCallbackListener;
    private final GetToday getToday = new GetToday();

    private AppDatabase appDatabase;
    private boolean hasTodayMemo, doDeleteMemo;
    private HashMap<Integer, Double> hashMapTotalWeight;

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Exercise> getItems() {
        return items;
    }

    public HashMap<Integer, ExerciseTableInfo> getHashMap() {
        return hashMap;
    }

    public HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> getHashMap2() {
        return hashMap2;
    }

    public void setItem(List<Exercise> data) {
        items = data;
        for (Exercise exercise : items) {
            setHashMap(exercise);
            setHashMap2(exercise);
        }
        System.out.println("setItem 실행");
        notifyDataSetChanged();
    }

    private void setHashMap(Exercise exercise) {
        if (hashMap.isEmpty() || !hashMap.containsKey(exercise.getExId())) {

            ExerciseTableInfo exerciseTableInfo = new ExerciseTableInfo(exercise.getExBodypart() + " | " + exercise.getExName());

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("세트");
            String[] exTypeArr = exercise.getExType().split("/");
            String exType1 = exTypeArr[0];
            if (exType1.equals("무게")) {
                exType1 += "(" + exercise.getExWeightUnit() + ")";
            }
            arrayList.add(exType1);
            if (exTypeArr.length > 1) {
                String exType2 = exercise.getExType().split("/")[1];
                if (exType2 != null || !exType2.equals("")) {
                    arrayList.add(exType2);
                }
            }
            arrayList.add("완료");
            exerciseTableInfo.setExerciseTableHeader(arrayList);
            hashMap.put(exercise.getExId(), exerciseTableInfo);

        }
    }

    private void setHashMap2(Exercise exercise) {
        if (hashMap2.isEmpty() || !hashMap2.containsKey(exercise.getExId())) {
            hashMap2.put(exercise.getExId(), new ArrayList<RoutineInfoListItemDetailed>());
        }
    }

    public void addItem(Exercise exercise, int position) {
        if (!hashMap2.containsKey(exercise.getExId())) {
            items.add(exercise);
            setHashMap(exercise);
            setHashMap2(exercise);
            notifyItemInserted(position);
        }
    }

    public void setRoutine(List<RoutineInfoListItem> routineInfoListItemArrayList,
                           HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> currentHashMap) {
        ArrayList<RoutineInfoListItem> originList = new ArrayList<>(); // 원래 운동 저장
        List<Exercise> exerciseList = getItems(); // 현재 운동 리스트
        Set<Integer> set = new HashSet<>();
        if (exerciseList.isEmpty()) {
            for (RoutineInfoListItem ri : routineInfoListItemArrayList) {
                exerciseList.add(ri.getExercise());
                ArrayList<RoutineInfoListItemDetailed> ar = ri.getRoutineInfoListItemDetailedArrayList();
                currentHashMap.put(ri.getExercise().getExId(), ar);
            }
            this.routineInfoListItemArrayList = routineInfoListItemArrayList;
            this.routineHashMap = currentHashMap;
            setItem(exerciseList);
        } else {
            for (int i = 0; i < exerciseList.size(); i++) {
                originList.add(
                        new RoutineInfoListItem(exerciseList.get(i), currentHashMap.get(exerciseList.get(i).getExId()))
                );
                set.add(exerciseList.get(i).getExId());

            }
            int originListSize = originList.size();
            for (RoutineInfoListItem r : routineInfoListItemArrayList) {
                Exercise addEx = r.getExercise();
                ArrayList<RoutineInfoListItemDetailed> addDetails = r.getRoutineInfoListItemDetailedArrayList();
                int sameIndex = -1;
                if (set.contains(addEx.getExId())) {
                    for (int i = 0; i < originListSize; i++) {
                        RoutineInfoListItem originItems = originList.get(i);
                        Exercise originEx = originItems.getExercise();
                        ArrayList<RoutineInfoListItemDetailed> originDetails = originItems.getRoutineInfoListItemDetailedArrayList();
                        if (originDetails == null)
                            originDetails = new ArrayList<RoutineInfoListItemDetailed>();
                        if (addEx.getExId() == originEx.getExId()) {
                            sameIndex = i;
                            int lastIndex = 0;
                            int originDetailsSize = originDetails.size();
                            if (originDetailsSize >= 1) {
                                lastIndex = originDetails.get(originDetails.size() - 1).getSet();
                                for (int j = 0; j < addDetails.size(); j++) {
                                    addDetails.get(j).setSet(addDetails.get(j).getSet() + lastIndex);
                                }
                            }
                            originDetails.addAll(addDetails);
                            currentHashMap.put(originEx.getExId(), originDetails);
                            originList.get(sameIndex).setRoutineInfoListItemDetailedArrayList(originDetails);

                        }
                    }
                } else {
                    currentHashMap.put(addEx.getExId(), addDetails);
                    originList.add(r);
                    exerciseList.add(r.getExercise());
                }
            }
            this.hashMap2 = currentHashMap;
            this.routineHashMap = currentHashMap;
            this.routineInfoListItemArrayList = originList;
            RoutineInfoListItem routineInfoListItem = originList.get(0);
            setItem(exerciseList);

        }
    }


    @Override
    @NonNull
    public ExerciseCardListItemAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.exercise_card, viewGroup, false);
        this.mContext = viewGroup.getContext();
        hashMapTotalWeight = new HashMap<>();
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        appDatabase = AppDatabase.getDBInstance(mContext);
        return new ExerciseCardListItemAdapter.ViewHolder(view);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.onBind(items.get(position), position);

        //Preload
        if (position <= items.size()) {
            int endPosition = (position + 6 > items.size()) ? items.size() : position + 6;
            List<Exercise> list = items.subList(position, endPosition);
            Context context = viewHolder.itemView.getRootView().getContext();

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView exerciseCardName, exerciseTotalWeight, exerciseTotalMyWeight, headerWeightTv, headerRepsTv; // 각 운동
        private TextView auto_describe;
        private Button exerciseCardDeleteBt, exerciseCardMemoBt, exerciseCardSettingBt, exerciseCardSetAddBt, exerciseCardSetDeleteBt;
        private TableLayout tableLayout;
        private TableRow tableHeader;

        private Exercise exercise;
        private double totalWeightSum = 0, totalMyWeightSum = 0, totalSum = 0, totalMySum = 0;
        private int lastSetWeightId, lastSetRepsId;
        private String weightUnit, tempWeightUnit, lastWeight, lastReps;
        private long restTime;
        private Long mLastClickTime = 0L; // 버튼 중복 클릭 제한 초
        private String exType, exType1, exType2;
        private boolean oneType, weightReps;
        private String startTime = null;

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        private final Context mContext;

        ViewHolder(View view) {
            super(view);
            init(view);
            mContext = view.getContext();
            setOnClickListener();
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });


        }

        public void onBind(Exercise exercise, int position) {
            List<RoutineInfoListItemDetailed> details = new ArrayList<>();
            boolean isDetailesNull = false;
            if (routineInfoListItemArrayList != null && !routineInfoListItemArrayList.isEmpty()) {
                isDetailesNull = false;
                details = routineHashMap.get(exercise.getExId());
            } else {
                isDetailesNull = true;
            }

            this.exercise = exercise;

            this.weightUnit = exercise.getExWeightUnit();
            this.restTime = exercise.getExRestTime();
            this.exType = exercise.getExType();
            this.exType1 = exType.split("/")[0];
            if (exType.split("/").length > 1) {
                this.exType2 = exType.split("/")[1];
                oneType = false;
            } else {
                this.exType2 = "";
                oneType = true;
            }
            exerciseCardName.setText(exercise.getExBodypart() + " | " + exercise.getExName());
            setTableHeader(exType1, exType2, weightUnit, tableHeader);
            switch (exType) {
                case "무게/횟수": {
                    double sum = 0.0;
                    if (!isDetailesNull) {
                        if (details != null) {
                            for (RoutineInfoListItemDetailed detailed : details) {
                                sum += Double.parseDouble(detailed.getVal1()) + Integer.parseInt(detailed.getVal2());
                            }
                        }
                    }
                    exerciseTotalWeight.setText("설정 운동량 " + sum + weightUnit);
                    exerciseTotalMyWeight.setText("수행 운동량 " + "0.0" + weightUnit);
                    weightReps = true;
                    break;
                }
                case "횟수/": {
                    int sum = 0;
                    if (!isDetailesNull) {
                        if (details != null) {
                            for (RoutineInfoListItemDetailed detailed : details) {
                                sum += Integer.parseInt(detailed.getVal1());
                            }
                        }
                    }
                    exerciseTotalWeight.setText("설정 횟수 " + sum + "회");
                    exerciseTotalMyWeight.setText("수행 횟수 0회");
                    break;
                }
                case "시간/": {
                    int m = 0;
                    int s = 0;
                    if (!isDetailesNull) {
                        if (details != null) {
                            for (RoutineInfoListItemDetailed detailed : details) {
                                m += Integer.parseInt(detailed.getVal1().split(" ")[0].split("분")[0]);
                                s += Integer.parseInt(detailed.getVal1().split(" ")[1].split("초")[0]);
                                m = s / 60;
                                s = s % 60;
                            }
                        }
                    }
                    if (m <= 0) exerciseTotalWeight.setText("설정 시간 " + s + "초");
                    else if (m > 0) exerciseTotalWeight.setText("설정 시간 " + m + "분 " + s + "초");
                    exerciseTotalMyWeight.setText("수행 시간 0초");
                    break;
                }
                case "무게/": {
                    double sum = 0.0;
                    if (!isDetailesNull) {
                        if (details != null) {
                            for (RoutineInfoListItemDetailed detailed : details) {
                                sum += Double.parseDouble(detailed.getVal1());
                            }
                        }
                    }
                    exerciseTotalWeight.setText("설정 무게 " + sum + weightUnit);
                    exerciseTotalMyWeight.setText("수행 무게 0.0" + weightUnit);
                    break;
                }
                default: {
                    exerciseTotalWeight.setVisibility(View.GONE);
                    exerciseTotalMyWeight.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tableLayout.getLayoutParams();
                    GetDp g = new GetDp();
                    layoutParams.setMargins(g.getDp(mContext, 20), g.getDp(mContext, 50), g.getDp(mContext, 20), g.getDp(mContext, 30));
                    tableLayout.setLayoutParams(layoutParams);
                    break;
                }
            }
            if (!isDetailesNull) {

                if (details != null) {
                    if (tableLayout.getChildCount() > 1) {
                        cleanSet(tableLayout);
                    }
                    for (RoutineInfoListItemDetailed detailed : details) {
                        addSet(mContext, tableLayout, detailed);
                    }
                }
            }

            hashMap.put(exercise.getExId(),
                    getExercisdCardSetInfo(tableLayout, hashMap.get(exercise.getExId())));
            hashMap2.put(exercise.getExId(), getRoutineDetails(tableLayout, hashMap2.get(exercise.getExId())));

            exerciseCardSetDeleteBt.setEnabled(false);


        }

        private void init(View view) {
            exerciseCardName = view.findViewById(R.id.exercise_card_name);
            exerciseTotalWeight = view.findViewById(R.id.exercise_total_weight);
            exerciseTotalMyWeight = view.findViewById(R.id.exercise_total_my_weight);
            exerciseCardDeleteBt = view.findViewById(R.id.exercise_card_delete_bt);
            exerciseCardMemoBt = view.findViewById(R.id.exercise_card_memo_bt);
            exerciseCardSettingBt = view.findViewById(R.id.exercise_card_setting_bt);
            exerciseCardSetAddBt = view.findViewById(R.id.exercise_card_set_add_bt);
            exerciseCardSetDeleteBt = view.findViewById(R.id.exercise_card_set_delete_bt);
            tableLayout = view.findViewById(R.id.exercise_card_set_table_layout);
            tableHeader = view.findViewById(R.id.exercise_card_header);
            headerWeightTv = view.findViewById(R.id.header_weight_tv);
            headerRepsTv = view.findViewById(R.id.header_reps_tv);
            auto_describe = view.findViewById(R.id.auto_describe);

        }

        //운동 카드 버튼 setting 운동 삭제, 메모, 쉬는 시간, 운동 세트 추가/삭제
        private void setOnClickListener() {
            View.OnClickListener clickEvent = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 버튼 중복 클릭 막기
                    switch (v.getId()) {
                        case R.id.exercise_card_delete_bt:
                            if (SystemClock.elapsedRealtime() - mLastClickTime > 500) {
                                cleanSet(tableLayout);
                                if (items.size() > 0) {
                                    hashMap.remove(items.get(getAdapterPosition()).getExId());
                                    hashMap2.remove(items.get(getAdapterPosition()).getExId());
                                    items.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    clickCallbackListener.callExerciseCount(items);
                                }
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            break;

                        case R.id.exercise_card_memo_bt:
                            //메모 작성 삭제
                            showWriteExerciseMemoAlertDialog(v.getContext());
                            break;

                        case R.id.exercise_card_setting_bt:
                            //무게 단위 조절 쉬는 시간 조절 운동 이름 교체하기
                            showEditExerciseAlertDialog(v.getContext(), exercise);
                            break;

                        case R.id.exercise_card_set_add_bt:
                            //운동 목록 카드에 운동 세트 추가
                            addSet(v.getContext(), tableLayout, null);
                            hashMap.put(exercise.getExId(),
                                    getExercisdCardSetInfo(tableLayout, hashMap.get(exercise.getExId())));
                            hashMap2.put(exercise.getExId(),
                                    getRoutineDetails(tableLayout, hashMap2.get(exercise.getExId())));
                            System.out.println("this is whem add Set getRoutineDetailsSize++" +
                                    ((ArrayList<RoutineInfoListItemDetailed>) hashMap2.get(exercise.getExId())).size()
                            );
                            break;

                        case R.id.exercise_card_set_delete_bt:
                            //운동 목록 카드에 세트 삭제
                            deleteSet(tableLayout);

                            hashMap.put(exercise.getExId(),
                                    getExercisdCardSetInfo(tableLayout, hashMap.get(exercise.getExId())));
                            hashMap2.put(exercise.getExId(),
                                    getRoutineDetails(tableLayout, hashMap2.get(exercise.getExId())));

                            break;

                    }
                }
            };

            exerciseCardDeleteBt.setOnClickListener(clickEvent);
            exerciseCardMemoBt.setOnClickListener(clickEvent);
            exerciseCardSettingBt.setOnClickListener(clickEvent);
            exerciseCardSetAddBt.setOnClickListener(clickEvent);
            exerciseCardSetDeleteBt.setOnClickListener(clickEvent);


        }

        //세트 추가
        public void addSet(Context context, TableLayout tableLayout, RoutineInfoListItemDetailed setDatailed) {
            boolean isSetDetailNull = setDatailed == null;
            int lastIndex = tableLayout.getChildCount();
            TableRow tableRow = new TableRow(context);
            TextView textView = new TextView(context);//세트
            EditText editText1 = new EditText(context);//무게 - 첫번째 editText

            EditText editText2 = new EditText(context);//횟수 - 두번째 editText

            CheckBox checkBox = new CheckBox(context);//완료 체크박스
            String editVal1, editVal2 = null;
            String lastWeight, lastReps;

            if (lastIndex == 1) {
                switch (exType.split("/")[0]) {
                    case "시간":
                        editVal1 = "0분 0초";
                        break;
                    default:
                        editVal1 = String.valueOf(0);
                        break;
                }
                if (!oneType) {
                    switch (exType.split("/")[1]) {
                        case "시간":
                            editVal2 = "0분 0초";
                            break;
                        default:
                            editVal2 = String.valueOf(0);
                            break;
                    }
                }

            } else {
                TableRow lastRow = (TableRow) tableLayout.getChildAt(lastIndex - 1);
                editVal1 = String.valueOf(((EditText) lastRow.getChildAt(1)).getText());
                if (!oneType) {
                    editVal2 = String.valueOf(((EditText) lastRow.getChildAt(2)).getText());
                }
                setTotal(tableLayout);
            }


            //Id연결
            lastSetWeightId = View.generateViewId();
            editText1.setId(lastSetWeightId);
            if (!oneType) {
                lastSetRepsId = View.generateViewId();
                editText2.setId(lastSetRepsId);
            }

            //setText

            textView.setText(String.valueOf(lastIndex));
            editText1.setText(editVal1);
            if (!oneType) {
                editText2.setText(editVal2);
            }
            if (!isSetDetailNull) {
                textView.setText(String.valueOf(setDatailed.getSet()));
                editText1.setText(String.valueOf(setDatailed.getVal1()));
                if (!oneType) {
                    editText2.setText(String.valueOf(setDatailed.getVal2()));
                }
                checkBox.setChecked(setDatailed.isCheck());
            }

            //setInputType and setTextSize
            editText1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            editText2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            //setListener - Key, OnClick , TextChanged
            TextWatcher textWatcher = new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null && !s.toString().isEmpty()) {
                        setTotal(tableLayout);
                        setLastWeightReps(editText1, editText2);
                        setTotalMy(tableLayout);
                        hashMap.put(exercise.getExId(),
                                getExercisdCardSetInfo(tableLayout, hashMap.get(exercise.getExId())));
                        hashMap2.put(exercise.getExId(),
                                getRoutineDetails(tableLayout, hashMap2.get(exercise.getExId())));
                    }
                }

                public void afterTextChanged(Editable s) {
                    if (s != null && !s.toString().isEmpty()) {
                        setLastWeightReps(editText1, editText2);
                        setTotal(tableLayout);
                        hashMap.put(exercise.getExId(),
                                getExercisdCardSetInfo(tableLayout, hashMap.get(exercise.getExId())));
                        hashMap2.put(exercise.getExId(),
                                getRoutineDetails(tableLayout, hashMap2.get(exercise.getExId())));
                    }
                }
            };
            View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        if (editText1.getText().toString().length() == 0) {
                            editText1.setText("0");
                        }
                        if (editText2.getText().toString().length() == 0) {
                            editText2.setText("0");
                        }
                    }
                }
            };

            editText2.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            if (exType.contains("무게/")) {
                editText1.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
            } else if (exType.contains("시간/")) {
                editText1.setFocusable(false);
                editText1.setCursorVisible(false);
                editText1.setKeyListener(null);
                editText1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddRowTimeDialog(context, editText1);
                    }
                });
            } else if (exType.contains("/시간")) {
                editText2.setFocusable(false);
                editText2.setCursorVisible(false);
                editText2.setKeyListener(null);
                editText2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddRowTimeDialog(context, editText2);
                    }
                });

            } else {
                editText1.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            }

            editText1.addTextChangedListener(textWatcher);
            editText2.addTextChangedListener(textWatcher);
            editText1.setOnFocusChangeListener(onFocusChangeListener);
            editText2.setOnFocusChangeListener(onFocusChangeListener);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int restTime = exercise.getExRestTime();
                    boolean checked = ((CheckBox) view).isChecked();
                    if (checked) {
                        //운동 타이머 시작 callBack
                        clickCallbackListener.callTotalExerciseTimerStart();
                        setDeleteButtonEnabled(tableLayout);
                        setTotalMy(tableLayout);
                        //해당 쉬는시간으로 세팅 후, 타이머 시작
                        if (clickCallbackListener != null) {
                            clickCallbackListener.callBackRestTime(restTime, true);
                        }
                        if (getStartTime() == null || getStartTime().equals("")) {
                            setStartTime(getToday.getTodayTime());
                            clickCallbackListener.callBackStartTime(getStartTime());
                        }

                    } else {
                        exerciseCardSetDeleteBt.setEnabled(true);
                        if (clickCallbackListener != null) {
                            clickCallbackListener.callBackRestTime(restTime, false);
                        }
                        setTotalMy(tableLayout);
                    }
                    hashMap.put(exercise.getExId(),
                            getExercisdCardSetInfo(tableLayout, hashMap.get(exercise.getExId())));
                    hashMap2.put(exercise.getExId(),
                            getRoutineDetails(tableLayout, hashMap2.get(exercise.getExId())));
                    setDeleteButtonEnabled(tableLayout);
                }
            });

            //컴포넌트 백그라운드 추가
            editText1.setBackgroundResource(R.drawable.exercise_card_table_body_border);
            editText2.setBackgroundResource(R.drawable.exercise_card_table_body_border);


            //setGravity and setLayoutParams
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            tableRow.setLayoutParams(params);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1f);
            layoutParams.gravity = Gravity.CENTER;
            LinearLayout.LayoutParams layoutParams2 = new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT
            );
            layoutParams2.gravity = Gravity.CENTER_HORIZONTAL;

            //테이블에 로우추가
            tableRow.addView(textView);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            tableRow.addView(editText1);// 무게
            editText1.setLayoutParams(layoutParams);
            editText1.setGravity(Gravity.CENTER);

            if (!oneType) {
                tableRow.addView(editText2);// 무게
                editText2.setLayoutParams(layoutParams);
                editText2.setGravity(Gravity.CENTER);
            }
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.addView(checkBox);
            tableRow.addView(linearLayout);

            tableLayout.addView(tableRow);
            setTotal(tableLayout);
            setDeleteButtonEnabled(tableLayout);
            String exName = exercise.getExName().trim().toLowerCase();
            if(
                    ( (exName.contains("푸시")||exName.contains("푸쉬")) && exName.contains("업") )
                || ( exName.contains("push") && exName.contains("up"))
            ){
                if (exType.contains("횟수")) {
                    auto_describe.setVisibility(View.VISIBLE);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int reps = Integer.parseInt(editText1.getText().toString());
                            if (!oneType) reps = Integer.parseInt(editText2.getText().toString());
                            showDialog(tableRow, reps,"pushUp");
                        }
                    });
                }
            }
            else if(exName.contains("스쿼트")||exName.contains("squat")){
                if (exType.contains("횟수")) {
                    auto_describe.setVisibility(View.VISIBLE);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int reps = Integer.parseInt(editText1.getText().toString());
                            if (!oneType) reps = Integer.parseInt(editText2.getText().toString());
                            showDialog(tableRow, reps,"squat");
                        }
                    });
                }
            }else if(exName.contains("달리기")){
                if (exType.contains("시간")&&exType.contains("거리")) {
                    auto_describe.setVisibility(View.VISIBLE);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDialog(tableRow, 0,"running");
                        }
                    });
                }
            }
        }

        public void showDialog(TableRow tableRow, int reps,String mode) {
            if(mode.equals("pushUp")) {
                int initialCount = reps; // 초기 카운트 설정

                PushUpCountdownDialogFragment dialogFragment = PushUpCountdownDialogFragment.newInstance(tableRow, initialCount);
                dialogFragment.setOnCountdownCompleteListener(new PushUpCountdownDialogFragment.OnCountdownCompleteListener() {
                    @Override
                    public void onCountdownComplete(int totalCount) {

                        ((CheckBox)((LinearLayout) tableRow.getChildAt(tableRow.getChildCount() - 1)).getChildAt(0)).setChecked(true);
                        ((EditText) tableRow.getChildAt(tableRow.getChildCount() - 2)).setText(totalCount + "");
                    }
                });
                dialogFragment.setCancelable(false);
                dialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "push_up_dialog");
            }
            else if(mode.equals("squat")){
                int initialCount = reps; // 초기 카운트 설정

                SquatCountdownDialogFragment dialogFragment = SquatCountdownDialogFragment.newInstance(tableRow, initialCount);
                dialogFragment.setOnCountdownCompleteListener(new SquatCountdownDialogFragment.OnCountdownCompleteListener() {
                    @Override
                    public void onCountdownComplete(int totalCount) {
                        ((CheckBox)((LinearLayout) tableRow.getChildAt(tableRow.getChildCount() - 1)).getChildAt(0)).setChecked(true);
                        ((EditText) tableRow.getChildAt(tableRow.getChildCount() - 2)).setText(totalCount + "");
                    }
                });

                dialogFragment.setCancelable(false);
                dialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "squat_dialog");
            }
            else if(mode.equals("running")){
                RunningCountdownDialogFragment dialogFragment = RunningCountdownDialogFragment.newInstance(tableRow);
                dialogFragment.setOnCountdownCompleteListener(new RunningCountdownDialogFragment.OnCountdownCompleteListener() {
                    @Override
                    public void onCountdownComplete(String time,float disatance) {
                        ((EditText)tableRow.getChildAt(1)).setText(time);
                        ((EditText)tableRow.getChildAt(2)).setText(disatance+"");
                        ((CheckBox)((LinearLayout) tableRow.getChildAt(tableRow.getChildCount() - 1)).getChildAt(0)).setChecked(true);

                    }
                });

                //dialogFragment.setCancelable(false);
                dialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "running_dialog");
            }



        }

        //세트 삭제
        private void deleteSet(TableLayout tableLayout) {
            int lastIndex = tableLayout.getChildCount();
            if (lastIndex > 1) {
                TableRow lastRow = (TableRow) tableLayout.getChildAt(lastIndex - 1);
                tableLayout.removeView(lastRow);
                setTotal(tableLayout);
                if (clickCallbackListener != null) {
                    clickCallbackListener.callRestTimerCancel();
                }
            }
            setDeleteButtonEnabled(tableLayout);
        }

        //세트 초기화
        private void cleanSet(TableLayout tableLayout) {
            int lastIndex = tableLayout.getChildCount();
            if (lastIndex > 1) {
                tableLayout.removeViews(1, lastIndex - 1);
                setTotal(tableLayout);
            }
        }

        //마지막 세트 완료 시, 세트 삭제 못하게 버튼 막기
        private void setDeleteButtonEnabled(TableLayout tableLayout) {
            if (tableLayout.getChildCount() > 1) {
                TableRow tr = (TableRow) tableLayout.getChildAt(tableLayout.getChildCount() - 1);
                LinearLayout linearLayout = (LinearLayout) tr.getChildAt(tr.getChildCount() - 1);
                CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
                exerciseCardSetDeleteBt.setEnabled(!checkBox.isChecked());
            } else if (tableLayout.getChildCount() == 1) {
                exerciseCardSetDeleteBt.setEnabled(false);
            }
        }

        //설정 무게 수정
        private void setTotal(TableLayout tableLayout) {
            String editVal1, editVal2;
            totalWeightSum = 0.0;
            totalSum = 0;
            weightUnit = exercise.getExWeightUnit();
            int lastIndex = tableLayout.getChildCount();
            if (lastIndex > 1) {
                if (exType.contains("무게/")) {
                    for (int i = 1; i < lastIndex; i++) {
                        TableRow tr = (TableRow) tableLayout.getChildAt(i);
                        editVal1 = String.valueOf(((EditText) tr.getChildAt(1)).getText());
                        totalSum += Double.parseDouble(editVal1);
                        if (weightReps) {
                            editVal2 = String.valueOf(((EditText) tr.getChildAt(2)).getText());
                            if (weightReps && !editVal1.isEmpty() && !editVal2.isEmpty()) {
                                totalWeightSum += Double.parseDouble(editVal1) * Integer.parseInt(editVal2);
                            }
                        }
                    }
                } else if (exType.equals("횟수/")) {
                    for (int i = 1; i < lastIndex; i++) {
                        TableRow tr = (TableRow) tableLayout.getChildAt(i);
                        editVal1 = String.valueOf(((EditText) tr.getChildAt(1)).getText());
                        totalSum += Integer.parseInt(editVal1);
                    }
                } else if (exType.equals("시간/")) {
                    for (int i = 1; i < lastIndex; i++) {
                        TableRow tr = (TableRow) tableLayout.getChildAt(i);
                        editVal1 = ((EditText) tr.getChildAt(1)).getText().toString();
                        if (!editVal1.equals("0")) {
                            String mStr = editVal1.split(" ")[0];
                            String sStr = editVal1.split(" ")[1];
                            mStr = mStr.substring(0, mStr.length() - 1);
                            sStr = sStr.substring(0, sStr.length() - 1);
                            totalSum += Integer.parseInt(mStr) * 60 + Integer.parseInt(sStr);
                        } else {
                            totalSum += 0;
                        }
                    }
                }

            }
            if (weightReps) {
                String totalWeightStr = totalWeightSum + " " + weightUnit;
                exerciseTotalWeight.setText("설정 운동량 " + totalWeightStr);
                if (weightUnit.equals("lbs"))
                    clickCallbackListener.callExerciseTotalWeight(getAdapterPosition(), totalWeightSum * 0.45);
                else
                    clickCallbackListener.callExerciseTotalWeight(getAdapterPosition(), totalWeightSum);
            } else {
                switch (exType) {
                    case "무게/":
                        exerciseTotalWeight.setText("설정 무게 " + totalSum + weightUnit);
                        break;
                    case "시간/":
                        int m = ((int) totalSum) / 60;
                        int s = ((int) totalSum) % 60;
                        exerciseTotalWeight.setText("설정 시간 " + m + "분 " + s + "초");
                        break;
                    case "횟수/":
                        exerciseTotalWeight.setText("설정 횟수 " + (int) totalSum + "회");
                        break;
                }
            }

        }

        //수행 무게 수정
        private void setTotalMy(TableLayout tableLayout) {
            String editVal1, editVal2;
            totalMyWeightSum = 0.0;
            totalMySum = 0;
            weightUnit = exercise.getExWeightUnit();
            int lastIndex = tableLayout.getChildCount();
            if (lastIndex > 1) {

                if (exType.contains("무게/")) {
                    for (int i = 1; i < lastIndex; i++) {
                        TableRow tr = (TableRow) tableLayout.getChildAt(i);
                        CheckBox checkBox = (CheckBox) (((LinearLayout) tr.getChildAt(tr.getChildCount() - 1)).getChildAt(0));
                        if (checkBox.isChecked()) {
                            editVal1 = String.valueOf(((EditText) tr.getChildAt(1)).getText());
                            totalMySum += Double.parseDouble(editVal1);
                            if (weightReps) {
                                editVal2 = String.valueOf(((EditText) tr.getChildAt(2)).getText());
                                if (weightReps && !editVal1.isEmpty() && !editVal2.isEmpty()) {
                                    totalMyWeightSum += Double.parseDouble(editVal1) * Integer.parseInt(editVal2);
                                }
                            }
                        }

                    }

                } else if (exType.equals("횟수/")) {
                    for (int i = 1; i < lastIndex; i++) {
                        TableRow tr = (TableRow) tableLayout.getChildAt(i);
                        CheckBox checkBox = (CheckBox) (((LinearLayout) tr.getChildAt(tr.getChildCount() - 1)).getChildAt(0));
                        if (checkBox.isChecked()) {
                            editVal1 = String.valueOf(((EditText) tr.getChildAt(1)).getText());
                            totalMySum += Integer.parseInt(editVal1);
                        }
                    }

                } else if (exType.equals("시간/")) {
                    for (int i = 1; i < lastIndex; i++) {
                        TableRow tr = (TableRow) tableLayout.getChildAt(i);
                        CheckBox checkBox = (CheckBox) (((LinearLayout) tr.getChildAt(tr.getChildCount() - 1)).getChildAt(0));

                        if (checkBox.isChecked()) {
                            editVal1 = ((EditText) tr.getChildAt(1)).getText().toString();
                            if (!editVal1.equals("0")) {
                                String mStr = editVal1.split(" ")[0];
                                String sStr = editVal1.split(" ")[1];
                                mStr = mStr.substring(0, mStr.length() - 1);
                                sStr = sStr.substring(0, sStr.length() - 1);
                                totalMySum += Integer.parseInt(mStr) * 60 + Integer.parseInt(sStr);
                            } else {
                                totalMySum += 0;
                            }
                        }
                    }
                }
            }
            if (weightReps) {
                String totalMyWeightStr = totalMyWeightSum + " " + weightUnit;
                exerciseTotalMyWeight.setText("수행 운동량 " + totalMyWeightStr);
            } else {
                switch (exType) {
                    case "무게/":
                        exerciseTotalMyWeight.setText("수행 무게 " + totalMySum + " " + weightUnit);
                        break;
                    case "시간/":
                        int m = ((int) totalMySum) / 60;
                        int s = ((int) totalMySum) % 60;
                        exerciseTotalMyWeight.setText("수행 시간 " + m + "분 " + s + "초");
                        break;
                    case "횟수/":
                        exerciseTotalMyWeight.setText("수행 횟수 " + (int) totalMySum + " 회");
                        break;
                }
            }
        }

        //마지막 무게 횟수 저장
        private void setLastWeightReps(EditText weightEt, EditText repsEt) {
            if (weightEt.getId() == lastSetWeightId) {
                this.lastWeight = String.valueOf(weightEt.getText());
                this.lastReps = String.valueOf(repsEt.getText());
            }
        }

        //운동 메모 작성
        private void showWriteExerciseMemoAlertDialog(Context context) {
            AlertDialog.Builder writeExerciseMemoAlert_builder = new AlertDialog.Builder(context);
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View writeExerciseMemoView = layoutInflater.inflate(R.layout.write_exercise_memo_dialog, null);


            //메모 데이터베이스에 현재 날짜에 해당 카드 운동 아이디를 가진 메모가 존재하면 내용을 set하고 없으면 기본 문구 출력
            ImageButton writeExerciseMemoPreArrow = writeExerciseMemoView.findViewById(R.id.write_exercise_memo_pre_arrow);
            ImageButton writeExerciseMemoNextArrow = writeExerciseMemoView.findViewById(R.id.write_exercise_memo_next_arrow);
            writeExerciseMemoNextArrow.setVisibility(View.INVISIBLE);

            //운동 메모 리사이클러 뷰
            RecyclerView writeExerciseMemoRecyclerView = writeExerciseMemoView.findViewById(R.id.write_exercise_memo_recyclerView);
            writeExerciseMemoRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            writeExerciseMemoRecyclerView.setHasFixedSize(true);

            //다이어로그 보이기
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            writeExerciseMemoRecyclerView.setLayoutManager(layoutManager);
            writeExerciseMemoAlert_builder.setView(writeExerciseMemoView);
            AlertDialog alertDialog_writeExerciseMemo = writeExerciseMemoAlert_builder.create();
            alertDialog_writeExerciseMemo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog_writeExerciseMemo.show();

            //스크롤 방향 역순
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false);
            linearLayoutManager.setReverseLayout(true);
            //linearLayoutManager.setStackFromEnd(true);
            writeExerciseMemoRecyclerView.setLayoutManager(linearLayoutManager);

            //아이템 하나씩 보이기
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(writeExerciseMemoRecyclerView);

            //다이어로그 크기 조절
            WindowManager.LayoutParams params = alertDialog_writeExerciseMemo.getWindow().getAttributes();
            DisplayMetrics dm = alertDialog_writeExerciseMemo.getContext().getResources().getDisplayMetrics();
            // int widthDp = Math.round(400 * dm.density);
            int heightDp = Math.round(350 * dm.density);
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = heightDp;
            alertDialog_writeExerciseMemo.getWindow().setAttributes(params);

            //메모 리스트 adapter setting
            exerciseMemoListItemAdapter = new ExerciseMemoListItemAdapter(exercise.getExId(), writeExerciseMemoRecyclerView, writeExerciseMemoPreArrow, writeExerciseMemoNextArrow, exerciseCardSetAddBt);
            writeExerciseMemoRecyclerView.setAdapter(exerciseMemoListItemAdapter);

            //UI 갱신 (라이브데이터 Observer 이용, 해당 디비값이 변화가생기면 실행됨)
            LiveData<List<ExerciseMemo>> listLiveDataExerciseMemo = appDatabase.exerciseMemoDao().getExerciseMemoList(exercise.getExId());
            LinkedList<ExerciseMemo> linkedListExerciseMemo = new LinkedList<>();

            alertDialog_writeExerciseMemo.getWindow().
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            listLiveDataExerciseMemo.observe(lifecycleOwner, new Observer<List<ExerciseMemo>>() {
                @Override
                public void onChanged(List<ExerciseMemo> exerciseMemoList) {
                    String getTodayStr = getToday.getToday();
                    if (exerciseMemoList.size() == 0 || !exerciseMemoList.get(0).getErimWriteTime().equals(getTodayStr)) {
                        hasTodayMemo = false;
                        ExerciseMemo exerciseMemo = new ExerciseMemo(exercise.getExId(), null, null, getTodayStr);
                        exerciseMemoList.add(0, exerciseMemo);
                    } else if (exerciseMemoList.get(0).getErimTitle().equals(getTodayStr) && exerciseMemoList.get(0).getErimTitle() != null) {
                        hasTodayMemo = true;
                    }

                    linkedListExerciseMemo.clear();
                    linkedListExerciseMemo.addAll(exerciseMemoList);
                    exerciseMemoListItemAdapter.setItem(linkedListExerciseMemo);

                    if (linkedListExerciseMemo.size() == 1) {
                        writeExerciseMemoPreArrow.setVisibility(View.INVISIBLE);
                        writeExerciseMemoNextArrow.setVisibility(View.INVISIBLE);
                    }
                }
            });

            //이전, 다음 버튼 - 이전
            writeExerciseMemoPreArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveToExerciseMemoList(writeExerciseMemoRecyclerView, writeExerciseMemoPreArrow, writeExerciseMemoNextArrow, "pre");
                }
            });
            //이전, 다음 버튼 - 다음
            writeExerciseMemoNextArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveToExerciseMemoList(writeExerciseMemoRecyclerView, writeExerciseMemoPreArrow, writeExerciseMemoNextArrow, "next");
                }
            });
        }

        //운동 설정 다이어로그 ( 휴식 시간, 무게 단위)
        private void showEditExerciseAlertDialog(Context context, Exercise exercise) {
            int restTime = exercise.getExRestTime();
            String weightUnit = exercise.getExWeightUnit();
            AlertDialog.Builder editExerciseAlert_builder = new AlertDialog.Builder(context);
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View editExerciseView = layoutInflater.inflate(R.layout.edit_exercise_dialog, null);
            EditText editExerciseRestTimeMinute = editExerciseView.findViewById(R.id.edit_rest_time_minute);
            EditText editExerciseRestTimeSecond = editExerciseView.findViewById(R.id.edit_rest_time_second);
            Button editExerciseWeightUnitKgBt = editExerciseView.findViewById(R.id.edit_exercise_weight_unit_kg_bt);
            Button editExerciseWeightUnitLbsBt = editExerciseView.findViewById(R.id.edit_exercise_weight_unit_lbs_bt);
            Button editExerciseRestTimeResetBt = editExerciseView.findViewById(R.id.edit_exercise_rest_time_reset_bt);
            Button editExerciseCompleteBt = editExerciseView.findViewById(R.id.edit_exercise_complete_bt);
            editExerciseAlert_builder.setView(editExerciseView);
            AlertDialog alertDialog_editExercise = editExerciseAlert_builder.create();
            alertDialog_editExercise.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog_editExercise.setCancelable(false);
            alertDialog_editExercise.show();


            //쉬는 시간이 설정되어 있지 않다면, 0초이므로 editText는 빈 값이다. 쉬는 시간이 설정되었다면, 0초가 아니므로 editText는 해당 값으로 set
            if (restTime > 0) {
                long minute = restTime / 60000;
                long second = (restTime % 60000) / 1000;
                editExerciseRestTimeMinute.setText(String.valueOf(minute));
                editExerciseRestTimeSecond.setText(String.valueOf(second));
            }
            //초기 kg/lbs 버튼 set
            if (weightUnit.equals("kg")) {
                editExerciseWeightUnitKgBt.setBackgroundColor(Color.GREEN);
                editExerciseWeightUnitLbsBt.setBackgroundColor(Color.DKGRAY);
            } else if (weightUnit.equals("lbs")) {
                editExerciseWeightUnitLbsBt.setBackgroundColor(Color.GREEN);
                editExerciseWeightUnitKgBt.setBackgroundColor(Color.DKGRAY);
            }
            //운동 타입이 무게가 아니면 버튼 비활성화
            if (!exercise.getExType().contains("무게/")) {
                editExerciseWeightUnitKgBt.setBackgroundColor(Color.DKGRAY);
                editExerciseWeightUnitLbsBt.setBackgroundColor(Color.DKGRAY);
                editExerciseWeightUnitKgBt.setEnabled(false);
                editExerciseWeightUnitLbsBt.setEnabled(false);
            }
            //운동 설정 버튼 kg 클릭 시,
            editExerciseWeightUnitKgBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editExerciseWeightUnitKgBt.setBackgroundColor(Color.GREEN);
                    editExerciseWeightUnitLbsBt.setBackgroundColor(Color.DKGRAY);
                    exercise.setExWeightUnit("kg");
                }
            });
            //운동 설정 버튼 lbs 클릭 시,
            editExerciseWeightUnitLbsBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editExerciseWeightUnitKgBt.setBackgroundColor(Color.DKGRAY);
                    editExerciseWeightUnitLbsBt.setBackgroundColor(Color.GREEN);
                    exercise.setExWeightUnit("lbs");

                }
            });
            //운동 설정 버튼 클릭 시,
            editExerciseRestTimeResetBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editExerciseRestTimeMinute.setText("");
                    editExerciseRestTimeSecond.setText("");
                }
            });
            //운동 설정 완료 버튼 시,
            editExerciseCompleteBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //운동 무게단위 업데이트
                    //운동 무게 변경 : kg -> lbs : kg / 0.45
                    //운동 무게 변경 : lbs -> kg : lbs * 0.45kg
                    String minute = String.valueOf(editExerciseRestTimeMinute.getText());
                    String second = String.valueOf(editExerciseRestTimeSecond.getText());
                    String totalWeightString = String.valueOf(exerciseTotalWeight.getText());
                    Character charAt = totalWeightString.charAt(totalWeightString.length() - 1);
                    String oldWeightUnit = charAt == 'g' ? "kg" : "lbs";
                    String newWeightUnit = exercise.getExWeightUnit(); // 버튼 누를 때 마다 바뀌는 weightUnit

                    if (minute.isEmpty() && second.isEmpty()) {
                        //해당 운동의 쉬는 시간 초기화
                        exercise.setExRestTime(0);
                    } else {
                        //해당 운동의 쉬는 시간 업데이트
                        if (minute.equals("")) minute = "0";
                        exercise.setExRestTime(Integer.parseInt(minute) * 60000 + Integer.parseInt(second) * 1000);
                    }
                    AsyncTask.execute(() -> appDatabase.exerciseDao().updateEditExercise(
                            exercise.getExRestTime(), newWeightUnit, exercise.getExId()));
                    if (!newWeightUnit.equals(oldWeightUnit)) {
                        calExerciseRecordTable(newWeightUnit, tableLayout);
                    } else {
                        exerciseTotalWeight.setText("설정 운동량 " + totalWeightSum + newWeightUnit);
                        exerciseTotalMyWeight.setText("수행 운동량 " + totalMyWeightSum + newWeightUnit);
                    }
                    headerWeightTv.setText("무게(" + newWeightUnit + ")");
                    alertDialog_editExercise.hide();
                }
            });
        }

        private void calExerciseRecordTable(String newWeightUnit, TableLayout tableLayout) {
            int lastIndex = tableLayout.getChildCount();
            if (lastIndex > 1) {
                for (int i = 1; i < lastIndex; i++) {
                    TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
                    EditText weightEt = (EditText) tableRow.getChildAt(1);
                    Double weight = Double.valueOf(weightEt.getText().toString());
                    if (newWeightUnit.equals("kg")) weight /= 0.45;
                    else weight *= 0.45; //lbs
                    weightEt.setText(String.valueOf(Math.round(weight * 10) / 10.0));
                }
            }
            setTotal(tableLayout);
            setTotalMy(tableLayout);
        }
    }

    private void preload(Context context) {
    }//exerciseImage.getWidth()

    public void setCallbackListener(ClickCallbackListener clickCallbackListener) {
        this.clickCallbackListener = clickCallbackListener;
    }

    //운동 타입 시간 포함일 떄, 시간 설정 다이어로그
    private void showAddRowTimeDialog(Context context, EditText editText1) {

        AlertDialog.Builder addExerciseRowTimeAlert_builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addExerciseRowTimeView = layoutInflater.inflate(R.layout.add_exercise_type_time_dialog, null);
        EditText addRowTimeMinute = addExerciseRowTimeView.findViewById(R.id.add_row_time_minute);
        EditText addRowTimeSecond = addExerciseRowTimeView.findViewById(R.id.add_row_time_second);

        EditText editAddRowTimeMinute = addExerciseRowTimeView.findViewById(R.id.edit_add_row_time_minute_et);
        Button editAddRowTimeMinuteUpBt = addExerciseRowTimeView.findViewById(R.id.edit_add_row_time_minute_up_bt);
        Button editAddRowTimeMinuteDownBt = addExerciseRowTimeView.findViewById(R.id.edit_add_row_time_minute_down_bt);

        EditText editAddRowTimeSecond = addExerciseRowTimeView.findViewById(R.id.edit_add_row_time_second_et);
        Button editAddRowTimeSecondUpBt = addExerciseRowTimeView.findViewById(R.id.edit_add_row_time_second_up_bt);
        Button editAddRowTimeSecondDownBt = addExerciseRowTimeView.findViewById(R.id.edit_add_row_time_second_down_bt);

        Button addExerciseRowTimeOkBt = addExerciseRowTimeView.findViewById(R.id.ok_add_row);

        addExerciseRowTimeAlert_builder.setView(addExerciseRowTimeView);
        AlertDialog alertDialog_addExerciseRowTime = addExerciseRowTimeAlert_builder.create();
        alertDialog_addExerciseRowTime.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog_addExerciseRowTime.setCancelable(true);
        alertDialog_addExerciseRowTime.show();

        addRowTimeMinute.setText("0");
        addRowTimeSecond.setText("0");

        String editTextStr = editText1.getText().toString();
        if (!editTextStr.equals("0")) {
            String mStr = editTextStr.split(" ")[0];
            String sStr = editTextStr.split(" ")[1];
            mStr = mStr.substring(0, mStr.length() - 1);
            sStr = sStr.substring(0, sStr.length() - 1);
            if (!mStr.equals("")) addRowTimeMinute.setText(mStr);
            if (!sStr.equals("")) addRowTimeSecond.setText(sStr);
        }

        addRowTimeMinute.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        addRowTimeSecond.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editAddRowTimeMinute.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editAddRowTimeSecond.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String upDownMinute = editAddRowTimeMinute.getText().toString();
                String upDownSecond = editAddRowTimeSecond.getText().toString();
                switch (view.getId()) {
                    case R.id.edit_add_row_time_minute_up_bt:
                        if (!upDownMinute.equals("")) {
                            String minute = addRowTimeMinute.getText().toString();
                            addRowTimeMinute.setText(String.valueOf(Integer.parseInt(minute) + 1));
                        }
                        break;
                    case R.id.edit_add_row_time_minute_down_bt:
                        if (!upDownMinute.equals("")) {
                            int m;
                            String minute = addRowTimeMinute.getText().toString();
                            m = Integer.parseInt(minute) - 1;
                            if (m < 0) m = 0;
                            addRowTimeMinute.setText(String.valueOf(m));
                        }
                        break;
                    case R.id.edit_add_row_time_second_up_bt:
                        if (!upDownSecond.equals("")) {
                            int s;
                            String second = addRowTimeSecond.getText().toString();
                            s = Integer.parseInt(second) + 1;
                            if (s >= 60) {
                                addRowTimeSecond.setText(0);
                                addRowTimeMinute.setText(
                                        String.valueOf(Integer.parseInt(addRowTimeMinute.getText().toString()) + 1
                                        ));
                            }
                            addRowTimeSecond.setText(String.valueOf(s));
                        }
                        break;
                    case R.id.edit_add_row_time_second_down_bt:
                        if (!upDownSecond.equals("")) {
                            int s;
                            String second = addRowTimeSecond.getText().toString();
                            s = Integer.parseInt(second) - 1;
                            if (s < 0) s = 0;
                            addRowTimeSecond.setText(String.valueOf(s));
                        }
                        break;
                    case R.id.ok_add_row:
                        alertDialog_addExerciseRowTime.dismiss();
                        break;

                }
            }
        };
        editAddRowTimeMinuteUpBt.setOnClickListener(onClickListener);
        editAddRowTimeMinuteDownBt.setOnClickListener(onClickListener);
        editAddRowTimeSecondUpBt.setOnClickListener(onClickListener);
        editAddRowTimeSecondDownBt.setOnClickListener(onClickListener);
        addExerciseRowTimeOkBt.setOnClickListener(onClickListener);
        alertDialog_addExerciseRowTime.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                String str = addRowTimeMinute.getText().toString() + "분 " + addRowTimeSecond.getText().toString() + "초";
                editText1.setText(str);
            }
        });

    }

    private void setTableHeader(String exType1, String exType2, String weightUnit, TableRow tableHeader) {
        ((TextView) tableHeader.getChildAt(1)).setText(exType1);
        if (exType1.equals("무게"))
            ((TextView) tableHeader.getChildAt(1)).setText(exType1 + "(" + weightUnit + ")");
        if (exType2 == null || exType2.equals("")) {
            tableHeader.getChildAt(2).setVisibility(View.GONE);
            ((TextView) tableHeader.getChildAt(2)).setText("");
        } else {
            if(exType2.equals("거리")){
                ((TextView) tableHeader.getChildAt(2)).setText(exType2+"(m)");
            }else{
                ((TextView) tableHeader.getChildAt(2)).setText(exType2);
            }


        }
    }

    //LifecycleOwner setting
    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    //메모장 이전,다음 세팅
    private void moveToExerciseMemoList(RecyclerView recyclerView, ImageButton pre, ImageButton nxt, String mode) {
        int currentPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int memoListSize = exerciseMemoListItemAdapter.getItemCount();
        pre.setVisibility(View.VISIBLE);
        nxt.setVisibility(View.VISIBLE);

        if (memoListSize == 1) {
            pre.setVisibility(View.INVISIBLE);
            nxt.setVisibility(View.INVISIBLE);
        }
        //pre
        if (mode.equals("pre")) {
            int prePosition = currentPosition + 1;
            if (currentPosition < memoListSize) {
                recyclerView.smoothScrollToPosition(prePosition);
                if (prePosition == memoListSize - 1) {
                    pre.setVisibility(View.INVISIBLE);
                }
            }
        }
        //nxt
        else if (mode.equals("next")) {
            int nextPosition = currentPosition - 1;
            if (currentPosition > 0) {
                recyclerView.smoothScrollToPosition(nextPosition);
                if (nextPosition == 0) {
                    nxt.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @NonNull
    private ExerciseTableInfo getExercisdCardSetInfo(TableLayout tableLayout,
                                                     ExerciseTableInfo exerciseTableInfo) {
        ViewGroup viewGroup = (ViewGroup) tableLayout.getParent();
        ArrayList<String> header = exerciseTableInfo.getExerciseTableHeader();
        ArrayList<String> body = exerciseTableInfo.getExerciseTableBody();
        if (!body.isEmpty()) body.clear();
        String bodyStr = "";

        for (int i = 0; i < tableLayout.getChildCount(); i++) { //테이블 탐색
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            if (i == 0) { //header : 복근 | 3/4 싯업/세트/횟수/완료
                if (header.isEmpty()) {
                    for (int j = 0; j < tableRow.getChildCount(); j++) {
                        TextView textView = (TextView) tableRow.getChildAt(j);
                        String str = textView.getText().toString();
                        if (!str.equals("")) header.add(str);
                    }
                }
            } else { //body

                TextView textView = (TextView) tableRow.getChildAt(0);

                if (textView.getText().toString().equals("운동을 해주세요 :/")) {
                    bodyStr = "운동을 해주세요 :/";
                } else {
                    EditText editText1 = (EditText) tableRow.getChildAt(1);

                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(tableRow.getChildCount() - 1);
                    CheckBox checkBox = (CheckBox) (linearLayout.getChildAt(0));

                    EditText editText2 = null;
                    if (tableRow.getChildCount() == 4) {
                        editText2 = (EditText) tableRow.getChildAt(2);
                    }
                    String set = textView.getText().toString();
                    String val1 = editText1.getText().toString();
                    String val2 = "";
                    String check = checkBox.isChecked() ? "true" : "false";
                    if (editText2 != null) {
                        val2 = editText2.getText().toString();
                        bodyStr = set + "/" + val1 + "/" + val2 + "/" + check;
                    } else {
                        bodyStr = set + "/" + val1 + "/" + check;
                    }
                }
                body.add(bodyStr.trim());
            }
        }
        exerciseTableInfo.setExerciseTableHeader(header);
        exerciseTableInfo.setExerciseTableBody(body);
        return exerciseTableInfo;
    }

    @NonNull
    private ArrayList<RoutineInfoListItemDetailed> getRoutineDetails(TableLayout tableLayout,
                                                                     ArrayList<RoutineInfoListItemDetailed> routineDetails) {
        ViewGroup viewGroup = (ViewGroup) tableLayout.getParent();
        if (!routineDetails.isEmpty()) routineDetails.clear();

        for (int i = 1; i < tableLayout.getChildCount(); i++) { //테이블 탐색
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            RoutineInfoListItemDetailed r = new RoutineInfoListItemDetailed();
            TextView textView = (TextView) tableRow.getChildAt(0);
            r.setSet(Integer.parseInt(textView.getText().toString())); //세트
            EditText editText1 = (EditText) tableRow.getChildAt(1);
            r.setVal1(editText1.getText().toString());
            LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(tableRow.getChildCount() - 1);
            CheckBox checkBox = (CheckBox) (linearLayout.getChildAt(0));
            r.setCheck(checkBox.isChecked());
            EditText editText2 = null;
            if (tableRow.getChildCount() == 4) {
                editText2 = (EditText) tableRow.getChildAt(2);
                r.setVal2(editText2.getText().toString());
            }
            routineDetails.add(r);
        }
        return routineDetails;
    }




}
