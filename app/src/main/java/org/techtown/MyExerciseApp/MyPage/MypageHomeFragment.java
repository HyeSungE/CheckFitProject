package org.techtown.MyExerciseApp.MyPage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.techtown.MyExerciseApp.Adapter.Mypage.BodypartRankItemAdapter;
import org.techtown.MyExerciseApp.Adapter.Mypage.LineChartMaxData;
import org.techtown.MyExerciseApp.Data.Exercise.ExerciseTableInfo;
import org.techtown.MyExerciseApp.Data.Feed.Post;
import org.techtown.MyExerciseApp.Data.Mypage.MypageBodypartRank;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Exercise.ExerciseListFragment;
import org.techtown.MyExerciseApp.Feed.EditProfile;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.Main.LoginActivity;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.MyClass.ShowAlertSimpleMessage;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.techtown.MyExerciseApp.db.Entity.ExerciseRecord;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MypageHomeFragment extends Fragment{

    private User currentUser;
    //View
    private TextView mypage_exercise_analytics_exercise_name,mypage_exercise_analytics_1rm,mypage_no_line_chart_message;
    private TextView mypage_exercise_analytics_max_weight,mypage_exercise_analytics_max_volume,mypage_no_ranking_message;
    private TextView mypage_exercise_analytics_exercise_rank_name,mypage_exercise_analytics_exercise_my_rank;
    private TextView my_feed_posts,my_feed_followers,my_feed_following;
    private TextView mypage_line_chart_date;
    private ImageView mypage_image_profile;
    private Button mypage_exercise_analytics_exercise_select,mypage_line_chart_1rm,mypage_line_chart_weight_max,mypage_line_chart_max_vol,my_feed_edit_profile;
    private ListView mypage_exercise_priority_rank_recycler;
    private RadarChart mypage_exercise_priority_radarChart;
    private LineChart mypage_exercise_analytics_lineChart;
    private CardView mypage_1rm_first_card,mypage_1rm_second_card,mypage_1rm_third_card;
    //
    private ShowAlertSimpleMessage showAlertSimpleMessage = new ShowAlertSimpleMessage();
    private AppDatabase appDatabase;
    private FirebaseAuth auth; private DatabaseReference reference;FirebaseUser user; private StorageReference storageRef;
    private Context context;
    private int indexForRank =0;
    //rader chart date
    private List<ExerciseRecord> allExerciseRecord;
    private ArrayList<RadarEntry> radarEntries;
    private int[] radarDate;
    //private String[] radarLabels =  {"가슴", "어깨", "등", "하체", "팔", "유산소", "복근"};
    // String[] radarLabels =  {"복근", "유산소", "팔", "하체", "등", "어깨", "가슴"};
    private BodypartRankItemAdapter bodypartRankItemAdapter;
    private CardView[] _1rmCardViews;

    //1rm max vol
    private Double weight1Rm,weightMax,volMax;
    private int weightMaxReps;
    private ArrayList<Entry> lineChart1Rm,lineChartWeightMax,lineChartVolMax;
    private ArrayList<String> lineChartDate;
    private LineData lineChartData;

    //line chart data

    //
    public static final int EDIT_PROFILE_REQUEST = 0327;
    public MypageHomeFragment() {
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_mypage_home, container, false);
        //TextView
        mypage_exercise_analytics_exercise_name = rootView.findViewById(R.id.mypage_exercise_analytics_exercise_name);
        mypage_exercise_analytics_1rm = rootView.findViewById(R.id.mypage_exercise_analytics_1rm);
        mypage_exercise_analytics_max_weight = rootView.findViewById(R.id.mypage_exercise_analytics_max_weight);
        mypage_exercise_analytics_max_volume = rootView.findViewById(R.id.mypage_exercise_analytics_max_volume);
        mypage_exercise_analytics_exercise_rank_name = rootView.findViewById(R.id.mypage_exercise_analytics_exercise_rank_name);
        mypage_exercise_analytics_exercise_my_rank = rootView.findViewById(R.id.mypage_exercise_analytics_exercise_my_rank);
        my_feed_posts = rootView.findViewById(R.id.my_feed_posts);
        my_feed_followers = rootView.findViewById(R.id.my_feed_followers);
        my_feed_following = rootView.findViewById(R.id.my_feed_following);
        mypage_line_chart_date = rootView.findViewById(R.id.mypage_line_chart_date);
        mypage_line_chart_date = rootView.findViewById(R.id.mypage_line_chart_date);
        mypage_no_ranking_message = rootView.findViewById(R.id.mypage_no_ranking_message);
        //mypage_no_ranking_message.setVisibility(View.GONE);
        mypage_no_line_chart_message = rootView.findViewById(R.id.mypage_no_line_chart_message);

        //ImageView
        mypage_image_profile = rootView.findViewById(R.id.mypage_image_profile);
        //Button
        mypage_exercise_analytics_exercise_select = rootView.findViewById(R.id.mypage_exercise_analytics_exercise_select);
        mypage_line_chart_1rm = rootView.findViewById(R.id.mypage_line_chart_1rm);
        mypage_line_chart_weight_max = rootView.findViewById(R.id.mypage_line_chart_weight_max);
        mypage_line_chart_max_vol = rootView.findViewById(R.id.mypage_line_chart_max_vol);
        my_feed_edit_profile = rootView.findViewById(R.id.my_feed_edit_profile);
        //ListView
        mypage_exercise_priority_rank_recycler = rootView.findViewById(R.id.mypage_exercise_priority_rank_recycler);
        //Chart
        mypage_exercise_priority_radarChart = rootView.findViewById(R.id.mypage_exercise_priority_radarChart);
        mypage_exercise_analytics_lineChart = rootView.findViewById(R.id.mypage_exercise_analytics_lineChart);
        //CardView
        mypage_1rm_first_card = rootView.findViewById(R.id.mypage_1rm_first_card); mypage_1rm_first_card.setVisibility(View.GONE);
        mypage_1rm_second_card = rootView.findViewById(R.id.mypage_1rm_second_card); mypage_1rm_second_card.setVisibility(View.GONE);
        mypage_1rm_third_card = rootView.findViewById(R.id.mypage_1rm_third_card); mypage_1rm_third_card.setVisibility(View.GONE);
        _1rmCardViews = new CardView[3];
        _1rmCardViews[0] = mypage_1rm_third_card;  _1rmCardViews[1] = mypage_1rm_second_card;  _1rmCardViews[2] = mypage_1rm_first_card;



        context = requireActivity().getApplicationContext();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        appDatabase = AppDatabase.getDBInstance(requireActivity().getApplicationContext());


        if(user == null){
            showAlertSimpleMessage.show(getContext(),"오류","로그인 후 이용해 주세요\n로그인으로 이동합니다!");
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        my_feed_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetUser("uid",user.getUid()).getUserByUid(new GetUserListener() {
                    @Override
                    public User getUserLoaded(User user) {
                        storageRef = FirebaseStorage.getInstance().getReference("profile_image");
                        storageRef.child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context).load(uri).into(mypage_image_profile);
                                getFollowers(user); getPosts(user);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("user",user);
                                bundle.putString("from","mypage");
                                Intent intent = new Intent(getActivity(), EditProfile.class);
                                intent.putExtras(bundle);
                                startActivityForResult(intent,EDIT_PROFILE_REQUEST);
                            }
                        });
                        return user;
                    }
                });

            }
        });

        new GetUser("uid",user.getUid()).getUserByUid(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                storageRef = FirebaseStorage.getInstance().getReference("profile_image");
                storageRef.child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(mypage_image_profile);
                        getFollowers(user); getPosts(user);

                    }
                });
                return user;
            }
        });

        //RaderChart
        radarEntries = new ArrayList<>();
        allExerciseRecord = new ArrayList<>();
        radarDate = new int[7];

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                allExerciseRecord = appDatabase.exerciseRecordDao().getAllExerciseRecord();
                if(!allExerciseRecord.isEmpty()){
                    //get data
                    for(ExerciseRecord exerciseRecord : allExerciseRecord){
                        String exerciseListString = exerciseRecord.getErExerciseList();
                        HashMap<Integer, ExerciseTableInfo> hashMap;
                        Type type = new TypeToken<HashMap<Integer, ExerciseTableInfo>>(){}.getType();
                        hashMap = new Gson().fromJson(exerciseListString,type);
                        for(Integer key : hashMap.keySet()){
                            ExerciseTableInfo exerciseTableInfo = hashMap.get(key);
                            assert exerciseTableInfo != null;
                            String name = exerciseTableInfo.getExerciseName();
                            int index = bodypartToIndex(name.split(" \\| ")[0].trim());
                            radarDate[index] = radarDate[index] + 1;
                        }
                    }
                    //set data
                    ArrayList<String> radarLabels = new ArrayList<>();
                    for(int i = 0; i < 7; i++){
                        radarEntries.add(new RadarEntry(radarDate[i]));
                        radarLabels.add(indexToBodypart(i));
                        System.out.println("radarDate[i]" + radarDate[i]+"");
                    }

                    //set chart
                    configureChartAppearance(mypage_exercise_priority_radarChart);
                    RadarDataSet dataSet = new RadarDataSet(radarEntries, "부위별 운동 횟수");
                    dataSet.setColor(Color.parseColor("#F4511E"));
                    dataSet.setDrawValues(false);
                    RadarData data = new RadarData();
                    data.addDataSet(dataSet);
                    XAxis xAxis = mypage_exercise_priority_radarChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(radarLabels));
                    mypage_exercise_priority_radarChart.setData(data);
                    mypage_exercise_priority_radarChart.invalidate();
                }
            }
        };
        Thread thread = new Thread(runnable);thread.start();
        try{Thread.sleep(500);}
        catch (InterruptedException e){e.printStackTrace();}
        //set bodypart rank
        ArrayList<MypageBodypartRank> mypageBodypartRanks = new ArrayList<MypageBodypartRank>();
        int[] rank = getRank(radarDate);
        for(int i = 0; i < rank.length; i++){
            MypageBodypartRank mypageBodypartRank = new MypageBodypartRank(rank[i],indexToBodypart(i),radarDate[i]);
            mypageBodypartRanks.add(mypageBodypartRank);
        }
        Collections.sort(mypageBodypartRanks);
        bodypartRankItemAdapter = new BodypartRankItemAdapter(getContext(),mypageBodypartRanks);
        mypage_exercise_priority_rank_recycler.setAdapter(bodypartRankItemAdapter);


        mypage_exercise_analytics_exercise_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment exericseListFragment = new ExerciseListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from","mypage");
                exericseListFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.tab_content_view,exericseListFragment).addToBackStack("null").commit();

            }
        });

        savedInstanceState = getArguments();
        if(savedInstanceState != null){
            mypage_no_line_chart_message.setVisibility(View.GONE);
            mypage_exercise_analytics_lineChart.setVisibility(View.VISIBLE);
            Exercise exercise = (Exercise)savedInstanceState.getSerializable("selectedExercise");
            String exName = exercise.getExName();
            mypage_exercise_analytics_exercise_name.setText(exName);

            ArrayList<LineChartMaxData> lineChartMaxDatas = new ArrayList<LineChartMaxData>();
            Runnable runnable2 = new Runnable() {
                @Override
                public void run() {
                    allExerciseRecord = appDatabase.exerciseRecordDao().getSelectedExerciseRecord(exName);
                    if(!allExerciseRecord.isEmpty()){
                        //get data
                        weight1Rm = 0.0; weightMax =0.0; volMax=0.0;
                        for(ExerciseRecord exerciseRecord : allExerciseRecord){

                            String exerciseListString = exerciseRecord.getErExerciseList();
                            HashMap<Integer, ExerciseTableInfo> hashMap;
                            Type type = new TypeToken<HashMap<Integer, ExerciseTableInfo>>(){}.getType();
                            hashMap = new Gson().fromJson(exerciseListString,type);
                            for(Integer key : hashMap.keySet()){
                                ExerciseTableInfo exerciseTableInfo = hashMap.get(key);
                                assert exerciseTableInfo != null;
                                String name = exerciseTableInfo.getExerciseName();
                                ArrayList<String> header = exerciseTableInfo.getExerciseTableHeader();
                                String weightUnit = header.get(1).contains("kg") ? "kg" : "lbs";
                                ArrayList<String> body = exerciseTableInfo.getExerciseTableBody();

                                if(name.split("\\|")[1].trim().equals(exercise.getExName())&& header.get(1).contains("무게") && header.get(2).contains("횟수")){
                                    double tempVol = 0.0;
                                    double temp1Rm = 0.0;
                                    double tempMaxWeight = 0.0;
                                    int tempMaxReps = 0;
                                    LineChartMaxData lineChartMaxData = new LineChartMaxData();
                                    lineChartMaxData.setDate(exerciseRecord.getErDate());
                                    for(String row : body) {
                                        if(row.contains("true")){
                                            Double weight = Double.valueOf(row.split("/")[1]);
                                            if(weightUnit.equals("lbs")) weight = weight * 0.45;
                                            int reps = Integer.parseInt(row.split("/")[2]);
                                            if(weightMax < weight) {
                                                weightMax = weight;
                                                weightMaxReps = reps;
                                            }
                                            tempVol += weight * reps;

                                            if(tempMaxWeight < weight){
                                                tempMaxWeight = weight;
                                                tempMaxReps = reps;
                                            }
                                        }
                                    }
                                    if(volMax <= tempVol) volMax = tempVol;
                                    lineChartMaxData.setWeightMax(tempMaxWeight);
                                    lineChartMaxData.setVolMax(volMax);
                                    lineChartMaxData.setWeight1Rm(tempMaxWeight / (1.0278 - (0.0278 * tempMaxReps)));
                                    lineChartMaxDatas.add(lineChartMaxData);
                                }
                            }
                        }
                        weight1Rm = weightMax / (1.0278 - (0.0278 * weightMaxReps));


                    }
                    for(int i = 0; i < lineChartMaxDatas.size(); i++) {
                        LineChartMaxData lineChartMaxData = lineChartMaxDatas.get(i);
                        System.out.println("lineChartMaxData : " + lineChartMaxData.getDate()+ " " +lineChartMaxData.getWeight1Rm()
                        +" "+ lineChartMaxData.getWeightMax()+" "+ lineChartMaxData.getVolMax());

                    }
                    //line chart
                    lineChartData = new LineData();
                    lineChartDate = new ArrayList<>();  lineChart1Rm = new ArrayList<>();  lineChartWeightMax = new ArrayList<>();  lineChartVolMax = new ArrayList<>();
                    for(int i = 0; i < lineChartMaxDatas.size(); i++) {
                        LineChartMaxData lineChartMaxData = lineChartMaxDatas.get(i);
                        System.out.println("lineChartMaxData : " + lineChartMaxData.getDate()+ " " +lineChartMaxData.getWeight1Rm());
                        lineChartDate.add(lineChartMaxData.getDate());
                        lineChart1Rm.add(new Entry(i,Float.valueOf(String.valueOf(lineChartMaxData.getWeight1Rm()))));
                        lineChartWeightMax.add(new Entry(i,Float.valueOf(String.valueOf(lineChartMaxData.getWeightMax()))));
                        lineChartVolMax.add(new Entry(i,Float.valueOf(String.valueOf(lineChartMaxData.getVolMax()))));
                    }
                    LineDataSet lineData1Rm = new LineDataSet(lineChart1Rm, "1RM");
                    LineDataSet lineDataWeightMax = new LineDataSet(lineChartWeightMax, "최고 중량");
                    LineDataSet lineDataVolMax = new LineDataSet(lineChartVolMax, "최대 볼륨");

                    lineData1Rm.setColor(Color.RED); lineData1Rm.setLineWidth(3f);
                    lineDataWeightMax.setColor(Color.BLUE); lineDataWeightMax.setLineWidth(3f);
                    lineDataVolMax.setColor(Color.GREEN); lineDataVolMax.setLineWidth(3f);

                    //mypage_exercise_analytics_lineChart.setTouchEnabled(false);
                    mypage_exercise_analytics_lineChart.setDescription(null);
                    mypage_exercise_analytics_lineChart.setDragEnabled(false);
                    mypage_exercise_analytics_lineChart.setScaleEnabled(false);
                    mypage_exercise_analytics_lineChart.setScaleXEnabled(false);
                    mypage_exercise_analytics_lineChart.setScaleYEnabled(false);
                    mypage_exercise_analytics_lineChart.setPinchZoom(false);
                    XAxis xAxis = mypage_exercise_analytics_lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawLabels(false);

                    mypage_exercise_analytics_lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                            mypage_line_chart_date.setText(lineChartDate.get((int) e.getX()));
                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });



                    lineChartData.addDataSet(lineData1Rm);
                    mypage_exercise_analytics_lineChart.setData(lineChartData);
                    mypage_exercise_analytics_lineChart.invalidate();

                    mypage_line_chart_1rm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            lineChartData.clearValues();
                            mypage_exercise_analytics_lineChart.clear();
                            lineChartData.addDataSet(lineData1Rm);
                            mypage_exercise_analytics_lineChart.setData(lineChartData);
                            mypage_exercise_analytics_lineChart.invalidate();
                        }
                    });
                    mypage_line_chart_weight_max.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            lineChartData.clearValues();
                            mypage_exercise_analytics_lineChart.clear();
                            lineChartData.addDataSet(lineDataWeightMax);
                            mypage_exercise_analytics_lineChart.setData(lineChartData);
                            mypage_exercise_analytics_lineChart.invalidate();
                        }
                    });
                    mypage_line_chart_max_vol.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            lineChartData.clearValues();
                            mypage_exercise_analytics_lineChart.clear();
                            lineChartData.addDataSet(lineDataVolMax);
                            mypage_exercise_analytics_lineChart.setData(lineChartData);
                            mypage_exercise_analytics_lineChart.invalidate();
                        }
                    });

                    FirebaseDatabase.getInstance().getReference("ONERM").child(String.valueOf(exercise.getExId())).orderByValue().limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String[] uidArr = new String[3]; Double[] doubleArr = new Double[3];
                            int i = 0;

                            int children = (int) snapshot.getChildrenCount();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                System.out.println("snapshot.getChildren()=" + snapshot.getChildren());
                                String uid = dataSnapshot.getKey();
                                Double value = (Double) dataSnapshot.getValue();
                                uidArr[i] = uid;
                                doubleArr[i] = value;
                                i++;
                            }
                            if(children > 0){
                                mypage_no_ranking_message.setVisibility(View.GONE);
                                if(children == 1){
                                    if(uidArr[0] != null){

                                        new GetUser("uid",uidArr[0]).getUserByUid(new GetUserListener() {
                                            @Override
                                            public User getUserLoaded(User user) {
                                                FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage())
                                                        .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                CardView cardView = (CardView) _1rmCardViews[2];
                                                                cardView.setVisibility(View.VISIBLE);
                                                                RelativeLayout rel = (RelativeLayout) cardView.getChildAt(0);
                                                                Glide.with(context).load(uri).into((CircleImageView)rel.getChildAt(1));
                                                                ((TextView)rel.getChildAt(2)).setText(user.getNickname());
                                                                ((TextView)rel.getChildAt(3)).setText(String.valueOf(Math.round(doubleArr[0])) + "kg");
                                                                _1rmCardViews[0].setVisibility(View.INVISIBLE);
                                                                _1rmCardViews[1].setVisibility(View.INVISIBLE);
                                                            }
                                                        });

                                                return user;
                                            }
                                        });

                                    }
                                }
                                else if(children == 2){
                                    new GetUser("uid",uidArr[0]).getUserByUid(new GetUserListener() {
                                        @Override
                                        public User getUserLoaded(User user) {
                                            FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage())
                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            CardView cardView = (CardView) _1rmCardViews[1];
                                                            cardView.setVisibility(View.VISIBLE);
                                                            RelativeLayout rel = (RelativeLayout) cardView.getChildAt(0);
                                                            Glide.with(context).load(uri).into((CircleImageView)rel.getChildAt(1));
                                                            ((TextView)rel.getChildAt(2)).setText(user.getNickname());
                                                            ((TextView)rel.getChildAt(3)).setText(String.valueOf(Math.round(doubleArr[0])) + "kg");

                                                        }
                                                    });

                                            return user;
                                        }
                                    });
                                    new GetUser("uid",uidArr[1]).getUserByUid(new GetUserListener() {
                                        @Override
                                        public User getUserLoaded(User user) {
                                            FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage())
                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            CardView cardView = (CardView) _1rmCardViews[2];
                                                            cardView.setVisibility(View.VISIBLE);
                                                            RelativeLayout rel = (RelativeLayout) cardView.getChildAt(0);
                                                            Glide.with(context).load(uri).into((CircleImageView)rel.getChildAt(1));
                                                            ((TextView)rel.getChildAt(2)).setText(user.getNickname());
                                                            ((TextView)rel.getChildAt(3)).setText(String.valueOf(Math.round(doubleArr[1])) + "kg");
                                                            _1rmCardViews[0].setVisibility(View.INVISIBLE);
                                                        }
                                                    });

                                            return user;
                                        }
                                    });

                                }
                                else{
                                    new GetUser("uid",uidArr[0]).getUserByUid(new GetUserListener() {
                                        @Override
                                        public User getUserLoaded(User user) {
                                            FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage())
                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            CardView cardView = (CardView) _1rmCardViews[0];
                                                            cardView.setVisibility(View.VISIBLE);
                                                            RelativeLayout rel = (RelativeLayout) cardView.getChildAt(0);
                                                            Glide.with(context).load(uri).into((CircleImageView)rel.getChildAt(1));
                                                            ((TextView)rel.getChildAt(2)).setText(user.getNickname());
                                                            ((TextView)rel.getChildAt(3)).setText(String.valueOf(Math.round(doubleArr[0])) + "kg");

                                                        }
                                                    });

                                            return user;
                                        }
                                    });
                                    new GetUser("uid",uidArr[1]).getUserByUid(new GetUserListener() {
                                        @Override
                                        public User getUserLoaded(User user) {
                                            FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage())
                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            CardView cardView = (CardView) _1rmCardViews[1];
                                                            cardView.setVisibility(View.VISIBLE);
                                                            RelativeLayout rel = (RelativeLayout) cardView.getChildAt(0);
                                                            Glide.with(context).load(uri).into((CircleImageView)rel.getChildAt(1));
                                                            ((TextView)rel.getChildAt(2)).setText(user.getNickname());
                                                            ((TextView)rel.getChildAt(3)).setText(String.valueOf(Math.round(doubleArr[1])) + "kg");

                                                        }
                                                    });

                                            return user;
                                        }
                                    });
                                    new GetUser("uid",uidArr[2]).getUserByUid(new GetUserListener() {
                                        @Override
                                        public User getUserLoaded(User user) {
                                            FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage())
                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            CardView cardView = (CardView) _1rmCardViews[2];
                                                            cardView.setVisibility(View.VISIBLE);
                                                            RelativeLayout rel = (RelativeLayout) cardView.getChildAt(0);
                                                            Glide.with(context).load(uri).into((CircleImageView)rel.getChildAt(1));
                                                            ((TextView)rel.getChildAt(2)).setText(user.getNickname());
                                                            ((TextView)rel.getChildAt(3)).setText(String.valueOf(Math.round(doubleArr[2])) + "kg");

                                                        }
                                                    });

                                            return user;
                                        }
                                    });
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    if(weight1Rm!=null){
                        FirebaseDatabase.getInstance().getReference("ONERM").child(String.valueOf(exercise.getExId()))
                                .orderByValue().startAfter(Math.round(weight1Rm)).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int myRank = (int) snapshot.getChildrenCount();
                                        //System.out.println("children"+children);
                                        //mypage_exercise_analytics_exercise_my_rank.setText(children+"위");
                                        FirebaseDatabase.getInstance().getReference("ONERM").child(String.valueOf(exercise.getExId())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        int children = (int) snapshot.getChildrenCount();
                                                        System.out.println("children"+children);
                                                        mypage_exercise_analytics_exercise_my_rank
                                                                .setText(children + "명 중 " + myRank+"위(상위 "+(100 - ( (1-((myRank-1.0)/children))*100))+
                                                                      " %)");

                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }

                }
            };
            Thread thread2 = new Thread(runnable2);thread2.start();
            try{Thread.sleep(1000);}
            catch (InterruptedException e){e.printStackTrace();}
            if(weight1Rm!=null){
                mypage_exercise_analytics_1rm.setText(exercise.getExName()+" 1RM은 " + Math.round(weight1Rm) +"KG 입니다.");
                mypage_exercise_analytics_max_weight.setText(exercise.getExName() +"의 최고 중량은 "+weightMax +"KG 입니다.");
                mypage_exercise_analytics_max_volume.setText(exercise.getExName() +"의 최고 운동 볼륨은 "+volMax +"KG 입니다.");
                mypage_no_line_chart_message.setVisibility(View.GONE);
                mypage_exercise_analytics_lineChart.setVisibility(View.VISIBLE);
            }else{
                mypage_exercise_analytics_1rm.setText(exercise.getExName()+" 1RM은 " + 0 +"KG 입니다.");
                mypage_exercise_analytics_max_weight.setText(exercise.getExName() +"의 최고 중량은 "+0 +"KG 입니다.");
                mypage_exercise_analytics_max_volume.setText(exercise.getExName() +"의 최고 운동 볼륨은 "+0 +"KG 입니다.");
                mypage_no_line_chart_message.setText("운동을 해주세요 !");
                mypage_no_line_chart_message.setVisibility(View.VISIBLE);
                mypage_exercise_analytics_lineChart.setVisibility(View.GONE);
            }


        }

        return rootView;
    }

    private void getFollowers(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("FOLLOW").child(user.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_feed_followers.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("FOLLOW").child(user.getUid()).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                my_feed_following.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getPosts(User user){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("POST");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getUid().equals(user.getUid())){
                        i++;
                    }
                }
                my_feed_posts.setText(""+i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private int bodypartToIndex(String bodypart){
        switch (bodypart) {
            case "가슴" : {
                return 0;
            }
            case "어깨" : {
                return 1;
            }
            case "등" : {
                return 2;
            }
            case "하체" : {
                return 3;
            }
            case "팔" : {
                return 4;
            }
            case "유산소" : {
                return 5;
            }
            case "복근" : {
                return 6;
            }
        }
        return -1;
    }
    private String indexToBodypart(int index) {
        switch (index) {
            case 0 : {
                return "가슴";
            }
            case 1 : {
                return "어깨";
            }
            case 2 : {
                return "등";
            }
            case 3 : {
                return "하체";
            }
            case 4 : {
                return "팔";
            }
            case 5 : {
                return "유산소";
            }
            case 6 : {
                return "복근";
            }
        }
        return null;
    }
    private void configureChartAppearance(RadarChart mypage_exercise_priority_radarChart) {

        mypage_exercise_priority_radarChart.getDescription().setEnabled(false); // chart 밑에 description 표시 유무
        mypage_exercise_priority_radarChart.setTouchEnabled(false); // 터치 유무
        mypage_exercise_priority_radarChart.getLegend().setEnabled(false); // Legend는 차트의 범례
        mypage_exercise_priority_radarChart.setExtraOffsets(10f, 0f, 40f, 0f);

        // XAxis (수평 막대 기준 왼쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis = mypage_exercise_priority_radarChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        //xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setGridLineWidth(25f);
        xAxis.setGridColor(Color.parseColor("#FFFFFF")); //80E5E5E5
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 축 데이터 표시 위치

        // YAxis(Left) (수평 막대 기준 아래쪽) - 선 유무, 데이터 최솟값/최댓값, label 유무
        YAxis axisLeft = mypage_exercise_priority_radarChart.getYAxis();
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisMinimum(0f); // 최솟값
        //axisLeft.setAxisMaximum(Math.round(max/100.0)+1); // 최댓값
        //axisLeft.setGranularity((Math.round(max/100.0)+1)/10); // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false); // label 삭제

        // YAxis(Right) (수평 막대 기준 위쪽) - 사이즈, 선 유무
        YAxis axisRight = mypage_exercise_priority_radarChart.getYAxis();
        axisRight.setTextSize(15f);
        axisRight.setDrawLabels(false); // label 삭제
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);





    }
    private int[] getRank(int[] data){
        int[] rankArr = {1, 1, 1, 1, 1, 1, 1}; // 각 점수별 순위(1로 초기화)
        for(int i = 0; i<radarDate.length; i++) {
            rankArr[i] = 1; // 순위 배열을 for 돌때마다 1등으로 초기화
            for(int j = 0; j<radarDate.length; j++) { // 배열 i 인덱스의 점수와 나머지 점수 비교
                if(radarDate[i] < radarDate[j]) { // i 인덱스의 값보다 크다면
                    rankArr[i] = rankArr[i] + 1; // 순위 증가
                }
            }
        }
        return rankArr;
    }


}