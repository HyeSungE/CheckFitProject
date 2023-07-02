package org.techtown.MyExerciseApp.Adapter.Exercise;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.MyExerciseApp.Data.Exercise.ExerciseTableInfo;
import org.techtown.MyExerciseApp.R;

import java.util.ArrayList;

public class CompleteExerciseListAdapter extends RecyclerView.Adapter<CompleteExerciseListAdapter.ViewHolder> {

    private ArrayList<ExerciseTableInfo> list = new ArrayList<>();
    private LifecycleOwner lifecycleOwner;
    private Context mContext;
    private final ArrayList<RelativeLayout> relativeLayoutArrayList = new ArrayList<>();
    private final ArrayList<RelativeLayout> headerArrayList = new ArrayList<>();
    private final ArrayList<TableLayout> tableLayoutArrayList = new ArrayList<>();
    private final ArrayList<TextView> textViewArrayList = new ArrayList<>();

    @Override
    public int getItemCount() {
        return list.size();
    }

    public ArrayList<ExerciseTableInfo> getList() {
        return list;
    }

    public void setItem(ArrayList<ExerciseTableInfo> data) {
        list = data;
    }

    //LifecycleOwner setting
    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public CompleteExerciseListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.complete_exercise_list_item, viewGroup, false);
        this.mContext = viewGroup.getContext();
        return new CompleteExerciseListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompleteExerciseListAdapter.ViewHolder viewHolder, int position) {
        viewHolder.onBind(list.get(position), position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView exerciseName;
        private final TextView exerciseWeight;
        private final TextView exerciseVal1;
        private final TextView exerciseVal2;
        private final TextView exerciseTotalSet;
        private final TableRow noExerciseMessage;
        private final RelativeLayout relativeLayout;
        private final RelativeLayout headerRelativeLayout;
        private final TableLayout tableLayout;

        private int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.comp_exercise_name);
            exerciseWeight = itemView.findViewById(R.id.comp_exercise_weight);
            exerciseVal1 = itemView.findViewById(R.id.comp_exercise_list_header_row_val1);
            exerciseVal2 = itemView.findViewById(R.id.comp_exercise_list_header_row_val2);
            relativeLayout = itemView.findViewById(R.id.comp_exercise_rel);
            headerRelativeLayout = itemView.findViewById(R.id.comp_exercise_header);
            tableLayout = itemView.findViewById(R.id.comp_table_layout);
            exerciseTotalSet = itemView.findViewById(R.id.comp_exercise_total_set);
            noExerciseMessage = itemView.findViewById(R.id.no_exercise_record_row);

        }

        public void onBind(ExerciseTableInfo exerciseTableInfo, int position) {
            exerciseTotalSet.setVisibility(View.INVISIBLE);
            this.position = position;
            //가슴 | 어시스트 딥스/세트/무게/횟수/완료@1/10/10/true@2/10/20/true@3/10/30/true
            //유산소 | 달리기/세트/시간/횟수/완료@1/01분 20초//true@2/01분 20초//true@3/01분 20초//true;
            String exerciseNameStr = exerciseTableInfo.getExerciseName();
            ArrayList<String> header = exerciseTableInfo.getExerciseTableHeader();
            ArrayList<String> body = exerciseTableInfo.getExerciseTableBody();

            int totalSet = 0;
            for (int i = 0; i < body.size(); i++) {
                String str = body.get(i);
                int strlen = str.split("/").length;
                if (str.split("/")[strlen - 1].equals("true")) {
                    totalSet += 1;
                }
            }
            exerciseTotalSet.setText(totalSet + " SET ");
            //복근 | 3/4 싯업/세트/횟수/완료@
            exerciseName.setText(exerciseNameStr); //운동이름

            String exTypeStr = null; // 운동타입
            if (header.size() == 3) exTypeStr = header.get(1) + "/";
            else exTypeStr = header.get(1) + "/" + header.get(2);

            String weightUnit = ""; // 운동 무게 단위
            if (header.get(1).contains("kg")) {
                weightUnit = "kg";
            } else if (header.get(1).contains("lbs")) {
                weightUnit = "lbs";
            }
            if (exTypeStr.equals("무게(kg)/횟수") || exTypeStr.equals("무게(lbs)/횟수")) { //운동 수행 무게
                double totalMyWeight = 0.0;
                for (int i = 0; i < body.size(); i++) {
                    String row = body.get(i);
                    if (row.split("/")[3].equals("true")) {
                        totalMyWeight +=
                                Double.parseDouble(row.split("/")[1]) * Integer.parseInt(row.split("/")[2]);
                    }
                }
                exerciseWeight.setText("수행 운동량 " + totalMyWeight + weightUnit);
                exerciseVal1.setText(header.get(1));
            } else if (exTypeStr.equals("횟수/")) {
                int totalReps = 0;
                for (int i = 0; i < body.size(); i++) {
                    String row = body.get(i);
                    if (row.split("/")[2].equals("true")) {
                        totalReps += Integer.parseInt(row.split("/")[1]);
                    }
                }
                exerciseWeight.setText("수행 횟수 " + totalReps + "회");
                exerciseVal1.setText("횟수");
                exerciseVal2.setVisibility(View.GONE);
                ((TableRow)exerciseVal2.getParent()).removeView(exerciseVal2);

            } else if (exTypeStr.equals("시간/")) {
                int minutes = 0;
                int seconds = 0;
                for (int i = 0; i < body.size(); i++) {
                    String row = body.get(i);
                    if (row.split("/")[2].equals("true")) {
                        String time = row.split("/")[1];
                        if(time.equals("0")) {
                            time = "0분 0초";
                        }
                        String mStr = time.split(" ")[0];
                        String sStr = time.split(" ")[1];
                        int mVal = Integer.parseInt(mStr.substring(0, mStr.length() - 1));
                        int sVal = Integer.parseInt(sStr.substring(0, sStr.length() - 1));
                        minutes += mVal;
                        seconds += sVal;
                    }
                }
                minutes = minutes + seconds / 60;
                seconds = seconds % 60;
                exerciseWeight.setText("수행 시간 " + minutes + "분" + seconds + "초");
                exerciseVal1.setText("시간");
                exerciseVal2.setVisibility(View.GONE);
                ((TableRow)exerciseVal2.getParent()).removeView(exerciseVal2);


            } else if (exTypeStr.equals("무게/")) {
                double totalMyWeight = 0.0;//운동 수행 무게
                for (int i = 0; i < body.size(); i++) {
                    String row = body.get(i);
                    if (row.split("/")[2].equals("true")) {
                        totalMyWeight +=
                                Double.parseDouble(row.split("/")[1]);
                    }
                }
                exerciseWeight.setText("수행 무게 " + totalMyWeight + weightUnit);
                exerciseVal1.setText("무게");
                exerciseVal2.setVisibility(View.GONE);
                ((TableRow)exerciseVal2.getParent()).removeView(exerciseVal2);

            } else {
                exerciseVal1.setText(header.get(1));
                exerciseVal2.setText(header.get(2));
                exerciseWeight.setText("");
                exerciseWeight.setVisibility(View.INVISIBLE);
            }
            headerArrayList.add(headerRelativeLayout);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT
            );
            TableRow.LayoutParams paramsTr = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.MATCH_PARENT,
                    1f
            );

            //운동 기록 로우 추가
            if (!body.isEmpty()) {
                int len = body.get(0).split("/").length;
                if (len > 0) {
                    for (int i = 0; i < body.size(); i++) {
                        TableRow tableRow = new TableRow(mContext);
                        tableRow.setLayoutParams(params);
                        String recordRow = body.get(i);
                        for (int j = 0; j < len; j++) {
                            TextView textView = new TextView(mContext);
                            textView.setLayoutParams(paramsTr);
                            textView.setGravity(Gravity.CENTER);
                            if (j == 0) {
                                textView.setText(recordRow.split("/")[j]);
                            } else if (j == len - 1) {
                                if (recordRow.split("/")[j].equals("true")) {
                                    textView.setText("완료");
                                } else {
                                    textView.setText("미완료");
                                }
                            } else {
                                textView.setText(recordRow.split("/")[j]);
                                //if 무게면 단위 붙이기
                            }
                            tableRow.addView(textView);
                        }
                        tableLayout.addView(tableRow);
                        tableLayoutArrayList.add(tableLayout);
                        textViewArrayList.add(exerciseTotalSet);
                    }

                }
            } else {
                noExerciseMessage.setVisibility(View.VISIBLE);
                tableLayoutArrayList.add(tableLayout);
                textViewArrayList.add(exerciseTotalSet);
            }
            relativeLayoutArrayList.add(relativeLayout);
        }


    }

    public ArrayList<RelativeLayout> getRelativeLayoutArrayList() {
        return relativeLayoutArrayList;
    }

    public ArrayList<RelativeLayout> getHeaderArrayList() {
        return headerArrayList;
    }

    public ArrayList<TableLayout> getTableLayoutArrayList() {
        return tableLayoutArrayList;
    }

    public void changeSummaryMode(String mode) {
        if (mode.equals("normal")) {
            for (TableLayout tableLayout : tableLayoutArrayList) {
                tableLayout.setVisibility(View.VISIBLE);
            }
            for (TextView textView : textViewArrayList) {
                textView.setVisibility(View.INVISIBLE);
            }

        } else if (mode.equals("summary")) {

            for (TableLayout tableLayout : tableLayoutArrayList) {
                tableLayout.setVisibility(View.GONE);
            }
            for (TextView textView : textViewArrayList) {
                textView.setVisibility(View.VISIBLE);
            }
        }
    }
}


