package org.techtown.MyExerciseApp.Group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.techtown.MyExerciseApp.Adapter.Group.GroupMemberAdapter;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItem;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineInfoListItemDetailed;
import org.techtown.MyExerciseApp.Data.Exercise.RoutineItem;
import org.techtown.MyExerciseApp.Data.Group.Group;
import org.techtown.MyExerciseApp.Data.Group.GroupRoutine;
import org.techtown.MyExerciseApp.Data.User;
import org.techtown.MyExerciseApp.Exercise.RoutineViewFragment;
import org.techtown.MyExerciseApp.Home.TodayDecorator;
import org.techtown.MyExerciseApp.Interface.GetUserListener;
import org.techtown.MyExerciseApp.Main.MainActivity;
import org.techtown.MyExerciseApp.MyClass.GetToday;
import org.techtown.MyExerciseApp.MyClass.GetUser;
import org.techtown.MyExerciseApp.R;
import org.techtown.MyExerciseApp.db.Database.AppDatabase;
import org.techtown.MyExerciseApp.db.Entity.Exercise;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;


public class ShowGroupFragment extends Fragment implements MainActivity.onBackPressedListener{

    private Button show_today_group_routine,delete_group_bt,delete_member_bt,shart_routine_bt,show_group_leave_group;

    private TableLayout show_group_member;
    private MaterialCalendarView show_group_routine_history;
    private Group selectedGroup;
    private ImageView leader_profile_image;
    private TextView leader_nickname;
    private LinearLayout show_group_buttons_leader,show_group_buttons_member;
    private long insertResult;
    private String currentUid,routineDialogName = "";
    private boolean isGroup;
    private ImageButton show_group_member_expand;
    private AppDatabase appDatabase;
    private OnBackPressedCallback onBackPressedCallback;

    private final TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.MATCH_PARENT,
            0.5f
    );

    private final TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.MATCH_PARENT,
            1f
    );
    private final TableRow.LayoutParams params = new TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
    );


    public ShowGroupFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = Objects.requireNonNull(requireActivity()).getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(ShowGroupFragment.this).commit();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_show_group, container, false);
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appDatabase = AppDatabase.getDBInstance(requireActivity().getApplicationContext());
        savedInstanceState = getArguments();
        selectedGroup = (Group) savedInstanceState.getSerializable("selectedGroup");
        show_today_group_routine = rootView.findViewById(R.id.show_today_group_routine);
        delete_group_bt = rootView.findViewById(R.id.delete_group_bt);
        delete_member_bt = rootView.findViewById(R.id.delete_member_bt);
        shart_routine_bt = rootView.findViewById(R.id.shart_routine_bt);
        show_group_member = rootView.findViewById(R.id.show_group_member);
        leader_profile_image = rootView.findViewById(R.id.leader_profile_image);
        leader_nickname = rootView.findViewById(R.id.leader_nickname);
        show_group_routine_history = rootView.findViewById(R.id.show_group_routine_history);
        show_group_buttons_leader = rootView.findViewById(R.id.show_group_buttons_leader);
        show_group_buttons_member = rootView.findViewById(R.id.show_group_buttons_member);
        show_group_leave_group = rootView.findViewById(R.id.show_group_leave_group);

        show_group_member_expand = rootView.findViewById(R.id.show_group_member_expand);
        show_group_member_expand.setTag("expand");

        layoutParams.gravity = Gravity.CENTER;
        layoutParams2.gravity = Gravity.CENTER;
        params.gravity = Gravity.CENTER;

        show_today_group_routine.setText(new GetToday().getToday()+"\n오늘의 운동 루틴은 ?!");
        show_today_group_routine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isGroup = false;
                Query query = FirebaseDatabase.getInstance().getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupMembers").orderByValue().equalTo(currentUid);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showGroupRoutineOneDay(snapshot,new GetToday().getToday());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        delete_group_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDeleteGroup();
            }
        });
        delete_member_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showViewGroupMembersAlertDialog();}
        });
        shart_routine_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String today = new GetToday().getToday();
                String path = "GROUP/"+selectedGroup.getGroupUid()+"/groupRoutines/"+today;
                FirebaseDatabase.getInstance().getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showAlertRenewGroupRoutine(snapshot.hasChildren());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
        show_group_leave_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertLeaveGroup();
            }
        });
        if(currentUid.equals(selectedGroup.getGroupLeaderUid())) {
            show_group_buttons_leader.setVisibility(View.VISIBLE);
        }else{
            Query query = FirebaseDatabase.getInstance().getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupMembers").orderByValue().equalTo(currentUid);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChildren()) show_group_buttons_member.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        new GetUser("uid",selectedGroup.getGroupLeaderUid()).getUserByUid(new GetUserListener() {
            @Override
            public User getUserLoaded(User user) {
                FirebaseStorage.getInstance().getReference("profile_image/"+user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(ShowGroupFragment.this).load(uri).into(leader_profile_image);
                        leader_nickname.setText(user.getNickname());
                    }
                });
                return null;
            }
        });
        FirebaseDatabase.getInstance()
                .getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupMembers").addListenerForSingleValueEvent(
                        new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String memberUid = dataSnapshot.getValue(String.class);
                        new GetUser("uid", memberUid).getUserByUid(new GetUserListener() {
                            @Override
                            public User getUserLoaded(User user) {
                                FirebaseStorage.getInstance().getReference("profile_image").child(user.getProfileImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        System.out.println("  String memberUid = dataSnapshot.getValue(String.class);"+memberUid);
                                        TableRow row = new TableRow(getContext());
                                        row.setLayoutParams(params);
                                        ImageView profileImage = new ImageView(getContext());
                                        TextView userNickname = new TextView(getContext());
                                        profileImage.setLayoutParams(layoutParams);
                                        userNickname.setLayoutParams(layoutParams2);
                                        userNickname.setGravity(Gravity.CENTER);
                                        Glide.with(ShowGroupFragment.this).load(uri).into(profileImage);
                                        userNickname.setText(user.getNickname());
                                        row.addView(profileImage);
                                        row.addView(userNickname);
                                        show_group_member.addView(row);
                                        if(show_group_member.getChildCount()>=7) row.setVisibility(View.GONE);
                                    }
                                });
                                return null;
                            }
                        });
                    }
                }
                if(show_group_member.getChildCount() >= 7)  {
                    show_group_member_expand.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //캘린더
        show_group_routine_history.addDecorator(new TodayDecorator(CalendarDay.today()));
        show_group_routine_history.setSelectedDate(CalendarDay.today());
        FirebaseDatabase.getInstance().getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupRoutines").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<CalendarDay> calendarDays = new HashSet<>();
                if(snapshot.hasChildren()){
                    for(DataSnapshot childSnapshot : snapshot.getChildren()){
                        String date = childSnapshot.getKey();
                        CalendarDay calendarDay = CalendarDay.from(
                                Integer.parseInt(date.split("-")[0]),
                                Integer.parseInt(date.split("-")[1]),
                                Integer.parseInt(date.split("-")[2])
                        );
                        calendarDays.add(calendarDay);
                    }
                    show_group_routine_history.addDecorators(new GroupCalendarDecorator(Color.RED,calendarDays));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {Thread.sleep(1000);} catch(InterruptedException e){e.printStackTrace();}
        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        show_group_routine_history.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                //Toast.makeText(requireActivity().getApplicationContext(), date.getDate().format(dateTimeFormatter), Toast.LENGTH_SHORT).show();
                String selectedDate = date.getDate().format(dateTimeFormatter);
                isGroup = false;
                Query query = FirebaseDatabase.getInstance().getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupMembers").orderByValue().equalTo(currentUid);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        showGroupRoutineOneDay(snapshot,selectedDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //확장

        show_group_member_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(show_group_member_expand.getTag().equals("expand")){
                    show_group_member_expand.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                    show_group_member_expand.setTag("fold");
                    if(show_group_member.getChildCount()>=7){
                        for (int i = 6; i<show_group_member.getChildCount(); i++){
                            show_group_member.getChildAt(i).setVisibility(View.VISIBLE);
                        }
                    }

                }else{
                    show_group_member_expand.setBackgroundResource(R.drawable.baseline_keyboard_arrow_down_24);
                    show_group_member_expand.setTag("expand");
                    if(show_group_member.getChildCount()>=7){
                        for (int i = 6; i<show_group_member.getChildCount(); i++){
                            show_group_member.getChildAt(i).setVisibility(View.GONE);
                        }
                    }
                }

            }
        });
        return rootView;
    }

    private void showAlertDeleteGroup() {
        AlertDialog.Builder deleteGroupAlert_builder = new AlertDialog.Builder(requireActivity());
        deleteGroupAlert_builder.setTitle("확인").setMessage("정말로 그룹을 삭제하시겠습니까 ?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase.getInstance().getReference("GROUP").child(selectedGroup.getGroupUid())
                                .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Query query = FirebaseDatabase.getInstance().getReference("GROUP_WAITING").child("LEADER").orderByKey().equalTo(selectedGroup.getGroupLeaderUid());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChildren()){
                                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                HashMap<String,Object> hs = (HashMap<String, Object>) dataSnapshot.getValue();
                                                for(String key : hs.keySet()){
                                                    if(key.equals(selectedGroup.getGroupUid())){
                                                        //List<String> uids = (List<String>) hs.get(key);
                                                        HashMap<String,Object> uids = (HashMap<String, Object>) hs.get(key);
                                                        for(String uid : uids.keySet()){
                                                            FirebaseDatabase.getInstance()
                                                                    .getReference("GROUP_WAITING/MEMBER/"+uid+"/"+selectedGroup.getGroupUid()).setValue(null);
                                                        }
                                                        FirebaseDatabase.getInstance()
                                                                .getReference("GROUP_WAITING/LEADER/"+selectedGroup.getGroupLeaderUid()+"/"+key).setValue(null);
                                                    }}}}}
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                                Toast.makeText(requireActivity().getApplicationContext(), "그룹 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                FragmentManager fragmentManager = Objects.requireNonNull(requireActivity()).getSupportFragmentManager();
                                fragmentManager.beginTransaction().remove(ShowGroupFragment.this).commit();
                                fragmentManager.popBackStack();
                            }
                        });
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alertDialog_deleteGroup = deleteGroupAlert_builder.create();

        alertDialog_deleteGroup.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog_deleteGroup.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                alertDialog_deleteGroup.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        });
        alertDialog_deleteGroup.show();
    }
    private void showViewGroupMembersAlertDialog() {
        Context context = requireActivity();
        AlertDialog.Builder viewGroupMember_builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewGroupMembersView = layoutInflater.inflate(R.layout.show_group_show_member_dialog_layout, null);

        ArrayList<User> members = new ArrayList<User>();
        RecyclerView show_member_list_recycler = viewGroupMembersView.findViewById(R.id.show_member_list_recycler);
        show_member_list_recycler.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        show_member_list_recycler.setLayoutManager(mLayoutManager);
        GroupMemberAdapter groupMemberAdapter = new GroupMemberAdapter(context,members,selectedGroup);
        groupMemberAdapter.setItem(members);
        show_member_list_recycler.setAdapter(groupMemberAdapter);

        FirebaseDatabase.getInstance().getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupMembers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String userUid =  dataSnapshot.getValue(String.class);
                        Query query = FirebaseDatabase.getInstance().getReference("USER").orderByKey().equalTo(userUid);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChildren()){
                                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        System.out.println(dataSnapshot.getValue(User.class).getUid());
                                        members.add(dataSnapshot.getValue(User.class));
                                    }
                                    groupMemberAdapter.setItem(members);
                                    viewGroupMember_builder.setView(viewGroupMembersView);
                                    AlertDialog alertDialog_viewGroupMember = viewGroupMember_builder.create();
                                    alertDialog_viewGroupMember.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    alertDialog_viewGroupMember.show();

                                    //다이어로그 크기 조절
                                    WindowManager.LayoutParams params = alertDialog_viewGroupMember.getWindow().getAttributes();
                                    DisplayMetrics dm = alertDialog_viewGroupMember.getContext().getResources().getDisplayMetrics();
                                    // int widthDp = Math.round(400 * dm.density);
                                    int heightDp = Math.round(600 * dm.density);
                                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    params.height = heightDp;
                                    alertDialog_viewGroupMember.getWindow().setAttributes(params);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });}}
                else{
                    Toast.makeText(context, "그룹원이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

//        try{Thread.sleep(1000);}
//        catch (InterruptedException e){e.printStackTrace();}
    }
    private void showAlertRenewGroupRoutine(boolean isThereRoutine) {
        AlertDialog.Builder renewGroupRoutineAlert_builder = new AlertDialog.Builder(requireActivity());
        String message = null;
        if(isThereRoutine) message = "기존 오늘의 그룹 운동을 삭제하시고\n 새로운 오늘의 그룹 운동을 작성하시겠습니까 ?";
        else message = "새로운 오늘의 그룹 운동을 작성하시겠습니까 ?";
        renewGroupRoutineAlert_builder.setTitle("확인").setMessage(message)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = new Bundle();
                        bundle.putString("mode","custom"); bundle.putString("from","group"); bundle.putSerializable("group",selectedGroup);
                        RoutineViewFragment routineViewFragment = new RoutineViewFragment();
                        routineViewFragment.setArguments(bundle);
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().add(R.id.tab_content_view,routineViewFragment).addToBackStack(null).commit();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alertDialog_renewGroupRoutine = renewGroupRoutineAlert_builder.create();

        alertDialog_renewGroupRoutine.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog_renewGroupRoutine.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                alertDialog_renewGroupRoutine.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        });
        alertDialog_renewGroupRoutine.show();
    }
    private void showGroupRoutineOneDay(@NonNull DataSnapshot snapshot,String selectedDate) {
        if (snapshot.hasChildren())  isGroup =true;
        else if(currentUid.equals(selectedGroup.getGroupLeaderUid())) isGroup = true;
        else isGroup = false;
        String groupRoutine = null;
        GroupRoutine todayGroupRoutine = null;
        //String today = new GetToday().getToday();
        HashMap<String,GroupRoutine> groupRoutineHashMap = selectedGroup.getGroupRoutines();
        if(groupRoutineHashMap==null || !groupRoutineHashMap.containsKey(selectedDate)){
            Toast.makeText(requireActivity().getApplicationContext(), selectedDate+" 그룹 운동이 없습니다.", Toast.LENGTH_SHORT).show();
        }else{
            todayGroupRoutine = groupRoutineHashMap.get(selectedDate);
            Fragment routineViewFragment = new RoutineViewFragment();
            Bundle bundle = new Bundle();
            bundle.putString("mode","select");
            bundle.putString("from","group");
            bundle.putString("groupView","groupView");
            bundle.putSerializable("group",selectedGroup);
            bundle.putBoolean("isGroup",isGroup);
            bundle.putString("groupRoutineName",todayGroupRoutine.getGroupRoutineName());
            LinkedTreeMap<String,ArrayList<RoutineInfoListItemDetailed>> hashMap;
            String fromJson =  todayGroupRoutine.getGroupRoutineExercise();
            Type type = new TypeToken<LinkedTreeMap< String, ArrayList<RoutineInfoListItemDetailed>>>() {}.getType();
            hashMap = new Gson().fromJson(fromJson, type);
            ArrayList<RoutineInfoListItem> routineInfoListItems = new ArrayList<>();
            for(String key : hashMap.keySet()) {
                Exercise exercise = new Gson().fromJson(key,Exercise.class);
                ArrayList<RoutineInfoListItemDetailed> itemDetailedArrayList =
                        (ArrayList<RoutineInfoListItemDetailed>)hashMap.get(key);
                RoutineInfoListItem routineInfoListItem = new RoutineInfoListItem(exercise, itemDetailedArrayList);
                routineInfoListItems.add(routineInfoListItem);
            }
            RoutineItem routineItem = new RoutineItem(null,routineInfoListItems);
            bundle.putSerializable("selectRoutine",routineItem);
            routineViewFragment.setArguments(bundle);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.tab_content_view, routineViewFragment).addToBackStack(null).commit();
        }
    }
    private void showAlertLeaveGroup() {
        AlertDialog.Builder deleteGroupAlert_builder = new AlertDialog.Builder(requireActivity());
        deleteGroupAlert_builder.setTitle("확인").setMessage("정말로 그룹을 나가시겠습니까 ?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase.getInstance().getReference("GROUP/"+selectedGroup.getGroupUid()+"/groupMembers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChildren()) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        String memberUid = dataSnapshot.getValue(String.class);
                                        if(memberUid.equals(currentUid)){
                                            dataSnapshot.getRef().setValue(null);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        onBackPressed();
                    }

                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog alertDialog_deleteGroup = deleteGroupAlert_builder.create();

        alertDialog_deleteGroup.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog_deleteGroup.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                alertDialog_deleteGroup.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        });
        alertDialog_deleteGroup.show();
    }


}


