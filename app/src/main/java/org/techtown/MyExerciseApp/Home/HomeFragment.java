package org.techtown.MyExerciseApp.Home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.techtown.MyExerciseApp.Data.Exercise.ExerciseCompleteBundle;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseTableInfo;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Exercise.ExerciseCompleteShare;
import org.techtown.MyExerciseApp.Main.LoginActivity;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.techtown.MyExerciseApp.db.Entity.ExerciseRecord;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;


public class HomeFragment extends Fragment {
    private MainActivity mainActivity;
    private Fragment fragment_home_today_exercise;
    private FirebaseAuth auth; private DatabaseReference reference; FirebaseUser user;
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;
    private int num_page = 3;
    private CircleIndicator3 mIndicator;
    private FirebaseUser currentUser;
    private ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();

    private MaterialCalendarView calendarView;
    private List<ExerciseRecord> allExerciseRecord,weekendExerciseRecord;
    private Double[] exerciseTotalSumList;
    private Hashtable<CalendarDay,Integer> doExerciseDays;
    private SimpleDateFormat format;
    private HorizontalBarChart calendarCard_weekChart;
    //private String[] DaysArr = {"일","월","화","수","목","금","토"};
    private String[] DaysArr = {"토","금","목","수","화","월","일"};

    private String routineDialogName = "";
    private long insertResult;
    private AppDatabase appDatabase;
    private boolean isContainRecord;
    private String selectRound;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        if(calendarCard_weekChart!=null) calendarCard_weekChart.invalidate();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        appDatabase = AppDatabase.getDBInstance(requireActivity().getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();
        format = new SimpleDateFormat("yyyy-MM-dd");
        if(user == null) {
            showAlertSimpleMessage.show(requireActivity().getApplicationContext(), "오류", "로그인 후 이용해 주세요\n로그인으로 이동합니다!");
            Intent intent = new Intent(requireActivity().getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        calendarCard_weekChart= rootView.findViewById(R.id.calendarCard_weekChart);

        //달력
        calendarView = rootView.findViewById(R.id.calendarView);
        calendarView.addDecorator(new TodayDecorator(CalendarDay.today()));
        calendarView.setSelectedDate(CalendarDay.today());
        allExerciseRecord = new ArrayList<>(); doExerciseDays = new Hashtable<>();



        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                allExerciseRecord = appDatabase.exerciseRecordDao().getAllExerciseRecord();
                if(!allExerciseRecord.isEmpty()){
                    for(ExerciseRecord er : allExerciseRecord){
                        String erDate = er.getErDate().split(" ")[0];
                        CalendarDay calendarDay = CalendarDay.from(
                                Integer.parseInt(erDate.split("-")[0]),
                                Integer.parseInt(erDate.split("-")[1]),
                                Integer.parseInt(erDate.split("-")[2])
                        );
                        if(doExerciseDays.get(calendarDay)!=null) doExerciseDays.put(calendarDay,doExerciseDays.get(calendarDay)+1);
                        else doExerciseDays.put(calendarDay,1);
                    }
                    ExerciseDecorator[] decoratorArray = new ExerciseDecorator[3];
                    decoratorArray[0] = new ExerciseDecorator(Color.parseColor("#FF5722"),5f,0);
                    decoratorArray[1] = new ExerciseDecorator(Color.parseColor("#4CAF50"),5f,1);
                    decoratorArray[2] = new ExerciseDecorator(Color.parseColor("#FFEB3B"),5f,2);

                    /*dayInstanceMap contains all the mappings.*/
                    for(CalendarDay calendarDay: doExerciseDays.keySet()){
                        CalendarDay currDay = calendarDay;
                        int round = doExerciseDays.get(calendarDay); //If you have max amount of dots, check here if currDay is too large.
                        if(round >=3) round =3;
                        for(int i = 0; i<round; i++)
                            decoratorArray[i].addDate(currDay);
                    }
                    calendarView.addDecorators(decoratorArray);

                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                        @Override
                        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                            isContainRecord = false;
                            String selectedDate = date.getDate().format(dateTimeFormatter);
                            ArrayList<ExerciseRecord> erList = new ArrayList<>();
                            ArrayList<String> roundList = new ArrayList<>();
                            HashMap<String,ExerciseRecord> erHashMap = new HashMap<>();
                            if(!allExerciseRecord.isEmpty()){
                                for(ExerciseRecord exerciseRecord : allExerciseRecord){
                                    if(exerciseRecord.getErDate().split(" ")[0].equals(selectedDate)){
                                        isContainRecord = true;
                                        System.out.println("roundroudn ++ "+exerciseRecord.getErRound());
                                        roundList.add(String.valueOf(exerciseRecord.getErRound()));
                                        erHashMap.put(String.valueOf(exerciseRecord.getErRound()),exerciseRecord);
                                    }
                                }
                            }

                            if(!isContainRecord){
                                Toast.makeText(requireActivity().getApplicationContext(), selectedDate+" 운동 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                AlertDialog.Builder selectRoundDialog = new AlertDialog.Builder(requireActivity());
                                selectRoundDialog.setTitle("라운드 선택");
                                selectRoundDialog.setItems(roundList.toArray(new String[0]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        String round = roundList.get(i);
                                        System.out.println("roundroudn2 ++ "+round);
                                        System.out.println(" erHashMap.get(round).getErExerciseList() : "+ erHashMap.get(round).getErExerciseList());
                                        goToExerciseCompleteShare(erHashMap.get(round),round);
                                        //System.out.println("round++ "+roundList.get(i));
                                        //showDayExerciseDialog(erHashMap,roundList.get(i));
                                    }
                                });
                                selectRoundDialog.show();
                            }
                        }
                    });
                }else{
                    calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                        @Override
                        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            String selectedDate = date.getDate().format(dateTimeFormatter);
                            Toast.makeText(requireActivity().getApplicationContext(), selectedDate+" 운동 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //

                //
                String startDate =  weekCalendar()[0]; String endDate =  weekCalendar()[1];
                weekendExerciseRecord = appDatabase.exerciseRecordDao().getWeekExerciseRecord(startDate,endDate);
                exerciseTotalSumList = new Double[7]; // sun = 0  sat = 6
                for(int i = 0; i < 7; i++) exerciseTotalSumList[i] = 0.0;
                double max = 0;
                for(int i = 0; i < weekendExerciseRecord.size(); i++){
                    ExerciseRecord exerciseRecord = weekendExerciseRecord.get(i);
                    String exerciseListString = exerciseRecord.getErExerciseList();
                    int dayIndex = getDayByDate(exerciseRecord.getErDate());
                    HashMap<Integer, ExerciseTableInfo> hashMap;
                    Type type = new TypeToken<HashMap<Integer, ExerciseTableInfo>>(){}.getType();
                    hashMap = new Gson().fromJson(exerciseListString,type);

                    for(Integer key : hashMap.keySet()){
                        ExerciseTableInfo exerciseTableInfo = hashMap.get(key);
                        assert exerciseTableInfo != null;
                        ArrayList<String> header = exerciseTableInfo.getExerciseTableHeader();
                        String weightUnit = header.get(1).contains("kg") ? "kg" : "lbs";
                        ArrayList<String> body = exerciseTableInfo.getExerciseTableBody();
                        if(header.get(1).contains("무게") && header.get(2).contains("횟수")){
                            for(String row : body) {
                                if(row.contains("true")){
                                    Double weight = Double.valueOf(row.split("/")[1]);
                                    if(weightUnit.equals("lbs")) weight = weight * 0.45;
                                    int reps = Integer.parseInt(row.split("/")[2]);
                                    exerciseTotalSumList[dayIndex] += weight * reps;
                                }
                            }
                            if(max <  exerciseTotalSumList[dayIndex]) max = exerciseTotalSumList[dayIndex];
                        }
                    }
                }

                configureChartAppearance(calendarCard_weekChart,max);
                ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

                for(int i = 1; i <= 7; i++) {
                    double value = exerciseTotalSumList[i-1];
                    value = Math.round(value/100.0);
                    entries.add(new BarEntry(7-i,Float.parseFloat(String.valueOf(value))));
                }

                BarDataSet barDataSet = new BarDataSet(entries,"");
                barDataSet.setDrawValues(false);
                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                BarData barData =  new BarData(barDataSet);
                barData.setBarWidth(0.25f);
                calendarCard_weekChart.setData(barData);
                calendarCard_weekChart.invalidate();

                SystemClock.sleep(100);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        //메인 오늘의 운동, 그룹 운동 탭
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.home_exercise_list_tab_layout);
        fragment_home_today_exercise = new HomeTodayExerciseFragment();

        mainActivity.replaceFragment(R.id.main_exercise_card_tab_container,fragment_home_today_exercise);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0) mainActivity.replaceFragment(R.id.main_exercise_card_tab_container,fragment_home_today_exercise);

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return rootView;
    }

    private void configureChartAppearance(HorizontalBarChart calendarCard_weekChart, double max) {

        calendarCard_weekChart.getDescription().setEnabled(false); // chart 밑에 description 표시 유무
        calendarCard_weekChart.setTouchEnabled(false); // 터치 유무
        calendarCard_weekChart.getLegend().setEnabled(false); // Legend는 차트의 범례
        calendarCard_weekChart.setExtraOffsets(10f, 0f, 40f, 0f);

        // XAxis (수평 막대 기준 왼쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis = calendarCard_weekChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        //xAxis.setGranularity(1f);
        xAxis.setTextSize(12f);
        xAxis.setGridLineWidth(25f);
        xAxis.setAxisLineWidth(10f);
        xAxis.setGridColor(Color.parseColor("#FFFFFF")); //80E5E5E5
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 축 데이터 표시 위치

        // YAxis(Left) (수평 막대 기준 아래쪽) - 선 유무, 데이터 최솟값/최댓값, label 유무
        YAxis axisLeft = calendarCard_weekChart.getAxisLeft();
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisMinimum(0f); // 최솟값
        axisLeft.setAxisMaximum(Math.round(max/100.0)+1); // 최댓값
        //axisLeft.setGranularity((Math.round(max/100.0)+1)/10); // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false); // label 삭제

        // YAxis(Right) (수평 막대 기준 위쪽) - 사이즈, 선 유무
        YAxis axisRight = calendarCard_weekChart.getAxisRight();
        axisRight.setTextSize(15f);
        axisRight.setDrawLabels(false); // label 삭제
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);

        // XAxis에 원하는 String 설정하기 (앱 이름)
        xAxis.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {
                return DaysArr[(int) value];
            }
        });

    }

    public String[] weekCalendar()  {
        String startDt = new GetToday().getToday(); //범위 검색용 날짜
        String[] dateArray = startDt.split("-");//년, 월, 일 분리
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");//날짜 표시 포멧 지정
        Calendar cal = Calendar.getInstance();//캘린더 생성
        cal.set(Integer.parseInt(dateArray[0]), (Integer.parseInt(dateArray[1]) - 1), Integer.parseInt(dateArray[2]));//검색용 날짜 세팅
        cal.setFirstDayOfWeek(Calendar.SUNDAY);//일주일의 첫날 선택
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek(); //해당 주차 시작일과의 차이 구하기용
        cal.add(Calendar.DAY_OF_MONTH, - dayOfWeek); //해당 주차의 첫날 세팅
        String stDt = formatter.format(cal.getTime());//해당 주차의 첫일자
        cal.add(Calendar.DAY_OF_MONTH, 6); //해당 주차의 마지막 세팅
        String edDt = formatter.format(cal.getTime());//해당 주차의 마지막일자
        String[] results = new String[2];
        results[0] = stDt; results[1] = edDt;
        System.out.println(stDt + " " + edDt);
        return results;
    }

    private int getDayByDate(String date){
        String dateSplit = date.split(" ")[0];
        int year = Integer.parseInt(dateSplit.split("-")[0]);
        int month = Integer.parseInt(dateSplit.split("-")[1]);
        int day = Integer.parseInt(dateSplit.split("-")[2]);
        int total=(year-1)*365 +(year-1)/4 -(year-1)/100 +(year-1)/400;
        int[] lastDay= {31,28,31,30,31,30,31,31,30,31,30,31};
        if((year%4==0 && year%100!=0)||(year%400==0)) lastDay[1]=29;
        else lastDay[1]=28;
        for(int i=0;i<month-1;i++) total+=lastDay[i];
        total+=day;
        return total%7;
    }




    private void goToExerciseCompleteShare(ExerciseRecord er, String round) {
        ExerciseCompleteShare fragment_exercise_complete_share = new ExerciseCompleteShare();
        ArrayList<Exercise> exerciseList = new ArrayList<>();
        HashMap<Integer, ArrayList<RoutineInfoListItemDetailed>> bundle2 = new HashMap<>();

        //Toast.makeText(ExerciseMainActivity.this, "!", Toast.LENGTH_SHORT).show();
        //총 운동시간, 총 운동량, 설정 운동량, 총 운동 종목 수, 운동 리스트
        HashMap<Integer,ExerciseTableInfo> hashMap = toExerciseTableInfoHashMap(er.getErExerciseList());
        for(Integer i : hashMap.keySet()){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    exerciseList.add(appDatabase.exerciseDao().getExercise(i));
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();

            ArrayList<RoutineInfoListItemDetailed> bundle2Values = new ArrayList<>();
            for(String row : hashMap.get(i).getExerciseTableBody()){
                RoutineInfoListItemDetailed value = new RoutineInfoListItemDetailed();
                value.setSet(Integer.parseInt(row.split("/")[0]));
                value.setVal1(row.split("/")[1]);
                if(row.split("/").length==3){
                    if(row.split("/")[2].equals("true")) value.setCheck(true);
                    else value.setCheck(false);
                }else if(row.split("/").length==4){
                    value.setVal2(row.split("/")[2]);
                    if(row.split("/")[3].equals("true")) value.setCheck(true);
                    else value.setCheck(false);
                }
                bundle2Values.add(value);
            }
            bundle2.put(i,bundle2Values);
        }
        ExerciseCompleteBundle exerciseCompleteBundle = new ExerciseCompleteBundle(hashMap,0,
                er.getErTotalTime(),er.getErTotalWeight()+"",hashMap.size()+"");

        Bundle bundle = new Bundle();
        bundle.putSerializable("exerciseCompleteBundle", exerciseCompleteBundle);
        bundle.putSerializable("exerciseCompleteBundle2", bundle2);
        bundle.putSerializable("exerciseCompleteBundle3", exerciseList);
        bundle.putString("startExerciseTime",er.getErStartDate());
        bundle.putString("from","home");
        bundle.putSerializable("er",er);
        bundle.putString("round",round);
        fragment_exercise_complete_share.setArguments(bundle);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tab_content_view, fragment_exercise_complete_share).addToBackStack(null).commit();

    }


    @Nullable
    private HashMap<Integer, ExerciseTableInfo> toExerciseTableInfoHashMap(String routine) {
        Type type = new TypeToken<HashMap<Integer,ExerciseTableInfo>>() {}.getType();
        HashMap<Integer,ExerciseTableInfo> exerciseTableInfos = new Gson().fromJson(routine, type);
        return exerciseTableInfos;
    }
}