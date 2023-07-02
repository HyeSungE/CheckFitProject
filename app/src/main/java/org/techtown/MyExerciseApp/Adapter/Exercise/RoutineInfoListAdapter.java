package org.techtown.MyExerciseApp.Adapter.Exercise;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Entity.Exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class RoutineInfoListAdapter extends RecyclerView.Adapter<RoutineInfoListAdapter.ViewHolder> {

    private List<RoutineInfoListItem> items;
    private ArrayList<RoutineInfoListItemDetailed> items_childArr;

    private ArrayList<ArrayList<RoutineInfoListItemDetailed>> childDetailedArrayList;
    ArrayList<HashMap<Integer,RoutineInfoListItem>> arr;
    private HashMap<Integer,RoutineInfoListItem> riHashMap = new LinkedHashMap<>();
    private HashMap<Integer,RoutineInfoListItemDetailed> ridHashMap = new LinkedHashMap<>();
    private HashMap<Integer,Exercise> exHashMap = new LinkedHashMap<>();
    private ArrayList<TableLayout> tableLayouts = new ArrayList<TableLayout>();

    private String mode;

    private Context mContext;

    public void setItem(List<RoutineInfoListItem> routineInfoListItemList) {
        this.items = routineInfoListItemList;
        if(riHashMap.size() > 0) ridHashMap.clear();
        for(RoutineInfoListItem routineInfoListItem : items){
            Exercise exercise = routineInfoListItem.getExercise();
            ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList = routineInfoListItem.getRoutineInfoListItemDetailedArrayList();
            if(itemDetailedArrayList == null || itemDetailedArrayList.isEmpty()){
                routineInfoListItem.setRoutineInfoListItemDetailedArrayList(new ArrayList<>());
            }
            exHashMap.put(exercise.getExId(),exercise);
            riHashMap.put(exercise.getExId(),routineInfoListItem);
        }
        notifyDataSetChanged();
    }
    public List<RoutineInfoListItem> getItem() {
        return this.items;
    }
    public HashMap<Integer,RoutineInfoListItem> getRiHashMap() {return this.riHashMap;}

    public void addItem(RoutineInfoListItem routineInfoListItem, int position){
        items.add(routineInfoListItem);

        Exercise exercise = routineInfoListItem.getExercise();
        ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList = routineInfoListItem.getRoutineInfoListItemDetailedArrayList();
        if(itemDetailedArrayList == null || itemDetailedArrayList.isEmpty()){
            routineInfoListItem.setRoutineInfoListItemDetailedArrayList(new ArrayList<>());
        }
        exHashMap.put(exercise.getExId(),exercise);
        riHashMap.put(exercise.getExId(),routineInfoListItem);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public RoutineInfoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routine_info_list_item_header, viewGroup, false);
        mContext = viewGroup.getContext();

        return new RoutineInfoListAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RoutineInfoListAdapter.ViewHolder viewHolder, int position) {
        viewHolder.onBind(items.get(position), position);
    }

    @Override
    public int getItemCount() {return items.size();}

    public void setMode(String mode) {
        this.mode = mode;
    }

    public interface RoutineInfoListClickListener {
        void setSaveBtVisible(boolean b);
    }

    private RoutineInfoListAdapter.RoutineInfoListClickListener mListener = null;

    public void setRoutineInfoListClickListener(RoutineInfoListAdapter.RoutineInfoListClickListener listener) {
        this.mListener = listener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView routineInfoExerciseImage;
        private TextView routineInfoExerciseName;
        private ImageButton routineExplainIcon;
        private TableLayout routineExerciseTable;
        private Button routineExerciseDeleteSetBt, routineExerciseAddSetBt, routineExerciseDeleteBt;
        private Boolean oneType = true;
        private TextView editTv2 = null;
        private Boolean expanded = false;
        private LinearLayout routineExerciseButtonPocket;

        public ViewHolder(@NonNull View view) {
            super(view);
            routineInfoExerciseImage = view.findViewById(R.id.routine_info_exercise_image);
            routineInfoExerciseName = view.findViewById(R.id.routine_info_exercise_name);
            routineExplainIcon = (ImageButton) view.findViewById(R.id.routine_explain_icon);
            routineExerciseTable = view.findViewById(R.id.routine_exercise_table);
            routineExerciseDeleteSetBt = view.findViewById(R.id.routine_exercise_delete_set_bt);
            routineExerciseAddSetBt = view.findViewById(R.id.routine_exercise_add_set_bt);
            routineExerciseDeleteBt = view.findViewById(R.id.routine_exercise_delete_bt);
            routineExerciseButtonPocket = view.findViewById(R.id.routine_exercise_button_pocket);
            if(mode!=null){
                if(mode.equals("Post") || mode.equals("GroupRoutineView")){
                    routineExerciseDeleteSetBt.setVisibility(View.GONE);
                    routineExerciseAddSetBt.setVisibility(View.GONE);
                    routineExerciseDeleteBt.setVisibility(View.GONE);
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        public void onBind(RoutineInfoListItem routineInfoListItem, int position) {

           TableRow.LayoutParams tableRowLP = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            );
            TableRow.LayoutParams tableRowContentLP = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1f); tableRowContentLP.gravity = Gravity.CENTER;


            Exercise exercise = routineInfoListItem.getExercise();
            Glide.with(itemView.getRootView().getContext()).asBitmap()
                    .load(exercise.getExImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(routineInfoExerciseImage);
            routineInfoExerciseName.setText(exercise.getExBodypart()+" | "+exercise.getExName());
            //테이블 헤더
            TableRow header = new TableRow(mContext);

            TextView setTv = new TextView(mContext); setTv.setText("세트"); setTv.setLayoutParams(tableRowContentLP); header.addView(setTv); setTv.setGravity(Gravity.CENTER);

            String exType = exercise.getExType(); if(exType.length() > 3) oneType = false;
            TextView editTv1 = new TextView(mContext); editTv1.setLayoutParams(tableRowContentLP); editTv1.setText(exType.split("/")[0]); editTv1 .setGravity(Gravity.CENTER);
            if(exType.contains("무게")) {
                editTv1.setText(exType.split("/")[0] + "("+exercise.getExWeightUnit()+")");
            }
            header.addView(editTv1);
            if(!oneType) {
                editTv2 = new TextView(mContext); editTv2.setText(exType.split("/")[1]);
                header.addView(editTv2);
                editTv2.setLayoutParams(tableRowContentLP);
                editTv2.setGravity(Gravity.CENTER);
            }
            routineExerciseTable.addView(header);
            header.setLayoutParams(tableRowLP);

            //테이블 바디
            ArrayList<RoutineInfoListItemDetailed> arrayList = routineInfoListItem.getRoutineInfoListItemDetailedArrayList();
            TextWatcher textWatcher = new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ArrayList<RoutineInfoListItemDetailed> arr = getRoutineDetails(routineExerciseTable, arrayList);
                    RoutineInfoListItem ri = (RoutineInfoListItem) riHashMap.get(exercise.getExId());
                    ri.setRoutineInfoListItemDetailedArrayList(arr);
                    riHashMap.put(exercise.getExId(), ri);


                }

                public void afterTextChanged(Editable s) {
                    ArrayList<RoutineInfoListItemDetailed> arr = getRoutineDetails(routineExerciseTable, arrayList);
                    RoutineInfoListItem ri = (RoutineInfoListItem) riHashMap.get(exercise.getExId());
                    ri.setRoutineInfoListItemDetailedArrayList(arr);
                    riHashMap.put(exercise.getExId(), ri);


                }
            };
            for(RoutineInfoListItemDetailed ri : arrayList) {
                TableRow tableRow = new TableRow(mContext);
                tableRow.setLayoutParams(tableRowLP);
                TextView set = new TextView(mContext);
                set.setLayoutParams(tableRowContentLP);
                set.setText(String.valueOf(ri.getSet()));  tableRow.addView(set); set.setGravity(Gravity.CENTER);
                EditText ed1 = new EditText(mContext); ed1.setBackground(null);
                ed1.setLayoutParams(tableRowContentLP); ed1.setGravity(Gravity.CENTER);
                ed1.setText(String.valueOf(ri.getVal1())); tableRow.addView(ed1);
                setEditTextClickListener(ed1,editTv1);

                if(!oneType){
                    EditText ed2 = new EditText(mContext);
                    ed2.setLayoutParams(tableRowContentLP); ed2.setGravity(Gravity.CENTER);
                    setEditTextClickListener(ed2,editTv2); ed2.setBackground(null);
                    ed2.setText(String.valueOf(ri.getVal2())); tableRow.addView(ed2);
                }
                routineExerciseTable.addView(tableRow);
            }


            int routineExerciseTableSize = routineExerciseTable.getChildCount();
            System.out.println("routineExerciseTableSize ++ " + routineExerciseTableSize);
            int routineExerciseTableRowSize = ((TableRow)routineExerciseTable.getChildAt(0)).getChildCount();
            for(int i = 1; i < routineExerciseTableSize; i++){
                TableRow t = ((TableRow)routineExerciseTable.getChildAt(i));
                ((EditText)t.getChildAt(1)).addTextChangedListener(textWatcher);
                ((EditText)t.getChildAt(1)).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (mListener != null) {
                                mListener.setSaveBtVisible(true);
                            }
                        }
                        return false;
                    }
                });
                if(routineExerciseTableRowSize==3) {
                    ((EditText)t.getChildAt(2)).addTextChangedListener(textWatcher);
                    ((EditText)t.getChildAt(2)).setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                if (mListener != null) {
                                    mListener.setSaveBtVisible(true);
                                }
                            }
                            return false;
                        }
                    });
                }
            }

            routineExplainIcon.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                 //이미지를 화살표로 바꾸고 테이블과 버튼을 visible
                   if(!expanded) { //버튼을 눌렀을 때 확장 상태가 아니라면( 초기값 false )
                       routineExplainIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                       routineExerciseTable.setVisibility(View.VISIBLE);
                       routineExerciseButtonPocket.setVisibility(View.VISIBLE);
                       expanded = true;
                   }else{
                       routineExplainIcon.setImageResource(R.drawable.baseline_keyboard_arrow_down_24);
                       routineExerciseTable.setVisibility(View.GONE);
                       routineExerciseButtonPocket.setVisibility(View.GONE);
                       expanded = false;
                   }

               }
            });
            routineExerciseAddSetBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int childCount = routineExerciseTable.getChildCount();
                    TableRow tableRow = new TableRow(mContext);
                    tableRow.setLayoutParams(tableRowLP);
                    TextView set = new TextView(mContext);
                    set.setLayoutParams(tableRowContentLP); set.setGravity(Gravity.CENTER);
                    EditText ed1 = new EditText(mContext); ed1.setBackground(null); ed1.addTextChangedListener(textWatcher);
                    ed1.setLayoutParams(tableRowContentLP); ed1.setGravity(Gravity.CENTER);
                    if(childCount > 1) {
                        TableRow lastRow = (TableRow) routineExerciseTable.getChildAt(
                                childCount-1);
                        String setString = ((TextView)lastRow.getChildAt(0)).getText().toString();
                        int setInt = Integer.parseInt(setString)+1;
                        set.setText(String.valueOf(setInt));  tableRow.addView(set);
                        ed1.setText(
                                ((EditText)lastRow.getChildAt(1)).getText()
                        ); tableRow.addView(ed1); setEditTextClickListener(ed1,editTv1);
                        if(!oneType){
                            EditText ed2 = new EditText(mContext); ed2.setBackground(null);
                            ed2.setLayoutParams(tableRowContentLP); ed2.setGravity(Gravity.CENTER); ed2.addTextChangedListener(textWatcher);
                            ed2.setText(((EditText)lastRow.getChildAt(2)).getText()); tableRow.addView(ed2); setEditTextClickListener(ed2,editTv2);
                        }

                    }else{
                        set.setText("1");  tableRow.addView(set);
                        ed1.setText("0"); tableRow.addView(ed1);
                        setEditTextClickListener(ed1,editTv1);
                        if(editTv1.getText().toString().contains("시간")){
                            ed1.setText("0분 0초");
                        }
                        if(!oneType){
                            EditText ed2 = new EditText(mContext); ed2.setBackground(null); ed2.addTextChangedListener(textWatcher);
                            ed2.setLayoutParams(tableRowContentLP); ed2.setGravity(Gravity.CENTER);
                            setEditTextClickListener(ed2,editTv2);
                            ed2.setText("0"); tableRow.addView(ed2);
                            if(editTv2.getText().toString().contains("시간")){
                                ed2.setText("0분 0초");
                            }
                        }
                    }
                    routineExerciseTable.addView(tableRow);

                    ArrayList<RoutineInfoListItemDetailed> arr = getRoutineDetails(routineExerciseTable, arrayList);
                    RoutineInfoListItem ri = (RoutineInfoListItem) riHashMap.get(exercise.getExId());
                    ri.setRoutineInfoListItemDetailedArrayList(arr);
                    riHashMap.put(exercise.getExId(), ri);


                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.setSaveBtVisible(true);
                        }
                    }
                }
            });
            routineExerciseDeleteSetBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int lastIndex = routineExerciseTable.getChildCount();
                    if (lastIndex > 2) {
                        TableRow lastRow = (TableRow) routineExerciseTable.getChildAt(lastIndex - 1);
                        routineExerciseTable.removeView(lastRow);
                        ArrayList<RoutineInfoListItemDetailed> arr = getRoutineDetails(routineExerciseTable, arrayList);
                        RoutineInfoListItem ri = (RoutineInfoListItem) riHashMap.get(exercise.getExId());
                        ri.setRoutineInfoListItemDetailedArrayList(arr);
                        riHashMap.put(exercise.getExId(), ri);
                    }
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.setSaveBtVisible(true);
                        }
                    }
                }
            });
            routineExerciseDeleteBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //운동삭제
                    cleanSet(routineExerciseTable);
                    if (items.size() > 0) {
                        items.remove(getAdapterPosition());
                        riHashMap.remove(exercise.getExId());
                        notifyItemRemoved(getAdapterPosition());
                    }
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.setSaveBtVisible(true);
                        }
                    }
                }
            });
        }
    }
    //세트 초기화
    private void cleanSet(TableLayout tableLayout) {
        int lastIndex = tableLayout.getChildCount();
        if (lastIndex > 1) {tableLayout.removeViews(1, lastIndex - 1);}
    }
    private void setEditTextClickListener(EditText editText,TextView textView) {
        String tv =  textView.getText().toString();
        switch (tv) {
            case "시간" :
                editText.setFocusable(false);
                editText.setCursorVisible(false);
                editText.setKeyListener(null);
                editText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAddRowTimeDialog(mContext, editText);
                    }
                });
                break;

            case "무게(kg)" :
            case "무게(lbs)":
                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                break;

            case "횟수" :
                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                break;
        }

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

    @NonNull
    private ArrayList<RoutineInfoListItemDetailed> getRoutineDetails(TableLayout tableLayout,
                                                                     ArrayList<RoutineInfoListItemDetailed> routineDetails) {
        ViewGroup viewGroup = (ViewGroup) tableLayout.getParent();
        if(!routineDetails.isEmpty()) routineDetails.clear();

        for (int i = 1; i < tableLayout.getChildCount(); i++) { //테이블 탐색
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            RoutineInfoListItemDetailed r = new RoutineInfoListItemDetailed();
            TextView textView = (TextView) tableRow.getChildAt(0);
            r.setSet(Integer.parseInt(textView.getText().toString())); //세트
            EditText editText1 = (EditText) tableRow.getChildAt(1);
            r.setVal1(editText1.getText().toString());
            EditText editText2 = null;
            if (tableRow.getChildCount() ==3) {
                editText2 = (EditText) tableRow.getChildAt(2);
                r.setVal2(editText2.getText().toString());
            }
            r.setCheck(false);
            routineDetails.add(r);
        }
        return  routineDetails;
    }
}
